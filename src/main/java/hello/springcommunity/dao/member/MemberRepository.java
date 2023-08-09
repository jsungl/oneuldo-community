package hello.springcommunity.dao.member;

import hello.springcommunity.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 중복 체크는 Bean validation (validation 어노테이션)으로 해결할 수 없기 때문에 따로 로직을 만들어주어야 한다
 * 별도의 Validator 사용 - 검증 로직 별도 분리
 */

public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 유효성 검사 - 중복체크
     * 중복이면 true
     * 중복이 아니면 false
     */
    boolean existsByLoginId(String loginId);
    boolean existsByNickname(String nickname);

    /**
     * 로그인 아이디로 회원 조회
     */
    Optional<Member> findByLoginId(String loginId);
}
