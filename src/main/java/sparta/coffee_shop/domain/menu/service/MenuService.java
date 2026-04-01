package sparta.coffee_shop.domain.menu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.coffee_shop.domain.menu.dto.response.MenuResponse;
import sparta.coffee_shop.domain.menu.dto.response.PopularMenuResponse;
import sparta.coffee_shop.domain.menu.repository.MenuRepository;
import sparta.coffee_shop.domain.order.repository.OrderRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private static final String POPULAR_KEY_PREFIX = "coffee:popular:";
    private static final int TOP_COUNT = 3;
    private static final int POPULAR_DAYS = 7;

    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional(readOnly = true)
    public List<MenuResponse> getMenus() {
        return menuRepository.findAllByIsActiveTrue()
                .stream()
                .map(MenuResponse::from)
                .toList();
    }

    public List<PopularMenuResponse> getPopularMenus() {
        try {
            return getPopularMenusFromRedis();
        } catch (Exception e) {
            log.warn("Redis 조회 실패, DB 폴백 실행", e);
            return getPopularMenusFromDb();
        }
    }

    private List<PopularMenuResponse> getPopularMenusFromRedis() {
        // 최근 7일 키 수집
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < POPULAR_DAYS; i++) {
            keys.add(POPULAR_KEY_PREFIX + LocalDate.now().minusDays(i));
        }

        // ZUNIONSTORE로 합산 후 Top3 조회
        String resultKey = POPULAR_KEY_PREFIX + "result:" + LocalDate.now();
        redisTemplate.opsForZSet().unionAndStore(keys.get(0), keys.subList(1, keys.size()), resultKey);

        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(resultKey, 0, TOP_COUNT - 1);

        if (tuples == null || tuples.isEmpty()) {
            return getPopularMenusFromDb();
        }

        List<PopularMenuResponse> result = new ArrayList<>();
        int[] rank = {1};
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            Long menuId = Long.parseLong(Objects.requireNonNull(tuple.getValue()));
            menuRepository.findById(menuId).ifPresent(menu ->
                    result.add(PopularMenuResponse.of(
                            rank[0]++,
                            menu.getId(),
                            menu.getProductName(),
                            menu.getPrice(),
                            Objects.requireNonNull(tuple.getScore()).longValue()
                    ))
            );
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<PopularMenuResponse> getPopularMenusFromDb() {
        LocalDateTime from = LocalDateTime.now().minusDays(POPULAR_DAYS);
        List<Object[]> rows = orderRepository.findTop3MenuIdsByOrderedAtAfter(from);

        List<PopularMenuResponse> result = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Long menuId = (Long) rows.get(i)[0];
            long count = (long) rows.get(i)[1];
            int rank = i + 1;
            menuRepository.findById(menuId).ifPresent(menu ->
                    result.add(PopularMenuResponse.of(rank, menu.getId(), menu.getProductName(), menu.getPrice(), count))
            );
        }
        return result;
    }
}