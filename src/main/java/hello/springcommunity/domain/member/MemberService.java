package hello.springcommunity.domain.member;

import hello.springcommunity.web.member.form.MemberSaveForm;
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
     * 간단한 회원가입이기 때문에 크게 상관은 없지만 나중에 Entity에 중요 정보가 있다면 해당 내용은 외부에 노출되지 않게끔
     * DTO(MemberSaveForm)를 사용하여 정보를 전달받고 Builder를 통해 Entity 화
     */
    public Long join(MemberSaveForm memberSaveForm) {
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
    public Optional<Member> findOne(Long id) {
        return memberRepository.findById(id);
    }
}
