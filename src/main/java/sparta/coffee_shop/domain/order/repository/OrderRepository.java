package sparta.coffee_shop.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.coffee_shop.domain.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
