package hello.springcommunity.config.oauth;

import hello.springcommunity.dao.member.RefreshTokenRepository;
import hello.springcommunity.dao.member.MemberRepository;
import hello.springcommunity.domain.member.RefreshToken;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.dto.member.MemberLeaveResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DisConnectOauth2UserService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;
    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    /**
     * 생성자 주입 - 의존객체를 생성자를 통해 주입받는 방식
     */
//    @Autowired
//    public DisConnectOauth2UserService(MemberRepository memberRepository, RefreshTokenRepository refreshTokenRepository) {
//        this.memberRepository = memberRepository;
//        this.refreshTokenRepository = refreshTokenRepository;
//    }


    /**
     * oauth2 회원 연결 끊기
     */
    public MemberLeaveResponseDTO disConnectUser(UserSessionDTO userSessionDTO, Long memberId) {

        String provider = userSessionDTO.getProvider();
        String accessToken = userSessionDTO.getAccessToken();
        RefreshToken token = refreshTokenRepository.findByMemberId(memberId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 토큰정보입니다."));
        String refreshToken = token.getValue();
        String result = "";

        if("naver".equals(provider)) {
            log.info("네이버 로그인 사용자입니다.");

            /**
             * 주의 하실 점은 토큰이 유효하지 않을 경우에도 결과가 'success'값으로 리턴되므로 토큰이 유효한지 먼저 검증한 다음, 유효한 토큰으로 갱신하여 연동해제 처리를 하시면 됩니다.
             * 연동해제를 확인하려면 delete token 이후, 기존 발급 refresh token을 이용하여 더이상 token refresh를 할 수 없을 경우 정상 연동해지가 되었다고 판단하시는 방법이 있습니다.
             */

            //1. accessToken 유효성 검사
            //accessToken 유효성 체크 API : https://openapi.naver.com/v1/nid/me
            ResponseEntity<Map> verifiedResult = verifyAccessToken(accessToken, provider);

            if(verifiedResult.getStatusCodeValue() != 200) {
                //2. accessToken 이 유효하지 않다면 DB에 저장되어 있는 refreshToken 으로 토큰 갱신 요청
                //accessToken 재발급 API : https://nid.naver.com/oauth2.0/token

                ResponseEntity<Map> response = refreshAccessToken(refreshToken, provider);

                //refreshToken 이 만료됐다면 재로그인
                if(response.getBody().containsKey("error")) {
                    return new MemberLeaveResponseDTO("갱신토큰이 만료되어 재로그인이 필요합니다.", false, true);
                }

                log.info("재발급한 accessToken={}", response.getBody().get("access_token"));
                log.info("재발급한 refreshToken={}", response.getBody().get("refresh_token"));

                accessToken = (String) response.getBody().get("access_token");
                refreshToken = (String) response.getBody().get("refresh_token");
            }

            //3. 유효한 accessToken 으로 로그인 연결 해제 요청
            ResponseEntity<Map> response = disconnectLogin(accessToken, provider);

            //4. refreshToken 으로 토큰을 갱신할 수 있는지 확인
            if(response.getStatusCodeValue() == 200) {
                //기존에 발급한 refresh token을 이용하여 더이상 token을 갱신 할 수 없을 경우 정상 연동해지가 되었다고 판단
                ResponseEntity<Map> isDeleted = refreshAccessToken(refreshToken, provider);
                /**
                 * 정상적으로 연동해제 되었다면 다음과 같은 응답을 받는다
                 * {
                 *   "error": "invalid_request",
                 *   "error_description": "invalid refresh_token"
                 * }
                 *
                 */
                if(isDeleted.getBody().containsKey("error")) {
                    result = "success";
                }

            }



        } else if("google".equals(provider)){
            log.info("구글 로그인 사용자입니다.");

            //1. access token 유효성 검사(기본적으로 access token은 1시간동안 유효하다)
            //https://oauth2.googleapis.com/tokeninfo?access_token=ACCESS_TOKEN
            ResponseEntity<Map> verifiedResult = verifyAccessToken(accessToken, provider);
            /**
             * <200,{azp=44708855183-rr9dvv9vetjiq2nqchdgv773s2ut0at6.apps.googleusercontent.com, aud=44708855183-rr9dvv9vetjiq2nqchdgv773s2ut0at6.apps.googleusercontent.com, sub=109324193206697560890 ....
             */

            if(verifiedResult.getStatusCodeValue() != 200) {
                //2. access token이 유효하지 않다면 refresh token을 통해 갱신요청
                //POST https://oauth2.googleapis.com/token?client_id=[your_client_id]&client_secret=[your_client_secret]&refresh_token=[refresh_token]&grant_type=[refresh_token]
                //refresh 토큰은 설정된 수명이 없음. 토큰이 만료될 수 있지만, 그렇지 않으면 계속 사용할 수 있다
                /**
                 * 정상적으로 access token을 갱신하면 다음과 같이 새로운 access token이 포함된 JSON 응답을 받는다
                 * {
                 *   "access_token": "1/fFAGRNJru1FTz70BzhT3Zg",
                 *   "expires_in": 3920,
                 *   "scope": "https://www.googleapis.com/auth/drive.metadata.readonly",
                 *   "token_type": "Bearer"
                 * }
                 */
                ResponseEntity<Map> response = refreshAccessToken(refreshToken, provider);
                log.info("access token 갱신 요청 응답={}", response.getBody());

                log.info("재발급한 accessToken={}", response.getBody().get("access_token"));
                accessToken = (String) response.getBody().get("access_token");
            }

            //3. 유효한 accessToken 으로 애플리케이션에 부여된 액세스 권한 취소
            //https://oauth2.googleapis.com/revoke?token=[token]
            ResponseEntity<Map> response = disconnectLogin(accessToken, provider);
            
            /**
             * 토큰은 액세스 토큰 또는 갱신 토큰일 수 있습니다. 토큰이 액세스 토큰이고 해당 갱신 토큰이 있으면 갱신 토큰도 취소됩니다.
             * 취소가 성공적으로 처리되면 응답의 HTTP 상태 코드는 200입니다. 오류 조건의 경우 HTTP 상태 코드 400가 오류 코드와 함께 반환됩니다.
             */
            int statusCode = response.getStatusCodeValue(); //200 or 400
            if(statusCode == 200) {
                //성공
                log.info("구글 로그인 연동해제 성공!");
                result = "success";
            } else {
                //실패
                log.info("구글 로그인 연동해제 실패!");
                log.info("구글 로그인 연동해제 요청 응답={}", response.getBody());
            }

        }


        if("success".equals(result)) {
            deleteMember(token);
            return new MemberLeaveResponseDTO("회원 탈퇴 처리 되었습니다. 지금까지 이용해주셔서 감사합니다.", true, false);
        } else {
            return new MemberLeaveResponseDTO("회원탈퇴에 실패하였습니다.", false, false);
        }

    }

    /**
     * accessToken 유효성 검사
     */
    private ResponseEntity<Map> verifyAccessToken(String accessToken, String provider) {

        if("naver".equals(provider)) {

            WebClient webClient = WebClient.builder()
                    .baseUrl("https://openapi.naver.com/v1/nid/me")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .build();

            ResponseEntity<Map> response = webClient
                    .get()
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatus::isError, clientResponse -> Mono.empty())
                    .toEntity(Map.class)
                    .block();

            return response;

        } else {

            WebClient webClient = WebClient.builder()
                    .baseUrl("https://oauth2.googleapis.com/tokeninfo")
                    .build();

            ResponseEntity<Map> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("access_token", accessToken)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatus::isError, clientResponse -> Mono.empty())
                    .toEntity(Map.class)
                    .block();

            return response;
        }

    }


    /**
     * 로그인 연동 해제 요청
     */
    private ResponseEntity<Map> disconnectLogin(String accessToken, String provider) {

        if("naver".equals(provider)) {

            WebClient webClient = WebClient.builder()
                    .baseUrl("https://nid.naver.com/oauth2.0/token")
                    .build();

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("grant_type", "delete");
            bodyMap.put("client_id", naverClientId);
            bodyMap.put("client_secret", naverClientSecret);
            bodyMap.put("access_token", accessToken);
            bodyMap.put("service_provider", "NAVER");

            ResponseEntity<Map> response = webClient
                    .post()
                    .bodyValue(bodyMap)
                    .retrieve()
                    .toEntity(Map.class)
                    .block();

            return response;
            
        } else {
            
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://oauth2.googleapis.com/revoke")
                    .build();

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("token", accessToken);

            /**
             * 상태코드가 200이 아니면 따로 예외를 처리해야 하지만 밑에서 처리하기 위해 정상응답으로 바꿔서 반환(Mono.empty() 를 반환)
             */
            ResponseEntity<Map> response = webClient
                    .post()
                    .bodyValue(bodyMap)
                    .retrieve()
                    .onStatus(HttpStatus::isError, clientResponse -> Mono.empty())
                    .toEntity(Map.class)
                    .block();

            return response;
        }
        
    }


    /**
     * accessToken 갱신 요청
     */
    private ResponseEntity<Map> refreshAccessToken(String refreshToken, String provider) {

        if("naver".equals(provider)) {

            WebClient webClient = WebClient.builder()
                    .baseUrl("https://nid.naver.com/oauth2.0/token")
                    .build();

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("grant_type", "refresh_token");
            bodyMap.put("client_id", naverClientId);
            bodyMap.put("client_secret", naverClientSecret);
            bodyMap.put("refresh_token", refreshToken);

            ResponseEntity<Map> response = webClient
                    .post()
                    .bodyValue(bodyMap)
                    .retrieve()
                    .toEntity(Map.class)
                    .block();

            return response;

        } else {

            WebClient webClient = WebClient.builder()
                    .baseUrl("https://oauth2.googleapis.com/token")
                    .build();

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("grant_type", "refresh_token");
            bodyMap.put("client_id", googleClientId);
            bodyMap.put("client_secret", googleClientSecret);
            bodyMap.put("refresh_token", refreshToken);

            ResponseEntity<Map> response = webClient
                    .post()
                    .bodyValue(bodyMap)
                    .retrieve()
                    .toEntity(Map.class)
                    .block();

            return response;
        }

    }

    /**
     * 회원 정보 비활성화, refresh 토큰 삭제
     */
    private void deleteMember(RefreshToken token) {

        //refresh token 삭제
        Member member = token.getMember();
        refreshTokenRepository.delete(token);

        //계정 비활성화
        memberRepository.changeActivated(member.getId());
    }

}
