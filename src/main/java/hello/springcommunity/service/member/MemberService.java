package hello.springcommunity.service.member;

import hello.springcommunity.config.oauth.UserSessionDTO;
import hello.springcommunity.dao.member.MemberRepository;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.Role;
import hello.springcommunity.dto.member.MemberNicknameUpdateDTO;
import hello.springcommunity.dto.member.MemberPwdUpdateDTO;
import hello.springcommunity.dto.member.MemberResponseDTO;
import hello.springcommunity.dto.member.MemberSaveRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import reactor.core.publisher.Mono;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;
    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;

//    WebClient webClient = WebClient.builder()
//            .baseUrl("https://nid.naver.com/oauth2.0/token")
//            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//            .build();

    /**
     * 회원가입
     * 간단한 회원가입이기 때문에 크게 상관은 없지만 나중에 Entity에 중요 정보가 있다면 해당 내용은 외부에 노출되지 않게끔
     * DTO(MemberSaveForm)를 사용하여 정보를 전달받고 Builder를 통해 Entity 화
     */
    public Long join(MemberSaveRequestDTO memberSaveRequestDTO) {

        //비밀번호 암호화
        memberSaveRequestDTO.setPassword(passwordEncoder.encode(memberSaveRequestDTO.getPassword()));

        //DTO -> Entity
        Member member = Member.builder()
                .loginId(memberSaveRequestDTO.getLoginId())
                .password(memberSaveRequestDTO.getPassword())
                .nickname(memberSaveRequestDTO.getNickname())
                .email(memberSaveRequestDTO.getEmail())
                .role(Role.USER)
                .build();

        return memberRepository.save(member).getId();
    }

    /**
     * 전체 회원 조회
     */
    public List<MemberResponseDTO> findAll() {
        List<Member> members = memberRepository.findAll();

        //Entity -> DTO
        return members.stream()
                .map(member -> MemberResponseDTO.builder()
                        .member(member)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 회원 1명 조회 - id, 로그인 아이디, 이메일
     */
    public Optional<Member> findOne(Long memberId) {
        return memberRepository.findById(memberId);
    }

    public MemberResponseDTO getMemberByLoginId(String loginId) {
        Member member = memberRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재하지 않습니다. loginId=" + loginId));

        //Entity -> DTO
        MemberResponseDTO dto = MemberResponseDTO.builder()
                                                    .member(member)
                                                    .build();

        return dto;

    }

    public MemberResponseDTO getMemberByEmail(String email) {

        //Member member = memberRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재하지 않습니다. email=" + email));

        Member member = memberRepository.findByEmail(email).orElse(null);


        //Entity -> DTO
        if(member != null) {

            MemberResponseDTO dto = MemberResponseDTO.builder()
                    .member(member)
                    .build();

            return dto;

        }

        return null;

    }

    /**
     * 회원 닉네임 수정
     */
    public void updateMemberNickname(MemberNicknameUpdateDTO memberNicknameUpdateDTO) {
        Member member = memberRepository.findByLoginId(memberNicknameUpdateDTO.getLoginId()).orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재하지 않습니다. loginId=" + memberNicknameUpdateDTO.getLoginId()));

        member.updateNickname(memberNicknameUpdateDTO.getNickname(), memberNicknameUpdateDTO.getEmail());
        //memberRepository.save(member);
    }

    /**
     * 회원 비밀번호 수정
     */
    public Boolean updateMemberPassword(MemberPwdUpdateDTO memberPwdUpdateDTO, String loginId) {
        //Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다. id=" + memberId));
        Member member = memberRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재하지 않습니다. loginId=" + loginId));

        //입력한 기존 비밀번호가 맞는지 확인
        if(!passwordEncoder.matches(memberPwdUpdateDTO.getCurrentPassword(), member.getPassword())) {
            return false;
        }else {
            //입력한 새로운 비밀번호를 암호화 후 저장
            memberPwdUpdateDTO.setNewPassword(passwordEncoder.encode(memberPwdUpdateDTO.getNewPassword()));
            member.updatePassword(memberPwdUpdateDTO.getNewPassword());
            //memberRepository.save(member);
        }

        return true;
    }

    /**
     * 회원 탈퇴
     */
    public Boolean withdrawal(String loginId, String currentPassword) {
        //Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다. id=" + memberId));
        Member member = memberRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재하지 않습니다. loginId=" + loginId));

        //입력한 기존 비밀번호가 맞는지 확인
        if(passwordEncoder.matches(currentPassword, member.getPassword())) {
            memberRepository.delete(member);
            return true;
        }else {
            return false;
        }
    }

    /**
     * oauth2 회원 연결 끊기
     */
    public Boolean disConnectOauth2User(UserSessionDTO userSessionDTO, String loginId, String provider) {
        //log.info("accessToken={}", accessToken);
        String accessToken = userSessionDTO.getAccessToken();
        String refreshToken = userSessionDTO.getRefreshToken();
        String result = "";

        if("naver".equals(provider)) {

            log.info("naver clinetId={}", naverClientId);
            log.info("naver clientSecret={}", naverClientSecret);

            /**
             * 주의 하실 점은 토큰이 유효하지 않을 경우에도 결과가 'success'값으로 리턴되므로 토큰이 유효한지 먼저 검증한 다음, 유효한 토큰으로 갱신하여 연동해제 처리를 하시면 됩니다.
             * 연동해제를 확인하려면 delete token 이후, 기존 발급 refresh token을 이용하여 더이상 token refresh를 할 수 없을 경우 정상 연동해지가 되었다고 판단하시는 방법이 있습니다.
             */

            //1. accessToken 유효성 검사
            //accessToken 유효성 체크 API : https://openapi.naver.com/v1/nid/me
            ResponseEntity<Map> response1 = verifyAccessToken(accessToken);

            if(response1.getStatusCodeValue() != 200) {
                //2. accessToken 이 유효하지 않다면 refreshToken 으로 토큰 갱신
                //accessToken 재발급 API : https://nid.naver.com/oauth2.0/token

                ResponseEntity<Map> response2 = refreshAccessToken(naverClientId, naverClientSecret, refreshToken);

                //refreshToken 이 만료됬다면 재로그인
                if(response2.getBody().containsKey("error")) {
                    log.info("refreshToken 이 만료! 재로그인 필요!");
                    return false;
                }

                log.info("재발급한 accessToken={}", response2.getBody().get("access_token"));
                log.info("재발급한 refreshToken={}", response2.getBody().get("refresh_token"));

                accessToken = (String) response2.getBody().get("access_token");
                refreshToken = (String) response2.getBody().get("refresh_token");
            }

            //3. 유효한 accessToken 으로 로그인 연결 해제 요청
            ResponseEntity<Map> response3 = unlinkLogin(naverClientId, naverClientSecret, accessToken);


            //4. 기존 refreshToken 으로 토큰을 갱신할 수 있는지 확인
            //refresh 할 수 없다면 정상적으로 연동해제
            if(response3.getStatusCodeValue() == 200) {
                //4. 기존 발급 refresh token을 이용하여 더이상 token refresh를 할 수 없을 경우 정상 연동해지가 되었다고 판단
                ResponseEntity<Map> isDeleted = refreshAccessToken(naverClientId, naverClientSecret, refreshToken);

                /*{
                    "error": "invalid_request",
                    "error_description": "invalid refresh_token"
                }*/
                log.info("갱신결과={}", isDeleted.getBody());

                if(isDeleted.getBody().containsKey("error")) {
                    result = "success";
                }

            }



        }else {

            //access token 유효성 검증
            //https://oauth2.googleapis.com/tokeninfo?access_token=ACCESS_TOKEN


            //access token 갱신 요청
            //POST https://oauth2.googleapis.com/token
            //Content-Type: application/x-www-form-urlencoded
            /*
            client_id=your_client_id&
            client_secret=your_client_secret&
            refresh_token=refresh_token&
            grant_type=refresh_token
            */


            //https://oauth2.googleapis.com/revoke?token=
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("token", "accessToken");

            WebClient webClient = WebClient.builder()
                    .baseUrl("https://oauth2.googleapis.com/revoke")
                    .build();

            /**
             * 상태코드가 200이 아니면 따로 예외를 처리해야 하지만 밑에서 처리하기 위해 정상응답으로 바꿔서 반환(Mono.empty() 를 반환)
             */
            ResponseEntity<Map> response = webClient
                    .post()
                    .bodyValue(bodyMap)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .retrieve()
                    .onStatus(HttpStatus::isError, clientResponse -> Mono.empty())
                    .toEntity(Map.class)
                    .block();

            /**
             * 토큰은 액세스 토큰 또는 갱신 토큰일 수 있습니다. 토큰이 액세스 토큰이고 해당 갱신 토큰이 있으면 갱신 토큰도 취소됩니다.
             * 취소가 성공적으로 처리되면 응답의 HTTP 상태 코드는 200입니다. 오류 조건의 경우 HTTP 상태 코드 400가 오류 코드와 함께 반환됩니다.
             */
            int statusCode = response.getStatusCodeValue(); //200 or 400
            if(statusCode == 200) {
                //성공
                log.info("구글 로그인 연동해제 성공!");
                result = "success";
            }else {
                //실패
                log.info("구글 로그인 연동해제 실패!");
                log.info("response body={}", response.getBody());

            }

        }


        //DB에서 회원정보 삭제
        if("success".equals(result)) {
            Member member = memberRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재하지 않습니다. loginId=" + loginId));
            memberRepository.delete(member);
            return true;
        }else {
            return false;
        }

    }

    /**
     * accessToken 유효성 검사
     */
    private ResponseEntity<Map> verifyAccessToken(String accessToken) {

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
    }

    /**
     * 네이버 로그인 연동 해제 요청
     */
    private ResponseEntity<Map> unlinkLogin(String naverClientId, String naverClientSecret, String accessToken) {

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
    }


    /**
     * accessToken 갱신 요청
     */
    private ResponseEntity<Map> refreshAccessToken(String naverClientId, String naverClientSecret, String refreshToken) {

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
    }

    /**
     * 아이디/비밀번호 찾기 - 아이디, 임시비밀번호 전송
     */
    public void sendIdPassword(MemberResponseDTO member, String to, String type) {

        String data = "";
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();


        if(type.equals("mail/loginId")) {

            data = member.getLoginId();

        } else if (type.equals("mail/password")) {
            data = createCode();
            //임시 비밀번호로 DB 세팅
            //입력한 새로운 비밀번호를 암호화 후 저장
            Member findMember = memberRepository.findByLoginId(member.getLoginId()).orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재하지 않습니다. loginId=" + member.getLoginId()));
            findMember.updatePassword(passwordEncoder.encode(data));
        }

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject("아이디/비밀번호 정보입니다.");
            mimeMessageHelper.setText(setContext(data, type), true); //메일 본문 내용, HTML 여부
            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            log.error("아이디/비밀번호 찾기 실패!", e);
            throw new RuntimeException(e);
        }

    }

    /**
     * 회원가입 이메일 인증
     * to : 메일 수신자
     * subject : 메일 제목
     * type : 메일 타입
     */
    public String mailConfirm(String to, String subject, String type) {

        String checkcode = createCode();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(setContext(checkcode, type), true); //메일 본문 내용, HTML 여부
            javaMailSender.send(mimeMessage);

            return checkcode;
        } catch (MessagingException e) {
            log.error("회원가입 이메일 인증 실패!", e);
            throw new RuntimeException(e);
        }


    }

    /**
     * thymeleaf를 통한 html 적용
     */
    private String setContext(String code, String type) {
        Context context = new Context();
        context.setVariable("code", code);
        return templateEngine.process(type, context);
    }

    /**
     * 인증번호 및 임시 비밀번호 생성 메서드
     */
    private String createCode() {
        Random random = new Random();
        StringBuffer key = new StringBuffer();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(4);

            switch (index) {
                case 0: key.append((char) ((int) random.nextInt(26) + 97)); break;
                case 1: key.append((char) ((int) random.nextInt(26) + 65)); break;
                default: key.append(random.nextInt(9));
            }
        }
        return key.toString();
    }
}
