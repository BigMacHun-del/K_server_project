package sparta.coffee_shop.domain.point.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.coffee_shop.common.exception.ErrorEnum;
import sparta.coffee_shop.common.exception.ServiceErrorException;
import sparta.coffee_shop.domain.point.dto.request.PointChargeRequest;
import sparta.coffee_shop.domain.point.dto.response.PointChargeResponse;
import sparta.coffee_shop.domain.point.entity.Point;
import sparta.coffee_shop.domain.point.repository.PointRepository;
import sparta.coffee_shop.domain.pointlog.entity.PointLog;
import sparta.coffee_shop.domain.pointlog.entity.PointLogType;
import sparta.coffee_shop.domain.pointlog.repository.PointLogRepository;
import sparta.coffee_shop.domain.user.entity.User;
import sparta.coffee_shop.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final PointLogRepository pointLogRepository;

    @Transactional
    public PointChargeResponse charge(PointChargeRequest request) {
        User user = userRepository.findByUserKey(request.getUserKey())
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_USER_NOT_FOUND));

        Point point = pointRepository.findByUser(user)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_POINT_NOT_FOUND));

        point.charge(request.getAmount());

        pointLogRepository.save(new PointLog(
                user, point, PointLogType.CHARGE, request.getAmount(), point.getBalance()
        ));

        return PointChargeResponse.of(user.getUserKey(), point.getBalance());
    }
}