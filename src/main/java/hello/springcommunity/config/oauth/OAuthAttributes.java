package hello.springcommunity.config.oauth;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.Role;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 소셜 로그인 성공시 사용자 정보를 담을 클래스
 * 가져오기로 한 범위(scope)들을 필드로 정의
 */

@Slf4j
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthAttributes {

    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String oauth2Id;
    private String nickname;
    private String email;
    private Role role;

    /**
     * 구글 로그인인지, 네이버 로그인인지 확인
     */
    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {

        if("naver".equals(registrationId)) {
            return ofNaver(registrationId, "id", attributes);
        }

        return ofGoogle(registrationId, userNameAttributeName, attributes);
    }

    /**
     * 네이버 로그인시 가져온 attribute를 설정
     *
     * 네이버 로그인을 통한 정보 제공 관련 정책 변경으로 현재 naverid@naver.com 형식의 이메일 정보는 제공하지 않고 있으며
     * 정책 변경 이후에 생성된 애플리케이션에는 '연락처 이메일' 정보로 제공됨
     * 사용자별로 '연락처 이메일'을 naverid@naver.com으로 설정한 경우가 있을 수는 있으나,
     * 제공되는 이메일 정보는 '연락처 이메일' 정보이며 회원별로 고유한 값이 아니다.
     */
    private static OAuthAttributes ofNaver(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        /* JSON 형태이기 때문에 Map을 통해 데이터를 가져온다 */
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        log.info("응답={}", response);
        /*{id=AvMxLlwRODQIV8a35S8A6Ru-sCuz_I_HDL1YLqXgMC4, nickname=JS, email=morefromjs@gmail.com, name=이재성}*/

        return OAuthAttributes.builder()
                .oauth2Id(registrationId + "_" + response.get("id"))
                //.nickname(response.get("nickname") + "_" + response.get("id"))
                .nickname("n_" + response.get("id").toString().substring(0,7))
                .email((String) response.get("email"))
                .attributes(response)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    /**
     * 구글 로그인시 가져온 attribute를 설정
     */
    private static OAuthAttributes ofGoogle(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .oauth2Id(registrationId + "_" + attributes.get("sub"))
                //.nickname(attributes.get("name") + "_" + attributes.get("sub"))
                .nickname("g_" + attributes.get("sub").toString().substring(0,7))
                .email((String) attributes.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }


    /**
     * 처음 소셜 로그인시 회원가입 -> DB에 저장
     */
    public Member toEntity() {
        return Member.builder()
                .loginId(oauth2Id) //provider_해당 유저 고유 ID 로 설정
                .nickname(nickname)
                .email(email)
                .role(Role.SOCIAL)
                .build();
    }
}
