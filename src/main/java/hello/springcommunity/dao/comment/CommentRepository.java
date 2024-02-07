package hello.springcommunity.dao.comment;

import hello.springcommunity.domain.comment.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 단순 조회와 기본적인 CRUD 기능은 스프링 데이터 JPA 로 처리한다
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 게시물 번호로 댓글 조회
     */
    @EntityGraph(attributePaths = {"post", "member", "children"})
    @Query("select c from Comment c where c.post.id = :postId order by c.groupId,c.step,c.parent.id NULLS FIRST")
    List<Comment> findByPostId(@Param("postId") Long postId);

    /**
     * Id 로 댓글 조회
     */
    @EntityGraph(attributePaths = {"children"})
    @Query("select c from Comment c where c.id = :commentId")
    Optional<Comment> findByCommentId(@Param("commentId") Long commentId);

    /**
     * 부모 댓글 id와 groupId가 인자와 일치하는 댓글들 조회
     */
    List<Comment> findByParentIdAndGroupId(Long id, Long groupId);

    /**
     * 특정 회원이 작성한 댓글 모두 조회
     */
//    @EntityGraph(attributePaths = {"post", "member", "children"})
//    Page<Comment> findByMemberId(Long id, Pageable pageable);

    /**
     * 그룹 내 총 대댓글 수(최상위 부모 댓글 제외)
     */
    @Query("select count(*) from Comment c where c.parent is not null and c.groupId = :groupId")
    Long getChildCount(@Param("groupId") Long groupId);


    /**
     * 그룹 내 depth 최대값
     */
    @Query("select Max(c.depth) from Comment c where c.groupId = :groupId and c.post.id = :postId")
    Integer getMaxDepth(@Param("groupId") Long groupId, @Param("postId") Long postId);

    /**
     * 그룹번호 최대값
     */
    @Query("select Max(c.groupId) from Comment c where c.post.id = :postId")
    Long getGroupId(@Param("postId") Long id);

    /**
     * @Query 어노테이션을 이용해 update, delete 쿼리를 하려면
     * @Modifying @Transactional 어노테이션 두 개를 붙혀줘야 한다
     * clearAutomatically = true : 업데이트 쿼리를 날린 후에 영속성 컨텍스트를 자동으로 clear() 해준다
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update Comment c set c.step = c.step + 1 where c.groupId = :groupId and c.step > :step")
    void plusCommentStep(@Param("groupId")Long commentGroupId, @Param("step")Integer step);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update Comment c set c.step = c.step - 1 where c.groupId = :groupId and c.step > :step")
    void minusCommentStep(@Param("groupId")Long commentGroupId, @Param("step")Integer commentStep);


    /**
     * 댓글삭제
     * 인자로 전달된 postId에 해당하는 댓글 모두 삭제
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from Comment c where c.post.id = :postId")
    void deleteAllByPostId(@Param("postId") Long id);

    /**
     * 게시물 삭제시 댓글을 한번에 삭제하기 위해
     * 먼저 연관관계 제거 후 삭제
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update Comment c set c.parent = NULL where c.post.id = :postId")
    void updateParentByPostId(@Param("postId") Long id);


}
