package sparta.coffee_shop.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static sparta.coffee_shop.constants.Constants.*;


@Getter
public enum ErrorEnum {

    // 유저
    ERR_USER_NOT_FOUND(HttpStatus.NOT_FOUND, MSG_USER_NOT_FOUND),

    // 포인트
    ERR_POINT_NOT_FOUND(HttpStatus.NOT_FOUND, MSG_POINT_NOT_FOUND),
    ERR_POINT_INSUFFICIENT(HttpStatus.BAD_REQUEST, MSG_POINT_INSUFFICIENT);

    private final HttpStatus status;
    private final String message;

    ErrorEnum(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
