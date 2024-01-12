package hello.springcommunity.dao.member;

import hello.springcommunity.domain.member.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 중복 체크는 Bean validation (validation 어노테이션)으로 해결할 수 없기 때문에 따로 로직을 만들어주어야 한다
 * 별도의 Validator 사용 - 검증 로직 별도 분리
 */

public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 유효성 검사 - 아이디, 닉네임, 이메일 중복체크
     * 중복이면 true
     * 중복이 아니면 false
     */
//    boolean existsByLoginId(String loginId);
//    boolean existsByNickname(String nickname);
//    boolean existsByEmail(String mail);

    @Query("select count(m.id) > 0 from Member m where m.loginId = :loginId and m.activated = TRUE")
    boolean existsByLoginIdAndActivated(@Param("loginId") String loginId);

    @Query("select count(m.id) > 0 from Member m where m.nickname = :nickname and m.activated = TRUE")
    boolean existsByNicknameAndActivated(@Param("nickname") String nickname);

    @Query("select count(m.id) > 0 from Member m where m.email = :email and m.activated = TRUE")
    boolean existsByEmailAndActivated(@Param("email") String email);

    /**
     * 로그인 아이디로 회원 조회
     */
    @EntityGraph(attributePaths = {"likePosts"})
    Optional<Member> findByLoginId(String loginId);


    /**
     * 이메일로 회원 조회
     */
    Optional<Member> findByEmail(String email);


    /**
     * 회원탈퇴 처리 - 계정 비활성화
     * activated = false, loginId = null, password = null
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update Member m set m.activated = FALSE, m.loginId = NULL, m.password = NULL where m.id = :memberId")
    void changeActivated(@Param("memberId") Long memberId);

}
