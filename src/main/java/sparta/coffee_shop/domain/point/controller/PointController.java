package sparta.coffee_shop.domain.point.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sparta.coffee_shop.common.response.BaseResponse;
import sparta.coffee_shop.domain.point.dto.request.PointChargeRequest;
import sparta.coffee_shop.domain.point.dto.response.PointChargeResponse;
import sparta.coffee_shop.domain.point.service.PointService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;

    @PostMapping
    public ResponseEntity<BaseResponse<PointChargeResponse>> charge(
            @Valid @RequestBody PointChargeRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success("200", "포인트 충전 성공", pointService.charge(request))
        );
    }
}