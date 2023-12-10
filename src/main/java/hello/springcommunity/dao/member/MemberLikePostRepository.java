package hello.springcommunity.dao.member;

import hello.springcommunity.domain.member.MemberLikePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MemberLikePostRepository extends JpaRepository<MemberLikePost, Long> {

    /**
     * 유저가 게시물을 추천했는지 확인
     */
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    /**
     * 게시물 추천 취소
     */
    void deleteByPostIdAndMemberId(Long postId, Long memberId);

    /**
     * 게시물 추천 취소(다수)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from MemberLikePost m where m.post.id = :postId")
    void deleteAllByPostId(@Param("postId") Long id);
}
