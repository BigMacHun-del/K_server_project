package sparta.coffee_shop.domain.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sparta.coffee_shop.common.response.BaseResponse;
import sparta.coffee_shop.domain.order.dto.request.OrderRequest;
import sparta.coffee_shop.domain.order.dto.request.PortoneOrderRequest;
import sparta.coffee_shop.domain.order.dto.response.OrderResponse;
import sparta.coffee_shop.domain.order.dto.response.PortoneOrderResponse;
import sparta.coffee_shop.domain.order.service.OrderService;
import sparta.coffee_shop.domain.order.service.PortoneOrderService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coffee/orders")
public class OrderController {

    private final OrderService orderService;
    private final PortoneOrderService portoneOrderService;

    @PostMapping
    public ResponseEntity<BaseResponse<OrderResponse>> order(
            @Valid @RequestBody OrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse.success("201", "주문 및 결제 성공", orderService.order(request))
        );
    }

    // 포트원 주문 생성 (결제 전 주문만)
    @PostMapping("/portone")
    public ResponseEntity<BaseResponse<PortoneOrderResponse>> createPortoneOrder(
            @Valid @RequestBody PortoneOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse.success("201", "주문 생성 성공", portoneOrderService.createOrder(request))
        );
    }
}