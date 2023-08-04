package hello.springcommunity.service.comment;

import hello.springcommunity.dao.comment.CommentQueryRepository;
import hello.springcommunity.dao.comment.CommentRepository;
import hello.springcommunity.domain.comment.Comment;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.dao.member.MemberRepository;
import hello.springcommunity.domain.post.Post;
import hello.springcommunity.dao.post.PostRepository;
import hello.springcommunity.dto.comment.CommentRequestDTO;
import hello.springcommunity.dto.comment.CommentResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentQueryRepository commentQueryRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    /**
     * 댓글 조회
     */
    public List<CommentResponseDTO> getCommentList(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다. id= " + id));

        /**
         * 리스트로 만든 엔티티를 DTO로 만들어 다시 리스트로 저장해 반환
         *
         * map - 데이터를 변형하는데 사용
         * 데이터에 해당 함수가 적용된 결과물을 제공하는 stream을 반환한다
         */

//        List<Comment> comments = commentRepository.findByPost(post);
//
//        List<CommentResponseDTO> dto = comments.stream()
//                .map(comment -> CommentResponseDTO.builder()
//                        .comment(comment)
//                        .build())
//                .collect(Collectors.toList());
//
//        return dto;

        return commentQueryRepository.findByPostId(post.getId());

    }

    /**
     * 댓글 조회 - 페이징
     */
    public Page<CommentResponseDTO> getCommentListPaging(Long id, Pageable pageable) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다. id= " + id));
        return commentQueryRepository.findCommentsByPostIdV2(post.getId(), pageable);
    }


    /**
     * 댓글 총 갯수 조회
     */
    public Long getCommentTotalCount(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다. id= " + id));
        return commentQueryRepository.getTotalCount(post.getId());
    }


    /**
     * 댓글 등록
     */
    public Comment add(CommentRequestDTO commentRequestDTO, Long postId, Long id) {
        Member findMember = memberRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재하지 않습니다. id=" + id));
        Post findPost = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다. id=" + postId));

        // DTO -> Entity
        Comment comment = Comment.builder()
                .content(commentRequestDTO.getContent())
                .post(findPost)
                .member(findMember)
                .isDeleted(false)
                .build();

        /**
         * 대댓글인 경우
         */
        if(commentRequestDTO.getParentId() != null) {
            Comment parent = commentRepository.findById(commentRequestDTO.getParentId()).orElseThrow(
                    () -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다. id= " + commentRequestDTO.getParentId()));

            //저장하려는 대댓글의 부모 댓글을 찾아 연결
            comment.updateParent(parent);

            //log.info("depth={}", commentRequestDTO.getDepth());
            //comment.updateDepth(commentRequestDTO.getDepth() + 1);
            Integer stepResult = CommentStepUpdate(parent);
            // null이면 대댓글 작성 오류
            if(stepResult == null) {
                return null;
            }
            log.info("현재 등록하는 대댓글의 step={}", stepResult);

            comment.updateDepth(parent.getDepth() + 1);
            comment.updateStep(stepResult);


        }

        return commentRepository.save(comment);

    }


    /**
     * 댓글 그룹내 순서(step) 정하기
     */
    private Integer CommentStepUpdate(Comment parent) {

        //댓글의 계층
        //대댓글이므로 부모 댓글의 depth 값에 +1
        int commentDepth = parent.getDepth() + 1;

        //댓글 그룹내 순서
        //부모 댓글의 step 값 가져오기
        Integer commentStep = parent.getStep();

        List<Comment> childComments = commentRepository.findByParentIdAndGroupId(parent.getId(), parent.getGroupId());
        //지금 등록하는 댓글의 부모댓글의 자식댓글 수(자기 자신 제외)
        Integer child = Integer.valueOf(childComments.size());
        //댓글 그룹 id
        Long commentGroupId = parent.getGroupId();

        log.info("depth = {}", commentDepth);
        log.info("step = {}", commentStep);
        log.info("groupId = {}", commentGroupId);
        log.info("자식댓글 수 = {}", child);

        //댓글그룹 내 최대 depth 값
        Long groupMaxDepth = commentRepository.findMaxDepth(commentGroupId); //3
        log.info("그룹 내 최대 depth = {}", groupMaxDepth);

        //댓글그룹 내 총 대댓글 수(최상위 부모 댓글 제외)
        Long childCountByGroup = commentRepository.findChildCountByGroup(commentGroupId);
        log.info("그룹 내 총 대댓글 수 = {}", childCountByGroup);


        //현재 대댓글의 depth 값과 그룹 내 최대 depth 값을 비교하여 step(그룹 내 순서)를 정한다
        if(commentDepth < groupMaxDepth) {
            log.info("commentDepth < groupMaxDepth");
            //Long -> int 형변환
            //Long은 null 값이 들어올수 있으므로 NullPointerException을 발생할 수 있다.
            //null일 경우에 대한 예외 처리를 위해 Optional.ofNullable()을 이용하여 null이 들어있을 경우 0L으로 바꿔서 Nullsafe하게 한다
            int result = Long.valueOf(Optional.ofNullable(childCountByGroup).orElse(0L)).intValue();

            //step = 그룹내의 총 자식 댓글 수(최상위 부모 댓글 제외) +1
            return Integer.valueOf(result + 1);

        }else if(commentDepth == groupMaxDepth) {
            log.info("commentDepth = groupMaxDepth");
            //그룹 내에서 commentStep + child 보다 큰 step 인 댓글들을 모두 +1
            commentRepository.updateCommentStepPlusV1(commentGroupId, commentStep + child);
            // 그리고 step =  commentStep + child + 1
            return commentStep + child + 1;

        } else if(commentDepth > groupMaxDepth) {
            log.info("commentDepth > groupMaxDepth");
            // 그룹 내에서 commentStep 보다 큰 step 인 댓글들을 모두 +1
            // 그리고 step = commentStep + 1
            commentRepository.updateCommentStepPlusV2(commentGroupId, commentStep);
            return commentStep + 1;
        }

        //오류시 null 반환
        return null;

    }

    /**
     * 댓글 수정
     */
    public void update(CommentRequestDTO commentRequestDTO, Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다. id=" + commentId));
        comment.updateComment(commentRequestDTO.getContent());
    }

    /**
     * 댓글 삭제
     */
    public void delete(Long commentId) {
        Comment comment = commentQueryRepository.findCommentByIdWithParent(commentId).orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다. id= " + commentId));

        if(comment.getChildren().size() != 0) {
            //자식 댓글이 있으면 댓글의 삭제 상태를 true 로 변경
            comment.changeIsDeleted(true);
        }else {
            //자식 댓글이 없으면 삭제 가능한 부모 댓글이 있는지 확인 후 삭제
            Comment deletableComment = getDeletableParentComment(comment);
            //삭제 하려는 댓글의 step 보다 큰 step 의 댓글들은 모두 -1
            commentRepository.updateCommentStepMinus(deletableComment.getGroupId(), deletableComment.getStep());
            commentRepository.delete(deletableComment);
        }
    }

    private Comment getDeletableParentComment(Comment comment) {
        //현재 댓글의 부모를 구함
        Comment parent = comment.getParent();

        //부모가 null이 아니고, 부모의 자식댓글이 1개(지금 삭제하는 댓글)이고, 부모의 삭제 상태가 true인 댓글이라면 재귀
        //재귀를 통해 삭제할 수 있는 조상 댓글까지 올라간다
        if(parent != null && parent.getIsDeleted() && parent.getChildren().size() == 1) {
            return getDeletableParentComment(comment);
        }

        //삭제해야 하는 댓글 반환
        return comment;
    }

    /**
     * 댓글 그룹 연결
     */
    public Long updateGroupId(Comment comment, Long commentId) {
        Comment findComment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다. id=" + commentId));
        Comment parent = findComment.getParent();

        if(parent != null) {
            return updateGroupId(comment, parent.getId());
        }

        comment.updateGroupId(findComment.getId());
        //Comment savedComment = commentRepository.save(comment);
        return findComment.getId();
    }
}
