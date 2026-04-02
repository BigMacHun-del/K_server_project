package sparta.coffee_shop.constants;

public class Constants {

    // 공통
    public static final String MSG_NOT_VALID_VALUE = "유효하지 않은 값이 입력되었습니다.";
    public static final String MSG_DATA_INSERT_FAIL = "데이터 등록에 실패하였습니다.";
    public static final String MSG_SERVER_ERROR_OCCUR = "서버 오류가 발생하였습니다. 잠시 후 다시 시도 바랍니다.";

    // 유저
    public static final String MSG_USER_NOT_FOUND = "존재하지 않는 사용자입니다.";

    // 포인트
    public static final String MSG_POINT_NOT_FOUND = "포인트 정보를 찾을 수 없습니다.";
    public static final String MSG_POINT_INSUFFICIENT = "포인트 잔액이 부족합니다.";

    // 메뉴
    public static final String MSG_MENU_NOT_FOUND = "존재하지 않는 메뉴입니다.";
    public static final String MSG_MENU_NOT_ACTIVE = "현재 주문할 수 없는 메뉴입니다.";

    // 주문
    public static final String MSG_ORDER_NOT_FOUND = "존재하지 않는 주문입니다.";

    // 결제
    public static final String MSG_PAYMENT_NOT_FOUND = "결제 정보를 찾을 수 없습니다.";
    public static final String MSG_PAYMENT_AMOUNT_MISMATCH = "결제 금액이 일치하지 않습니다.";
    public static final String MSG_PAYMENT_ALREADY_PROCESSED = "이미 처리된 결제입니다.";
    public static final String MSG_PAYMENT_PORTONE_FAILED = "포트원 결제 검증에 실패했습니다.";
}
