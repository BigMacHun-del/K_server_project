package sparta.coffee_shop.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.coffee_shop.domain.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
}
