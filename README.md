# K_server_project
K사 서버 개발 과제

---

## API 명세

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/coffee/menus` | 커피 메뉴 목록 조회 |
| GET | `/api/coffee/menus/populars` | 인기 메뉴 Top3 조회 (최근 7일) |
| POST | `/api/points` | 포인트 충전 |
| POST | `/api/coffee/orders` | 커피 주문 + 포인트 결제 |
| POST | `/api/coffee/orders/portone` | 커피 주문 생성 (포트원 결제용) |
| POST | `/api/coffee/payments/portone` | 포트원 결제 검증 |
| POST | `/api/coffee/payments/webhook` | 포트원 웹훅 수신 |

---

## 🗄ERD

| 테이블 | 설명 |
|--------|------|
| users | 사용자 (userKey로 외부 식별) |
| menus | 커피 메뉴 |
| points | 포인트 잔액 |
| points_log | 포인트 변동 이력 |
| orders | 주문 |
| orders_log | 주문 상태 변경 이력 |
| payments | 결제 정보 |
| payments_log | 결제 상태 변경 이력 |

---

## 캐싱 전략

### 메뉴 목록 캐싱 — `@Cacheable`

메뉴 목록은 자주 변경되지 않는 데이터임에도 모든 API 요청마다 DB를 조회하면 불필요한 부하가 발생합니다.
특히 다수의 인스턴스가 동시에 운영되는 환경에서는 인스턴스마다 독립적으로 DB를 조회하기 때문에 부하가 인스턴스 수에 비례해서 증가합니다.

이를 해결하기 위해 Redis를 중앙 캐시 저장소로 사용해 `@Cacheable(cacheNames = "menus", key = "'all'")`을 적용했습니다.
첫 번째 요청에서만 DB를 조회하고 결과를 Redis에 저장하며, 이후 요청은 Redis에서 바로 반환합니다.
TTL은 5분으로 설정해 메뉴 데이터 변경이 최대 5분 이내에 반영되도록 했습니다.
```java
@Cacheable(cacheNames = "menus", key = "'all'")
@Transactional(readOnly = true)
public List<MenuResponse> getMenus() {
    log.info("[Cache MISS] 메뉴 목록 DB 조회");
    return menuRepository.findAllByIsActiveTrue()
            .stream()
            .map(MenuResponse::from)
            .toList();
}
```

Redis를 로컬 캐시(`Caffeine` 등) 대신 선택한 이유는 다중 인스턴스 환경에서 모든 인스턴스가 동일한 캐시 데이터를 공유해야 하기 때문입니다.
로컬 캐시는 인스턴스별로 독립적으로 관리되어 한 인스턴스에서 캐시를 무효화해도 다른 인스턴스에는 반영되지 않는 문제가 있습니다.

향후 메뉴 추가/수정 API가 생기면 `@CacheEvict`로 캐시를 즉시 무효화할 수 있도록 `evictMenuCache()` 메서드를 미리 준비해두었습니다.

### 인기 메뉴 집계 — Redis Sorted Set

최근 7일 인기 메뉴 Top3를 매 요청마다 DB에서 `GROUP BY`로 집계하는 방식은 주문 데이터가 쌓일수록 풀스캔 비용이 선형으로 증가합니다.
또한 다중 인스턴스 환경에서는 각 인스턴스가 독립적으로 집계하므로 순간적으로 집계 결과가 달라지는 일관성 문제가 발생합니다.

이를 해결하기 위해 Redis Sorted Set을 사용했습니다.
주문이 완료될 때마다 `ZINCRBY`로 해당 메뉴의 점수를 원자적으로 1 증가시키고,
조회 시에는 `ZUNIONSTORE`로 최근 7일치 키를 합산해 `ZREVRANGE`로 Top3를 O(log N) 시간 복잡도로 바로 반환합니다.
```
키 설계: coffee:popular:2026-04-06 (날짜별 분리)
TTL: 8일 (7일치 데이터 보장 + 1일 여유)

주문 성공 시:
  ZINCRBY coffee:popular:2026-04-06 {menuId} 1

인기 메뉴 조회 시:
  ZUNIONSTORE coffee:popular:result:2026-04-06
    coffee:popular:2026-04-06
    coffee:popular:2026-04-05
    ... (7일치)
  ZREVRANGE coffee:popular:result:2026-04-06 0 2 WITHSCORES
```

`ZINCRBY`는 Redis의 원자적 연산이기 때문에 다수의 인스턴스에서 동시에 주문이 발생해도 카운트가 누락되거나 중복되지 않습니다.
Redis 장애 시에는 DB 직접 집계로 폴백해 가용성을 보장했습니다.

---

## 동시성 제어

### 문제 상황

다수의 서버 인스턴스가 동시에 운영되는 환경에서 포인트 충전과 차감이 동시에 요청될 경우 아래와 같은 문제가 발생할 수 있습니다.
```
Thread A: balance 읽기 (10000원)
Thread B: balance 읽기 (10000원)  ← 동시에 같은 값을 읽음
Thread A: 3000원 차감 → 7000원 저장
Thread B: 3000원 차감 → 7000원 저장  ← A의 차감이 덮어씌워짐
결과: 6000원이 되어야 하는데 7000원으로 남음 (3000원 손실)
```

이 문제를 해결하기 위해 충전과 차감의 성격이 다르다는 점에 주목해 서로 다른 락 전략을 적용했습니다.

---

### 포인트 충전 — 낙관적 락 (`@Version`)

**선택 이유**

포인트 충전은 같은 유저에 대한 동시 요청이 빈번하지 않고, 충전 실패가 사용자 경험에 즉각적인 영향을 주지 않아 재시도가 가능한 작업입니다.
비관적 락은 충돌이 없는 상황에서도 항상 DB 락을 잡고 대기시키기 때문에 트래픽이 몰리면 오히려 병목이 됩니다.
낙관적 락은 실제 충돌이 발생했을 때만 비용이 발생하므로 일반적인 트래픽 환경에서 성능이 더 우수합니다.

**동작 원리**

`Point` 엔티티에 `@Version int version` 필드를 추가합니다.
트랜잭션이 시작될 때 현재 `version`을 함께 읽고, 커밋 시점에 DB의 `version`이 읽었던 값과 같은지 확인합니다.
다른 트랜잭션이 먼저 커밋해 `version`이 바뀌어 있으면 `ObjectOptimisticLockingFailureException`이 발생해 충돌을 감지합니다.
```java
// Point.java
@Version
private int version;
```
```sql
-- JPA가 내부적으로 실행하는 UPDATE 쿼리
UPDATE points
SET balance = ?, version = version + 1
WHERE id = ? AND version = ?  -- version이 다르면 0 rows affected → 예외 발생
```

**락 전략 비교**

| 전략 | 충돌 없을 때 | 충돌 있을 때 | 다중 인스턴스 |
|------|-------------|-------------|--------------|
| 비관적 락 | 락 획득 대기 발생 | 순차 처리 | 가능 |
| 낙관적 락 ✅ | 락 없이 바로 처리 | 예외 후 재시도 | 가능 |
| Redis 분산 락 | 락 획득 대기 발생 | 순차 처리 | 가능 |

충전 요청이 동시에 몰리는 경우가 드물기 때문에 충돌 빈도가 낮고, 낙관적 락이 가장 효율적인 선택입니다.

---

### 포인트 차감 — 비관적 락 (`PESSIMISTIC_WRITE`)

**선택 이유**

주문 시 포인트 차감은 충전과 성격이 다릅니다.

- 잔액 부족 시 즉시 실패해야 하며 재시도가 의미 없습니다.
- 잔액이 음수가 되는 상황을 절대로 허용하면 안 됩니다.
- 낙관적 락은 충돌 감지 후 예외를 던지고 롤백하는데, 이 시점에 이미 다른 차감이 완료되어 잔액이 음수가 될 수 있습니다.

따라서 차감 요청이 들어오는 순간 해당 행에 배타적 락을 걸어 다른 트랜잭션이 접근 자체를 못하도록 막는 비관적 락을 선택했습니다.

**동작 원리**
```java
// PointRepository.java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Point p WHERE p.user = :user")
Optional<Point> findByUserWithLock(@Param("user") User user);
```
```sql
-- JPA가 내부적으로 실행하는 SELECT 쿼리
SELECT * FROM points WHERE user_id = ? FOR UPDATE  -- 행 단위 배타 락
```

`FOR UPDATE`가 붙으면 해당 행을 다른 트랜잭션이 읽거나 수정할 수 없고 대기 상태가 됩니다.
락을 획득한 트랜잭션이 커밋 또는 롤백하면 그 다음 트랜잭션이 락을 획득합니다.
이 방식으로 동시에 여러 차감 요청이 들어와도 반드시 순차적으로 처리되어 잔액이 음수가 되는 상황을 원천 차단합니다.

**처리 흐름**
```
Thread A: SELECT ... FOR UPDATE (락 획득)
Thread B: SELECT ... FOR UPDATE (대기)

Thread A: balance 10000 → 3000 차감 → 7000 저장 후 커밋 (락 해제)
Thread B: 락 획득 → balance 7000 읽기 → 3000 차감 → 4000 저장 후 커밋

결과: 10000 - 3000 - 3000 = 4000 (정확)
```

---

### 충전 vs 차감 전략 분리 이유

| 구분 | 충전 | 차감 |
|------|------|------|
| 락 전략 | 낙관적 락 (`@Version`) | 비관적 락 (`FOR UPDATE`) |
| 실패 허용 | 재시도 가능 | 재시도 불가 (즉시 실패) |
| 정확성 요구 | 보통 | 매우 높음 (음수 절대 불가) |
| 성능 우선순위 | 높음 | 정확성 > 성능 |

두 작업의 실패 허용 수준과 정확성 요구 수준이 다르기 때문에 동일한 락 전략을 적용하지 않았습니다.
충전에 비관적 락을 쓰면 불필요한 대기가 발생하고, 차감에 낙관적 락을 쓰면 잔액 음수 위험이 생깁니다.

---

### 인기 메뉴 집계 동시성 — Redis `ZINCRBY` 원자성

다중 인스턴스 환경에서 여러 서버가 동시에 인기 메뉴 점수를 올리면 카운트가 누락될 수 있습니다.
Redis의 `ZINCRBY`는 단일 명령어로 처리되는 원자적 연산이라 여러 인스턴스에서 동시에 호출해도 카운트가 정확하게 유지됩니다.
```
인스턴스 A: ZINCRBY coffee:popular:2026-04-06 1 1  → score: 1
인스턴스 B: ZINCRBY coffee:popular:2026-04-06 1 1  → score: 2
인스턴스 C: ZINCRBY coffee:popular:2026-04-06 1 1  → score: 3

결과: 3 (누락 없음)
```

Redis 단일 스레드 이벤트 루프가 명령어를 순차적으로 처리하기 때문에 별도의 락 없이도 정확한 카운트가 보장됩니다.
Redis 장애 시에는 주문 트랜잭션에 영향을 주지 않도록 `try-catch`로 감싸 장애를 격리했습니다.
```java
private void updatePopularMenu(Long menuId) {
    try {
        String todayKey = POPULAR_KEY_PREFIX + LocalDate.now();
        redisTemplate.opsForZSet().incrementScore(todayKey, String.valueOf(menuId), 1);
        redisTemplate.expire(todayKey, Duration.ofDays(8));
    } catch (Exception e) {
        log.warn("Redis 인기 메뉴 업데이트 실패 - menuId: {}", menuId, e);
    }
}
```

---

## 포트원 결제 연동

### 결제 흐름
```
1. POST /api/coffee/orders/portone
   → 주문 생성 (PAYMENT_PENDING), merchantUid 발급

2. 클라이언트 → 포트원 SDK로 결제 진행
   → customData에 merchantUid 포함

3. POST /api/coffee/payments/portone
   → 포트원 결제 단건 조회로 서버 검증
   → 금액 검증 (위변조 방지)
   → 결제 확인 (PENDING → SUCCESS)

4. POST /api/coffee/payments/webhook
   → 포트원이 서버로 직접 전송
   → 멱등성 검증 후 처리
```

### 웹훅 멱등성 보장

포트원 웹훅은 네트워크 문제나 서버 응답 지연으로 동일한 결제 이벤트가 여러 번 전송될 수 있습니다.
중복 처리를 막기 위해 `portonePaymentId`를 멱등성 키로 사용합니다.

웹훅이 수신되면 `existsByPortonePaymentId()`로 이미 처리된 결제인지 먼저 확인하고, 처리된 경우 즉시 리턴합니다.
웹훅 응답은 항상 200을 반환해 포트원이 재전송을 멈추도록 합니다.
또한 웹훅 데이터를 그대로 신뢰하지 않고 포트원 API를 재조회해 금액을 검증합니다.

---

## 인덱스 설계

| 테이블 | 인덱스 | 용도 |
|--------|--------|------|
| users | `idx_users_user_key` | 모든 API 진입점에서 userKey로 유저 조회 |
| orders | `idx_orders_ordered_at_status` | 인기 메뉴 DB 폴백 집계 쿼리 (`WHERE ordered_at >= ? AND status = 'PAID'`) |
| orders | `idx_orders_user_id` | 유저별 주문 조회 |
| points | `idx_points_user_id` | 포인트 조회 |
| payments | `idx_payments_portone_payment_id` | 웹훅 멱등성 검증 |
| payments | `idx_payments_merchant_uid` | merchantUid로 결제 조회 |

`ordered_at + status` 복합 인덱스에서 컬럼 순서를 `ordered_at` 먼저 둔 이유는 카디널리티 때문입니다.
`ordered_at`은 값의 종류가 많아(높은 카디널리티) 먼저 범위를 좁혀주고, `status`는 세 가지 값만 존재해(낮은 카디널리티) 뒤에 위치합니다.
순서를 반대로 하면 인덱스 효율이 크게 떨어집니다.

---