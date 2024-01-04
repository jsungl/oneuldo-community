package hello.springcommunity.service.member;


import hello.springcommunity.dao.member.MemberAuthRepository;
import hello.springcommunity.dao.member.MemberRepository;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberAuth;
import hello.springcommunity.dto.member.MemberProfileUpdateDTO;
import hello.springcommunity.dto.member.MemberPwdUpdateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static hello.springcommunity.common.mail.MailUtil.createAuthKey;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberUpdateService {

    private final MemberRepository memberRepository;
    private final MemberAuthRepository memberAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    /**
     * 회원 닉네임 수정
     */
    public Member updateMemberProfile(MemberProfileUpdateDTO updateDTO, Member member, String path) {

        try {
            //기존 이메일
            String oldEmail = member.getEmail();

            //인증키 생성
            String authKey = createAuthKey();

            //변경된 회원정보 저장
            member.updateProfile(updateDTO.getNickname(), updateDTO.getEmail());
            Member updatedMember = memberRepository.save(member);

            //이메일 변경시 인증키 저장후 인증메일 전송
            if(!oldEmail.equals(updatedMember.getEmail())) {
                MemberAuth memberAuth = memberAuthRepository.findByMemberId(updatedMember.getId()).orElseThrow(() -> new NoSuchElementException("존재하지 않는 인증정보입니다."));
                memberAuth.updateAuth(false, authKey);
                sendMail(updatedMember, authKey, path);
            }

            return updatedMember;

        } catch (MessagingException | MailException e) {
            throw new RuntimeException(e);
        }


    }


    /**
     * 회원 비밀번호 수정
     */
    public Member updateMemberPassword(MemberPwdUpdateDTO memberPwdUpdateDTO, String loginId) {
        Member member = memberRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));

        //입력한 기존 비밀번호가 맞는지 확인
        if(!passwordEncoder.matches(memberPwdUpdateDTO.getCurrentPassword(), member.getPassword())) {
            return null;
        }

        //입력한 새로운 비밀번호를 암호화 후 저장
        memberPwdUpdateDTO.setNewPassword(passwordEncoder.encode(memberPwdUpdateDTO.getNewPassword()));
        member.updatePassword(memberPwdUpdateDTO.getNewPassword());

        return memberRepository.save(member);
    }


    /**
     * 인증메일 발송
     */
    public void sendMail(Member member, String authKey, String path) throws MessagingException, MailException {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        Map<String, Object> map = new HashMap<>();
        map.put("path", path);
        map.put("memberId", member.getId());
        map.put("authKey", authKey);
        map.put("subject", "오늘도 커뮤 회원정보 변경 인증 메일입니다.");

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        mimeMessageHelper.setTo(member.getEmail());
        mimeMessageHelper.setSubject("오늘도 커뮤 회원정보 변경 인증 메일입니다.");
        mimeMessageHelper.setText(setContext(map), true); //메일 본문 내용, HTML 여부
        javaMailSender.send(mimeMessage);

    }


    private String setContext(Map<String, Object> map) {
        Context context = new Context();
        context.setVariable("path", map.get("path"));
        context.setVariable("memberId", map.get("memberId"));
        context.setVariable("authKey", map.get("authKey"));
        context.setVariable("subject", map.get("subject"));
        return templateEngine.process("mail/confirmMail", context);
    }
}
