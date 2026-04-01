package sparta.coffee_shop.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sparta.coffee_shop.domain.order.entity.Order;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Redis 장애 시 폴백용 - 최근 7일 PAID 주문 기준 Top3
    @Query("""
            SELECT o.menu.id, COUNT(o.id)
            FROM Order o
            WHERE o.orderedAt >= :from
            AND o.status = 'PAID'
            GROUP BY o.menu.id
            ORDER BY COUNT(o.id) DESC
            LIMIT 3
            """)
    List<Object[]> findTop3MenuIdsByOrderedAtAfter(@Param("from") LocalDateTime from);
}