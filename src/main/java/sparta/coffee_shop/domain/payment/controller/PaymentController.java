package sparta.coffee_shop.domain.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sparta.coffee_shop.common.response.BaseResponse;
import sparta.coffee_shop.domain.payment.dto.request.PortonePaymentRequest;
import sparta.coffee_shop.domain.payment.dto.request.PortoneWebhookRequest;
import sparta.coffee_shop.domain.payment.dto.response.PortonePaymentResponse;
import sparta.coffee_shop.domain.payment.service.PortonePaymentService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coffee/payments")
public class PaymentController {

    private final PortonePaymentService portonePaymentService;

    // 클라이언트 결제 완료 후 서버 검증
    @PostMapping("/portone")
    public ResponseEntity<BaseResponse<PortonePaymentResponse>> confirmPayment(
            @Valid @RequestBody PortonePaymentRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success("200", "결제 검증 완료", portonePaymentService.confirmPayment(request))
        );
    }

    // 포트원 웹훅 수신 (항상 200 반환 - 포트원이 재전송 멈추도록)
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody PortoneWebhookRequest request
    ) {
        log.info("[웹훅 수신] type={}", request.getType());
        portonePaymentService.handleWebhook(request);
        return ResponseEntity.ok().build();
    }
}