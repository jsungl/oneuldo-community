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
import java.util.stream.Collectors;

import static hello.springcommunity.domain.comment.QComment.*;
import static hello.springcommunity.dto.comment.CommentResponseDTO.entityToDto;

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
     * 댓글 조회 1 - 페이징
     */
//    public Page<CommentResponseDTO> findCommentByPostId(PostResponseDTO post, Pageable pageable) {
//
//        //조회 결과인 commentList 는 정렬된 결과를 가지고 있다
//        List<Comment> commentList = query.selectFrom(comment)
//                .leftJoin(comment.parent).fetchJoin()
//                .join(comment.member).fetchJoin() //n+1 문제를 해결하기위해 fetchJoin
//                .where(comment.post.id.eq(post.getId()))
//                .orderBy(comment.groupId.asc(), comment.step.asc(), comment.parent.id.asc().nullsFirst()) //group id, step, 부모댓글 오름차순 정렬
//                .offset(pageable.getOffset()) //시작지점(0부터 시작)
//                .limit(pageable.getPageSize()) //페이지 사이즈(한 페이지당 몇개의 로우를 가져올지)
//                .fetch();
//
//
//        List<CommentResponseDTO> commentResponseDTOList = new ArrayList<>();
//
//        commentList.forEach(comment -> {
//            CommentResponseDTO commentResponseDTO = entityToDto(comment);
//            commentResponseDTOList.add(commentResponseDTO);
//        });
//
//        //3번째 인자 total 값으로 댓글(해당 post에 달린 댓글) 총 개수를 전달한다
//        return new PageImpl<>(commentResponseDTOList, pageable, post.getCommentNumber());
//    }

    /**
     * 댓글 조회 - 페이징
     */
    public List<Comment> findByPostId(Long postId, Pageable pageable) {

        List<Long> ids = query.select(comment.id)
                .from(comment)
                .where(comment.post.id.eq(postId))
                .orderBy(comment.groupId.asc(), comment.step.asc(), comment.parent.id.asc().nullsFirst())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        //ids.forEach(id -> log.info("id={}", id));

        return query.selectFrom(comment)
                .leftJoin(comment.post).fetchJoin()
                .leftJoin(comment.member).fetchJoin()
                .leftJoin(comment.children).fetchJoin()
                .where(comment.id.in(ids))
                .orderBy(comment.groupId.asc(), comment.step.asc(), comment.parent.id.asc().nullsFirst())
                .fetch().stream().distinct().collect(Collectors.toList()); //중복제거

    }

    public Page<CommentResponseDTO> convertNestedStructure(List<Comment> comments, Pageable pageable, Long totalCount) {
        Map<Long, CommentResponseDTO> commentResponseDTOMap = new HashMap<>();
        //최상위 댓글만 저장
        List<CommentResponseDTO> commentResponseDTOList = new ArrayList<>();

        comments.forEach(c -> {
            CommentResponseDTO commentResponseDTO = entityToDto(c);
            //각 댓글(commentResponseDTO) 은 자신의 자식 댓글들을 리스트로 가진다
            commentResponseDTOMap.put(commentResponseDTO.getId(), commentResponseDTO);

            if(c.getParent() != null) {
                //대댓글이면 Map에 저장되어 있는 해당 댓글 DTO(commentResponseDTO) 의 자식댓글 리스트에 넣어준다
                commentResponseDTOMap.get(c.getParent().getId()).getChildren().add(commentResponseDTO);
            } else {
                //최상위 댓글이면 바로 리스트에 넣어준다
                commentResponseDTOList.add(commentResponseDTO);
            }
        });

        return new PageImpl<>(commentResponseDTOList, pageable, totalCount);
    }


    /**
     * 전체 댓글 갯수 조회(count)
     */
    public Long findTotalCount(Long id) {
        Long count = query.select(Wildcard.count)
                .from(comment)
                .where(comment.post.id.eq(id))
                .fetchOne();

        return count;
    }

    /**
     * 댓글 step - 삭제되는 댓글 갯수
     */
    public void minusCommentStep(Comment c, Integer deleteTotalCount) {

        query.update(comment)
                .set(comment.step,comment.step.subtract(deleteTotalCount))
                .where(comment.post.id.eq(c.getPost().getId()),comment.groupId.eq(c.getGroupId()),comment.step.gt(c.getStep()))
                .execute();
    }

    /**
     * 댓글 step + 1
     */
    public void plusCommentStep(Long postId, Long groupId, Integer step) {
        query.update(comment)
                .set(comment.step,comment.step.add(1))
                .where(comment.post.id.eq(postId),comment.groupId.eq(groupId),comment.step.gt(step))
                .execute();
    }



    /**
     * 회원이 작성한 댓글의 현재 페이지 조회
     */
//    public List<CommentResponseDTO> findCommentByCommentId(PostResponseDTO post) {
//        List<Comment> commentList = query.selectFrom(comment)
//                .leftJoin(comment.post).fetchJoin()
//                .leftJoin(comment.member).fetchJoin()
//                .leftJoin(comment.children).fetchJoin()
//                .where(comment.post.id.eq(post.getId()))
//                .orderBy(comment.groupId.asc(), comment.step.asc(), comment.parent.id.asc().nullsFirst()) //group id, step, 부모댓글 오름차순 정렬
//                .fetch();
//
//        List<CommentResponseDTO> commentResponseDTOList = new ArrayList<>();
//        Map<Long, CommentResponseDTO> commentResponseDTOMap = new HashMap<>();
//
//        commentList.forEach(comment -> {
//            CommentResponseDTO commentResponseDTO = entityToDto(comment);
//            commentResponseDTOMap.put(commentResponseDTO.getId(), commentResponseDTO);
//
//            if(comment.getParent() != null) {
//                //대댓글이면 Map에 저장되어 있는 해당 댓글 DTO(commentResponseDTO) 의 자식댓글 리스트에 넣어준다
//                commentResponseDTOMap.get(comment.getParent().getId()).getChildren().add(commentResponseDTO);
//            } else {
//                //최상위 댓글이면 바로 리스트에 넣어준다
//                commentResponseDTOList.add(commentResponseDTO);
//            }
//        });
//
//        return commentResponseDTOList;
//    }
}
