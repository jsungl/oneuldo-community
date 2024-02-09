package hello.springcommunity.common;

/**
 * HttpSession 에 데이터를 보관하고 조회할 때, 같은 이름이 중복되어 사용되므로 상수를 통해 정의한다
 * static final 로 글자만 참조할 것이기 때문에, 추상 클래스나 인터페이스로 생성한다
 */
public class SessionConst {

    public static final String LOGIN_MEMBER = "loginMember";

    /* 소셜 로그인한 회원의 정보가 담긴 세션데이터(UserSessionDTO) */
    public static final String OAUTH2_MEMBER = "oauth2Member";

    /* 아이디 찾기시 조회한 Member 객체 */
    public static final String FIND_MEMBER = "findMember";

    /* 비밀번호 찾기를 한 FindPasswordDTO 객체 */
    public static final String FIND_MEMBER_PASSWORD = "findPassword";

    /* 소셜 계정 사용자의 랜덤 아이디 */
    public static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";

}
