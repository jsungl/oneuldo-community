package hello.springcommunity.domain.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * 회원가입
     */
    public Long join(Member member) {
        validateDuplicateLoginId(member); //중복 아이디 검증
        Member savedMember = memberRepository.save(member);
        return savedMember.getId();
    }

    /**
     * 아이디 중복 검증
     */
    private void validateDuplicateLoginId(Member member) {
        Optional<Member> findMember = memberRepository.findByLoginId(member.getLoginId());
        if(!findMember.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }

//        memberRepository.findByLoginId(member.getLoginId()).ifPresent(m -> {
//            throw new IllegalStateException("이미 존재하는 아이디입니다");
//        });
    }

    /**
     * 전체 회원 조회
     */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    /**
     * 회원 1명 조회
     */
    public Optional<Member> findOne(Long id) {
        return memberRepository.findOne(id);
    }





}
