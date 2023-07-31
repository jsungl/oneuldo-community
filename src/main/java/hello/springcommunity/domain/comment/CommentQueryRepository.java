package hello.springcommunity.domain.comment;

import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.springcommunity.domain.member.QMember;
import hello.springcommunity.domain.post.QPost;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.*;

import static hello.springcommunity.domain.comment.CommentResponseDTO.*;
import static hello.springcommunity.domain.comment.QComment.*;

/**
 * Querydsl 사용하여 댓글 조회 - 대댓글까지 포함하여 가져오기 위해
 *
 */

@Repository
public class CommentQueryRepository {
    private final JPAQueryFactory query;

    public CommentQueryRepository(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    /**
     * 댓글 조회
     */
    public List<CommentResponseDTO> findByPostId(Long id) {

        //조회 결과인 commentList 는 정렬된 결과를 가지고 있다
        List<Comment> commentList = query.selectFrom(comment)
                .leftJoin(comment.parent).fetchJoin()
                .join(comment.member).fetchJoin() //n+1 문제를 해결하기위해 fetchJoin
                .where(comment.post.id.eq(id))
                .orderBy(comment.parent.id.asc().nullsFirst(), comment.regDate.asc()) //부모댓글 내림차순, 작성일자 내림차순 정렬
                .fetch();

        Map<Long, CommentResponseDTO> commentResponseDTOMap = new HashMap<>();
        List<CommentResponseDTO> commentResponseDTOList = new ArrayList<>();

        commentList.forEach(comment -> {
            CommentResponseDTO commentResponseDTO = entityToDto(comment);
            commentResponseDTOMap.put(commentResponseDTO.getId(), commentResponseDTO); //각 댓글 DTO(commentResponseDTO) 는 자신의 자식 댓글들을 리스트로 가진다

            if(comment.getParent() != null) {
                //대댓글이면 Map에 저장되어 있는 부모댓글 DTO(commentResponseDTO) 의 자식댓글 리스트에 넣어준다
                commentResponseDTOMap.get(comment.getParent().getId()).getChildren().add(commentResponseDTO);
            }else {
                //최상위 댓글이면 리스트에 넣어준다
                commentResponseDTOList.add(commentResponseDTO);
            }
        });

        return commentResponseDTOList;
    }

    /**
     * 전체 댓글 갯수 조회(count)
     */
    public Long getTotalCount(Long id) {
        Long count = query.select(Wildcard.count)
                .from(comment)
                .where(comment.post.id.eq(id))
                .fetchOne();

        return count;
    }


    /**
     * 댓글 삭제시 부모 댓글을 포함하여 댓글 조회
     */
    public Optional<Comment> findCommentByIdWithParent(Long commentId) {
        Comment selectedComment = query.selectFrom(comment)
                .leftJoin(comment.parent).fetchJoin()
                .where(comment.id.eq(commentId))
                .fetchOne();

        return Optional.ofNullable(selectedComment);
    }
}
