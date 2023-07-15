package hello.springcommunity.domain.member;

import hello.springcommunity.web.member.form.MemberNameUpdateForm;
import hello.springcommunity.web.member.form.MemberPwdUpdateForm;
import hello.springcommunity.web.member.form.MemberResponseDTO;
import hello.springcommunity.web.member.form.MemberSaveForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     * 간단한 회원가입이기 때문에 크게 상관은 없지만 나중에 Entity에 중요 정보가 있다면 해당 내용은 외부에 노출되지 않게끔
     * DTO(MemberSaveForm)를 사용하여 정보를 전달받고 Builder를 통해 Entity 화
     */
    public Long join(MemberSaveForm memberSaveForm) {

        //비밀번호 암호화
        memberSaveForm.setPassword(passwordEncoder.encode(memberSaveForm.getPassword()));

        Member member = Member.builder()
                .loginId(memberSaveForm.getLoginId())
                .password(memberSaveForm.getPassword())
                .name(memberSaveForm.getName())
                .build();

        return memberRepository.save(member).getId();
    }

    /**
     * 전체 회원 조회
     */
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    /**
     * 회원 1명 조회
     */
    public Optional<Member> findOne(Long memberId) {
        return memberRepository.findById(memberId);
    }
//    public MemberResponseDTO findMember(String loginId) {
//        Member member = memberRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("아이디가 존재하지 않습니다"));
//
//        MemberResponseDTO result = MemberResponseDTO.builder()
//                .member(member)
//                .build();
//
//        return result;
//
//    }

    public MemberResponseDTO findMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new UsernameNotFoundException("아이디가 존재하지 않습니다"));

        MemberResponseDTO result = MemberResponseDTO.builder()
                                                    .member(member)
                                                    .build();

        return result;

    }

    public Long updateMemberName(MemberNameUpdateForm memberNameUpdateForm) {
        Member member = memberRepository.findByLoginId(memberNameUpdateForm.getLoginId()).orElseThrow(() -> new UsernameNotFoundException("아이디가 존재하지 않습니다"));

        member.updateName(memberNameUpdateForm.getName());
        memberRepository.save(member);

        return member.getId();
    }

    public Long updateMemberPassword(MemberPwdUpdateForm memberPwdUpdateForm, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new UsernameNotFoundException("아이디가 존재하지 않습니다"));

        //입력한 기존 비밀번호가 맞는지 확인
        if(!passwordEncoder.matches(memberPwdUpdateForm.getCurrentPassword(), member.getPassword())) {
            return null;
        }else {
            //입력한 새로운 비밀번호를 암호화 후 저장
            memberPwdUpdateForm.setNewPassword(passwordEncoder.encode(memberPwdUpdateForm.getNewPassword()));
            member.updatePassword(memberPwdUpdateForm.getNewPassword());
            memberRepository.save(member);
        }

        return member.getId();
    }
}
