package hello.springcommunity.config.oauth;

import hello.springcommunity.dao.member.RefreshTokenRepository;
import hello.springcommunity.dao.member.MemberRepository;
import hello.springcommunity.domain.member.RefreshToken;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.dto.security.UserDetailsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.util.Optional;

import static hello.springcommunity.common.SessionConst.OAUTH2_MEMBER;

/**
 * oauth 인증이 정상적으로 완료되었을 때 회원 정보를 처리하기 위한 custom 클래스
 */

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final HttpSession httpSession;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        String accessToken = userRequest.getAccessToken().getTokenValue();
        String refreshToken = (String) userRequest.getAdditionalParameters().get(OAuth2ParameterNames.REFRESH_TOKEN);

        log.info("접근토큰={}", accessToken);
        log.info("갱신토큰={}", refreshToken);

        //DefaultOAuth2UserService의 loadUser()는 소셜 로그인 API의 사용자 정보 제공 URI로 요청을 보내서
        //사용자 정보를 얻은 후, 이를 통해 DefaultOAuth2User 객체를 생성 후 반환한다.
        //결과적으로, OAuth2User는 OAuth 서비스에서 가져온 유저 정보를 담고 있는 유저
        OAuth2User oAuth2User = super.loadUser(userRequest);
        //log.info("getAttributes : {}", oAuth2User.getAttributes());

        //OAuth2 서비스 id 구분코드 ( google, naver )
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        //OAuth2 로그인 진행시 키가 되는 필드 값(PK) (구글의 기본 코드는 "sub", response)
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();
        //log.info("userNameAttributeName={}", userNameAttributeName);

        //social type에 따라 유저정보를 통해 OAuthAttributes 객체 생성
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
        //log.info("attributes={}", attributes);

        if (!StringUtils.hasText(attributes.getEmail())) {
            throw new RuntimeException("Email not found from OAuth2 provider");
        }

        Member member = saveOrUpdate(attributes);

        //같은 이메일로 이미 회원가입 한 경우 기존 회원 데이터를 유지한채 로그인처리
        if(member.getRoleValue().equals("ROLE_USER")) {
            return new UserDetailsDTO(member);
        }


        //DB에 refresh token 저장
        refreshTokenRepository.findByMemberId(member.getId()).ifPresentOrElse(
                r -> r.update(refreshToken), () -> refreshTokenRepository.save(new RefreshToken(member, refreshToken))
        );

        //회원 탈퇴시 사용(MemberController)
        httpSession.setAttribute(OAUTH2_MEMBER, new UserSessionDTO(member, accessToken, registrationId));

        //최종적으로 Spring Security 가 인증여부를 확인할 수 있도록 OAuth2User 객체를 반환한다
        return new UserDetailsDTO(member, oAuth2User.getAttributes());

    }

    private Member saveOrUpdate(OAuthAttributes attributes) {
        Optional<Member> member = memberRepository.findByEmail(attributes.getEmail());

        if(member.isPresent()) {
            return member.get().updateModifiedDate();

        } else {
            return memberRepository.save(attributes.toEntity());
        }

    }

}
