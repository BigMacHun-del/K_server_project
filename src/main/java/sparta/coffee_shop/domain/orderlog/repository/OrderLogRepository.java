package sparta.coffee_shop.domain.orderlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.coffee_shop.domain.orderlog.entity.OrderLog;

public interface OrderLogRepository extends JpaRepository<OrderLog, Long> {

}
