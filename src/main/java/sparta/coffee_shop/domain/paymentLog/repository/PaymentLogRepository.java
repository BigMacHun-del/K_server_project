package sparta.coffee_shop.domain.paymentLog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.coffee_shop.domain.paymentLog.entity.PaymentLog;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {
}
