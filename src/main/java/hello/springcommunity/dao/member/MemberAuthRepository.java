package hello.springcommunity.dao.member;

import hello.springcommunity.domain.member.MemberAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface MemberAuthRepository extends JpaRepository<MemberAuth, Long> {

    /**
     * MemberId 로 인증권한 조회
     */
    Optional<MemberAuth> findByMemberId(Long id);


    /**
     * 회원탈퇴시 인증권한 삭제
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from MemberAuth ma where ma.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
}
