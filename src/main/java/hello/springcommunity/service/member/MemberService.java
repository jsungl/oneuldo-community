package hello.springcommunity.service.member;

import hello.springcommunity.dao.member.MemberAuthRepository;
import hello.springcommunity.dao.member.MemberQueryRepository;
import hello.springcommunity.dao.member.MemberRepository;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberAuth;
import hello.springcommunity.domain.member.Role;
import hello.springcommunity.dto.member.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.stream.Collectors;

import static hello.springcommunity.common.mail.MailUtil.createAuthKey;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberQueryRepository memberQueryRepository;
    private final MemberAuthRepository memberAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;


    /**
     * 회원가입
     * 간단한 회원가입이기 때문에 크게 상관은 없지만 나중에 Entity에 중요 정보가 있다면 해당 내용은 외부에 노출되지 않게끔
     * DTO(MemberSaveForm)를 사용하여 정보를 전달받고 Builder를 통해 Entity 화
     */
    public void join(MemberSaveRequestDTO memberSaveRequestDTO, String path) {

        try {
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

            //.role(memberSaveRequestDTO.getLoginId().contains("admin") ? Role.ADMIN : Role.USER)

            //기본정보 저장
            Member savedMember = memberRepository.save(member);
            MemberResponseDTO dto = MemberResponseDTO.builder().member(savedMember).build();

            //인증 메일 발송
            String authKey = sendConfirmMail(dto, path);

            /**
             * 회원가입시 인증정보 저장
             *
             * 인증상태 false
             * authkey 저장
             */
            MemberAuth memberAuth = MemberAuth.builder()
                    .authKey(authKey)
                    .member(savedMember)
                    .isAuthenticated(false)
                    .build();

            memberAuthRepository.save(memberAuth);

        } catch (MessagingException | MailException e) {
            throw new RuntimeException(e);
        }



    }


    /**
     * 관리자 페이지
     * 전체 회원 조회
     */
    public Page<MemberResponseDTO> getAllMember(Pageable pageable) {

        Page<Member> members = memberQueryRepository.findAll(pageable);

        //Entity -> DTO
        List<MemberResponseDTO> list = members.stream()
                .map(member -> MemberResponseDTO.builder()
                        .member(member)
                        .build())
                .collect(Collectors.toList());

        return new PageImpl<>(list, pageable, members.getTotalElements());
    }


    /**
     * 회원 1명 조회 - Id
     */
    public MemberResponseDTO getMemberById(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));

        //Entity -> DTO
        return MemberResponseDTO.builder().member(member).build();
    }


    /**
     * 회원 1명 조회 - loginId
     */
    public Member getMemberByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));
    }

    public MemberResponseDTO getMemberDtoByLoginId(String loginId) {
        Member member = memberRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));
        return MemberResponseDTO.builder().member(member).build();
    }


    /**
     * 회원 탈퇴
     * 계정 비활성화
     * 작성한 게시물 블라인드 처리 
     * 댓글은 남겨둔다
     */
    public Boolean deleteMember(Member member, String currentPassword) {
        boolean result = false;

        //입력한 비밀번호가 맞는지 확인
        if(passwordEncoder.matches(currentPassword, member.getPassword())) {
            //인증권한 삭제
            memberAuthRepository.deleteByMemberId(member.getId());
            //계정 비활성화
            memberRepository.changeActivated(member.getId());
            result = true;
        }

        return result;
    }


    /**
     * 회원가입, 회원정보(이메일) 변경시 이메일 인증
     * to : 메일 수신자
     * subject : 메일 제목
     * type : 메일 타입
     */
    public String sendConfirmMail(MemberResponseDTO member, String path) throws MessagingException, MailException {

        String authKey = createAuthKey();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        Map<String, Object> map = new HashMap<>();
        map.put("path", path);
        map.put("memberId", member.getId());
        map.put("authKey", authKey);
        map.put("subject", "오늘도 커뮤 회원가입 인증 메일입니다.");

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        mimeMessageHelper.setTo(member.getEmail());
        mimeMessageHelper.setSubject("오늘도 커뮤 회원가입 인증 메일입니다.");
        mimeMessageHelper.setText(setContext(map), true); //메일 본문 내용, HTML 여부
        javaMailSender.send(mimeMessage);

        return authKey;

    }


    /**
     * thymeleaf를 통한 html 적용 - 회원가입, 이메일 수정
     */
    private String setContext(Map<String, Object> map) {
        Context context = new Context();
        context.setVariable("path", map.get("path"));
        context.setVariable("memberId", map.get("memberId"));
        context.setVariable("authKey", map.get("authKey"));
        context.setVariable("subject", map.get("subject"));
        return templateEngine.process("mail/confirmMail", context);
    }

}
