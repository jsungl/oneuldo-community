package hello.springcommunity.config.oauth;

import hello.springcommunity.dao.member.MemberRepository;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.util.Collections;

/**
 * oauth 인증이 정상적으로 완료되었을 때 회원 정보를 처리하기 위한 custom 클래스
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;
    private final HttpSession httpSession;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        /* OAuth2UserService의 기본 구현체인 DefaultOAuth2UserService를 통해 User 정보를 가져와야 하기 때문에 대리자 생성 */
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        log.info("getAttributes : {}", oAuth2User.getAttributes());

        /* OAuth2 서비스 id 구분코드 ( google, naver ) */
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        /* OAuth2 로그인 진행시 키가 되는 필드 값(PK) (구글의 기본 코드는 "sub") */
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();
        log.info("userNameAttributeName={}", userNameAttributeName);

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
        /**
         * oAuth2User.getAttributes()
         * {sub=XXX, name=XXX, given_name=XX(이름), family_name=X(성), picture=XXX, email=XXXX@gmail.com, email_verified=true, locale=ko}
         */

        if (!StringUtils.hasText(attributes.getEmail())) {
            log.error("Email not found from OAuth2 provider");
            //throw new RuntimeException("Email not found from OAuth2 provider")
        }

        /* 로그인한 유저 정보 */
        Member member = saveOrUpdate(attributes);

        /* 세션에 사용자 정보를 저장하는 직렬화된 DTO 클래스 */
        //TODO: 보안을 위해 회원정보를 어떤걸 넣어야하나
        //httpSession.setAttribute("member", new UserSessionDTO(member));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRoleValue())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }


    /**
     * 소셜 로그인 시 해당 이메일로 가입한 기존 회원이 존재하는 경우 수정 날짜 정보만 업데이트하고 기존의 데이터는 그대로 보존
     * 닉네임이나 프로필 등은 회원 정보에서 변경가능하므로 소셜계정에서 변경했더라도 기존 데이터는 유지한다
     *
     * 처음 가입하는 회원이라면 새로운 회원 정보를 만들어서 저장
     */
    private Member saveOrUpdate(OAuthAttributes attributes) {
        Member member = memberRepository.findByEmail(attributes.getEmail())
                .map(m -> m.updateModifiedDate())
                .orElse(attributes.toEntity());

        log.info("loginId={}", member.getLoginId());
        log.info("nickname={}", member.getNickname());
        log.info("email={}", member.getEmail());
        log.info("role={}", member.getRoleValue());

        return memberRepository.save(member);
    }

}
