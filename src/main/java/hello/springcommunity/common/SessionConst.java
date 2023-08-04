package hello.springcommunity.common;

/**
 * HttpSession 에 데이터를 보관하고 조회할 때, 같은 이름이 중복되어 사용되므로 상수를 하나 정의한다
 * static final 로 글자만 참조할 것이기 때문에, 추상 클래스나 인터페이스로 생성한다
 */
public class SessionConst {
    public static final String LOGIN_MEMBER = "loginMember";
}
