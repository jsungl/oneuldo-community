package hello.springcommunity.dao.member;

import hello.springcommunity.domain.member.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    //memberId로 refresh token 조회
    @EntityGraph(attributePaths = {"member"})
    Optional<RefreshToken> findByMemberId(Long memberId);

}
