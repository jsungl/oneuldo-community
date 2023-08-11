package hello.springcommunity.dao.comment;

import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.springcommunity.domain.comment.Comment;
import hello.springcommunity.dto.comment.CommentResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.*;

import static hello.springcommunity.dto.comment.CommentResponseDTO.*;
import static hello.springcommunity.domain.comment.QComment.*;

/**
 * Querydsl 사용하여 댓글 조회 - 대댓글까지 포함하여 가져오기 위해
 *
 */

@Slf4j
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
                .orderBy(comment.parent.id.asc().nullsFirst(), comment.createdDate.asc()) //부모댓글 내림차순, 작성일자 내림차순 정렬
                .fetch();

        Map<Long, CommentResponseDTO> commentResponseDTOMap = new HashMap<>();
        //최상위 댓글만 저장
        List<CommentResponseDTO> commentResponseDTOList = new ArrayList<>();


        commentList.forEach(comment -> {
            CommentResponseDTO commentResponseDTO = entityToDto(comment);
            log.info("commentResponseDTO id={} content={} children={}", commentResponseDTO.getId(), commentResponseDTO.getContent(), commentResponseDTO.getChildren());

            commentResponseDTOMap.put(commentResponseDTO.getId(), commentResponseDTO); //각 댓글 DTO(commentResponseDTO) 는 자신의 자식 댓글들을 리스트로 가진다

            if(comment.getParent() != null) {
                //대댓글이면 Map에 저장되어 있는 해당 댓글 DTO(commentResponseDTO) 의 자식댓글 리스트에 넣어준다
                commentResponseDTOMap.get(comment.getParent().getId()).getChildren().add(commentResponseDTO);
            }else {
                //최상위 댓글이면 리스트에 넣어준다
                commentResponseDTOList.add(commentResponseDTO);
            }

        });


        return commentResponseDTOList;
    }

    /**
     * 댓글 조회 - 페이징
     */
    public Page<CommentResponseDTO> findCommentsByPostIdV2(Long id, Pageable pageable) {

        //조회 결과인 commentList 는 정렬된 결과를 가지고 있다
        List<Comment> commentList = query.selectFrom(comment)
                .leftJoin(comment.parent).fetchJoin()
                .join(comment.member).fetchJoin() //n+1 문제를 해결하기위해 fetchJoin
                .where(comment.post.id.eq(id))
                .orderBy(comment.groupId.asc(), comment.step.asc(), comment.parent.id.asc().nullsFirst()) //group id, step, 부모댓글 오름차순 정렬
                .offset(pageable.getOffset()) //시작지점(0부터 시작)
                .limit(pageable.getPageSize()) //페이지 사이즈(한 페이지당 몇개의 로우를 가져올지)
                .fetch();

        Map<Long, CommentResponseDTO> commentResponseDTOMap = new HashMap<>();
        List<CommentResponseDTO> commentResponseDTOList = new ArrayList<>();

        // 조회결과
//        for (Comment comment1 : commentList) {
//            log.info("comment id={} content={} parent={} children={}", comment1.getId(), comment1.getContent(), comment1.getParent(), comment1.getChildren());
//        }

        commentList.forEach(comment -> {
            CommentResponseDTO commentResponseDTO = entityToDto(comment);

            commentResponseDTOMap.put(commentResponseDTO.getId(), commentResponseDTO);
            if(comment.getParent() != null) {
                //대댓글이면 Map에 저장되어 있는 해당 댓글 DTO(commentResponseDTO) 의 자식댓글 리스트에 넣어준다
                commentResponseDTOMap.get(comment.getParent().getId()).getChildren().add(commentResponseDTO);
            }else {
                //최상위 댓글이면 리스트에 넣어준다
                commentResponseDTOList.add(commentResponseDTO);
            }



            //commentResponseDTOList.add(commentResponseDTO);
        });

        //DTO로 변환 후 결과
//        for (CommentResponseDTO commentResponseDTO : commentResponseDTOList) {
//            log.info("commentResponseDTO id={} content={} parent={} children={}", commentResponseDTO.getId(), commentResponseDTO.getContent(),
//                    commentResponseDTO.getParent(), commentResponseDTO.getChildren());
//        }

        return new PageImpl<>(commentResponseDTOList, pageable, getTotalCount(id));
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
