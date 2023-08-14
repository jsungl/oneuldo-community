package hello.springcommunity.config.oauth;

import hello.springcommunity.domain.member.Member;
import lombok.Getter;

import java.io.Serializable;

/**
 * 세션에 인증된 사용자 정보를 저장하기 위한 DTO 클래스
 */

@Getter
public class UserSessionDTO implements Serializable {

    private String loginId;
    private String nickname;
    private String email;
    private String role;
    private String accessToken;

    public UserSessionDTO(Member member, String accessToken) {
        this.loginId = member.getLoginId();
        this.nickname = member.getNickname();
        this.email = member.getEmail();
        this.role = member.getRoleValue();
        this.accessToken = accessToken;
    }
}
