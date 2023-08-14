package hello.springcommunity.config.oauth;

import hello.springcommunity.dao.member.MemberRepository;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.dto.security.UserDetailsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("access token={}", userRequest.getAccessToken().getTokenValue());

        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("getAttributes : {}", oAuth2User.getAttributes());
        /**
         * google
         * {sub=XXX, name=XXX, given_name=XX(이름), family_name=X(성), picture=XXX, email=XXXX@gmail.com, email_verified=true, locale=ko}
         * naver
         * {resultcode=00, message=success, response={id=AvMxLlwRODQIV8a35S8A6Ru-sCuz_I_HDL1YLqXgMC4, nickname=JS, email=morefromjs@gmail.com, name=이재성}}
         */

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();
        log.info("userNameAttributeName={}", userNameAttributeName);// sub, response

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
        log.info("attributes={}", attributes);

        if (!StringUtils.hasText(attributes.getEmail())) {
            log.error("Email not found from OAuth2 provider");
            //throw new RuntimeException("Email not found from OAuth2 provider")
        }

        Member member = saveOrUpdate(attributes);
        httpSession.setAttribute("oauth2member", new UserSessionDTO(member, userRequest.getAccessToken().getTokenValue()));

        return new UserDetailsDTO(member, oAuth2User.getAttributes());

    }

    private Member saveOrUpdate(OAuthAttributes attributes) {
//        Optional<Member> member = memberRepository.findByEmail(attributes.getEmail());
        Optional<Member> member = memberRepository.findByLoginId(attributes.getOauth2Id());

        if(member.isEmpty()) {
            return memberRepository.save(attributes.toEntity());
        }else {
            return member.get().updateModifiedDate();
        }

    }
}
