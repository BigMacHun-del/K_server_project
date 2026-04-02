package sparta.coffee_shop.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.coffee_shop.domain.payment.entity.Payment;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 웹훅 멱등성 검증: 동일 portonePaymentId 중복 처리 방지
    boolean existsByPortonePaymentId(String portonePaymentId);

    Optional<Payment> findByMerchantUid(String merchantUid);
}