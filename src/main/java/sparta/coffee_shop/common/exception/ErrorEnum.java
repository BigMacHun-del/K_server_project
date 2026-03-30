package sparta.coffee_shop.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static sparta.coffee_shop.constants.Constants.MSG_USER_NOT_FOUND;


@Getter
public enum ErrorEnum {

    // 유저
    ERR_USER_NOT_FOUND(HttpStatus.NOT_FOUND, MSG_USER_NOT_FOUND);

    private final HttpStatus status;
    private final String message;

    ErrorEnum(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
