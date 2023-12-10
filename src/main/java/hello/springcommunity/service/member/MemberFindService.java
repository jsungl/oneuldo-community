package hello.springcommunity.service.member;

import hello.springcommunity.dao.member.MemberRepository;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.dto.member.MemberResponseDTO;
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

import static hello.springcommunity.common.mail.MailUtil.createAuthKey;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberFindService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    /**
     * 이메일로 회원 조회
     */
    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));
    }

    public MemberResponseDTO getMemberDtoByEmail(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));

        return MemberResponseDTO.builder().member(member).build();
    }

    /**
     * 비밀번호 찾기
     */
    public void sendPassword(Member member, String to) {

        try {
            //임시 비밀번호 생성
            String password = createAuthKey();
    
            //임시 비밀번호를 암호화 후 DB 세팅
            member.updatePassword(passwordEncoder.encode(password));

            //임시비밀번호 전송
            sendMail(to, password);

        } catch (MessagingException | MailException e) {
            throw new RuntimeException(e);
        }

    }


    private String setContext(String password) {
        Context context = new Context();
        context.setVariable("data", password);
        return templateEngine.process("mail/sendPassword", context);
    }


    /**
     * 임시 비밀번호 메일 전송
     */
    private void sendMail(String to, String password) throws MessagingException,MailException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject("비밀번호 찾기 결과 메일입니다.");
        mimeMessageHelper.setText(setContext(password), true); //메일 본문 내용, HTML 여부
        javaMailSender.send(mimeMessage);
    }
}
