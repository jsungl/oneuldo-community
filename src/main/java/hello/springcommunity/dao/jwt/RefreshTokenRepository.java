package hello.springcommunity.dao.jwt;

import hello.springcommunity.domain.jwt.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    //refresh token 조회
    @EntityGraph(attributePaths = {"member"})
    Optional<RefreshToken> findByMemberId(Long memberId);

}
