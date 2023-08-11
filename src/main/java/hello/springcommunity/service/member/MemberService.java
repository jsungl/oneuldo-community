package hello.springcommunity.service.member;

import hello.springcommunity.dao.member.MemberRepository;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.Role;
import hello.springcommunity.dto.member.MemberNicknameUpdateDTO;
import hello.springcommunity.dto.member.MemberPwdUpdateDTO;
import hello.springcommunity.dto.member.MemberResponseDTO;
import hello.springcommunity.dto.member.MemberSaveRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * 회원 1명 조회
     */
    public Optional<Member> findOne(Long memberId) {
        return memberRepository.findById(memberId);
    }

    public MemberResponseDTO getMemberByLoginId(String loginId) {
        Member member = memberRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재하지 않습니다. loginId=" + loginId));

        //Entity -> DTO
        MemberResponseDTO result = MemberResponseDTO.builder()
                                                    .member(member)
                                                    .build();

        return result;

    }

    /**
     * 회원 닉네임 수정
     */
    public void updateMemberNickname(MemberNicknameUpdateDTO memberNicknameUpdateDTO) {
        Member member = memberRepository.findByLoginId(memberNicknameUpdateDTO.getLoginId()).orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재하지 않습니다. loginId=" + memberNicknameUpdateDTO.getLoginId()));

        member.updateNickname(memberNicknameUpdateDTO.getNickname());
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
}
