package sparta.coffee_shop.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static sparta.coffee_shop.constants.Constants.*;

@Getter
public enum ErrorEnum {

    // 유저
    ERR_USER_NOT_FOUND(HttpStatus.NOT_FOUND, MSG_USER_NOT_FOUND),

    // 메뉴
    ERR_MENU_NOT_FOUND(HttpStatus.NOT_FOUND, MSG_MENU_NOT_FOUND),
    ERR_MENU_NOT_ACTIVE(HttpStatus.BAD_REQUEST, MSG_MENU_NOT_ACTIVE),

    // 포인트
    ERR_POINT_NOT_FOUND(HttpStatus.NOT_FOUND, MSG_POINT_NOT_FOUND),
    ERR_POINT_INSUFFICIENT(HttpStatus.BAD_REQUEST, MSG_POINT_INSUFFICIENT),

    // 주문
    ERR_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, MSG_ORDER_NOT_FOUND),

    // 결제
    ERR_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, MSG_PAYMENT_NOT_FOUND),
    ERR_PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, MSG_PAYMENT_AMOUNT_MISMATCH),
    ERR_PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, MSG_PAYMENT_ALREADY_PROCESSED),
    ERR_PAYMENT_PORTONE_FAILED(HttpStatus.BAD_REQUEST, MSG_PAYMENT_PORTONE_FAILED);

    private final HttpStatus status;
    private final String message;

    ErrorEnum(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}