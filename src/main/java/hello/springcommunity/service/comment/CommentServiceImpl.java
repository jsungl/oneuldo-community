package hello.springcommunity.service.comment;

import hello.springcommunity.dao.comment.CommentQueryRepository;
import hello.springcommunity.dao.comment.CommentRepository;
import hello.springcommunity.dao.member.MemberRepository;
import hello.springcommunity.dao.post.PostRepository;
import hello.springcommunity.domain.comment.Comment;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.post.Post;
import hello.springcommunity.dto.comment.CommentRequestDTO;
import hello.springcommunity.dto.comment.CommentResponseDTO;
import hello.springcommunity.dto.post.PostResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static hello.springcommunity.dto.comment.CommentResponseDTO.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl {

    private final CommentRepository commentRepository;
    private final CommentQueryRepository commentQueryRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;


    /**
     * 댓글 조회 - 페이징
     */
    public Page<CommentResponseDTO> getPostComment(PostResponseDTO post, Pageable pageable) {
        return commentQueryRepository.findCommentsByPostId(post, pageable);
    }

    /**
     * 댓글 마지막 페이지 번호
     */
    public int getLastPageNumber(PostResponseDTO post, Pageable pageable) {
        int totalCount = commentQueryRepository.findTotalCount(post.getId()).intValue();
        int pageNumber = totalCount / pageable.getPageSize();
        int remainder = totalCount % pageable.getPageSize();

        if(totalCount == 0) {
            return 0;
        }

        return remainder == 0 ? pageNumber - 1 : pageNumber;
    }


    /**
     * 댓글의 현재 위치에 해당하는 페이지
     */
    public int getCommentPageNumber(Long commentId, PostResponseDTO post, Pageable pageable) {
        List<CommentResponseDTO> list = commentQueryRepository.findCommentsByCommentId(post);
        int num = 1;
        int temp = 0;

        for(CommentResponseDTO dto : list) {
            if(dto.getId().equals(commentId)) {
                temp = num;
            }
            num++;
        }

        int pageNumber = temp / pageable.getPageSize();
        int remainder = temp % pageable.getPageSize();

        return remainder == 0 ? pageNumber - 1 : pageNumber;

    }


    /**
     * 댓글 등록
     */
//    public Long addComment(CommentRequestDTO commentRequestDTO, Long postId, String loginId) {
//
//        Member member = memberRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));
//
//        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));
//
//
//        //DTO -> Entity
//        Comment comment = Comment.builder()
//                .content(commentRequestDTO.getContent())
//                .post(post)
//                .member(member)
//                .isDeleted(false)
//                .build();
//
//        try {
//            /**
//             * 대댓글인 경우
//             */
//            if(commentRequestDTO.getParentId() != null) {
//                Comment parent = commentRepository.findById(commentRequestDTO.getParentId()).orElseThrow(
//                        () -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
//
//                //저장하려는 대댓글의 부모 댓글을 찾아 연결
//                comment.updateParent(parent);
//
//                //댓글 그룹내 순서(step) 정하기
//                Integer result = updateCommentStep(parent);
//                log.info("step={}", result);
//
//                //음수면 대댓글 작성 오류
//                if(result == null) {
//                    throw new RuntimeException("답글을 등록할 수 없습니다.");
//                }
//
//                comment.updateDepth(parent.getDepth() + 1); //댓글 깊이 설정
//                comment.updateStep(result); //댓글 그룹내 순서 설정
//            }
//
//            //댓글 저장
//            Comment savedComment = commentRepository.save(comment);
//
//            //댓글 그룹 연결
//            updateGroupId(savedComment, savedComment.getId());
//
//            return savedComment.getId();
//
//        } catch (NullPointerException | ArithmeticException e) {
//            throw new RuntimeException("서버내부에서 오류가 발생하였습니다.");
//        }
//
//
//    }

    public Long addComment(CommentRequestDTO commentRequestDTO, Long postId, String loginId) {

        Member member = memberRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));

        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));

        int size = post.getComments().size();

        //DTO -> Entity
        Comment comment = Comment.builder()
                .content(commentRequestDTO.getContent())
                .post(post)
                .member(member)
                .isDeleted(false)
                .build();

        try {
            /**
             * 대댓글인 경우
             */
            if(commentRequestDTO.getParentId() != null) {
                Comment parent = commentRepository.findById(commentRequestDTO.getParentId()).orElseThrow(
                        () -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

                //저장하려는 대댓글의 부모 댓글을 찾아 연결
                comment.updateParent(parent);

                //댓글 그룹내 순서(step) 정하기
                Integer result = updateCommentStep(parent);

                //음수면 대댓글 작성 오류
                if(result == null) {
                    throw new RuntimeException("답글을 등록할 수 없습니다.");
                }

                comment.updateDepth(parent.getDepth() + 1); //댓글 깊이 설정
                comment.updateStep(result); //댓글 그룹내 순서 설정
                comment.updateGroupId(parent.getGroupId()); //댓글 그룹 연결

            } else {
                Long groupId = Optional.ofNullable(commentRepository.getGroupId(post.getId())).orElse(1L);

                if(size != 0) {
                    groupId = groupId + 1;
                }
                //댓글 그룹 연결
                comment.updateGroupId(groupId);
            }

            //댓글 저장
            Comment savedComment = commentRepository.save(comment);

            return savedComment.getId();

        } catch (NullPointerException | ArithmeticException e) {
            throw new RuntimeException("서버내부에서 오류가 발생하였습니다.");
        }

    }


    /**
     * 댓글 그룹내 순서(step) 정하기
     */
    private Integer updateCommentStep(Comment parent) {

        //댓글의 계층
        //대댓글이므로 부모 댓글의 depth 값에 +1
        //Integer commentDepth = parent.getDepth() + 1;
        Integer commentDepth = Optional.ofNullable(parent.getDepth() + 1).orElseThrow(NullPointerException::new);

        //댓글 그룹내 순서
        //부모 댓글의 step 값 가져오기
        //Integer commentStep = parent.getStep();
        Integer commentStep = Optional.ofNullable(parent.getStep()).orElseThrow(NullPointerException::new);

        List<Comment> childComments = commentRepository.findByParentIdAndGroupId(parent.getId(), parent.getGroupId());
        //지금 등록하는 댓글의 부모댓글의 자식댓글 수(자기 자신 제외)
        int child = childComments.size();

        //댓글 그룹 id
        //Long commentGroupId = parent.getGroupId();
        Long commentGroupId = Optional.ofNullable(parent.getGroupId()).orElseThrow(NullPointerException::new);

        //댓글그룹 내 최대 depth 값
        Integer groupMaxDepth = commentRepository.getMaxDepth(commentGroupId);

        //댓글그룹 내 총 대댓글 수(최상위 부모 댓글 제외)
        Long childCount = commentRepository.getChildCount(commentGroupId);


        //현재 대댓글의 depth 값과 그룹 내 최대 depth 값을 비교하여 step(그룹 내 순서)를 정한다
        if(commentDepth < groupMaxDepth) {
            log.info("그룹 내 최대 depth 보다 작음");
            //step = 그룹내의 총 자식 댓글 수(최상위 부모 댓글 제외) +1
            return Math.toIntExact(childCount + 1);

        } else if(commentDepth == groupMaxDepth) {
            log.info("그룹 내 최대 depth 와 같음");
            //그룹 내에서 commentStep + child 보다 큰 step 인 댓글들을 모두 +1
            commentRepository.plusCommentStep(commentGroupId, commentStep + child);
            // 그리고 step =  commentStep + child + 1
            return commentStep + child + 1;

        } else if(commentDepth > groupMaxDepth) {
            log.info("그룹 내 최대 depth 보다 큼");
            // 그룹 내에서 commentStep 보다 큰 step 인 댓글들을 모두 +1
            // 그리고 step = commentStep + 1
            commentRepository.plusCommentStep(commentGroupId, commentStep);
            return commentStep + 1;
        }

        //오류시 null 반환
        return null;

    }


    /**
     * 댓글 수정
     */
    public void updateComment(CommentRequestDTO commentRequestDTO, Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        comment.updateComment(commentRequestDTO.getContent());
    }

    /**
     * 댓글 삭제
     */
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        if(comment.getChildren().size() != 0) {
            //자식 댓글이 있으면 댓글의 삭제 상태를 true 로 변경
            comment.changeIsDeleted(true);
        }else {
            //자식 댓글이 없으면 삭제 가능한 부모 댓글이 있는지 확인 후 삭제
            Comment deletableComment = getDeletableParentComment(comment);
            //댓글 그룹에서 삭제 하려는 댓글의 step 보다 큰 step 의 댓글들은 모두 -1
            commentRepository.minusCommentStep(deletableComment.getGroupId(), deletableComment.getStep());
            commentRepository.delete(deletableComment);
        }
    }


    /**
     * 삭제 가능한 조상 댓글 찾기
     */
    private Comment getDeletableParentComment(Comment comment) {
        //현재 댓글의 부모를 구함
        Comment parent = comment.getParent();

        //부모가 null이 아니고, 부모의 자식댓글이 1개(지금 삭제하는 댓글)이고, 부모의 삭제 상태가 true(이미 삭제처리된 상태)인 댓글이라면
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
//    private void updateGroupId(Comment comment, Long commentId) {
//        Comment findComment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
//        Comment parent = findComment.getParent();
//
//        if(parent != null) {
//            updateGroupId(comment, parent.getId());
//        } else {
//            comment.updateGroupId(findComment.getId());
//        }
//    }


    /**
     * 해당 id를 가진 사용자가 작성한 댓글 모두 조회
     */
    public Page<CommentResponseDTO> getMemberCommentAll(Long id, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByMemberId(id, pageable);

        List<CommentResponseDTO> list = new ArrayList<>();
        for(Comment comment : comments) {
            CommentResponseDTO dto = entityToDto(comment);
            list.add(dto);
        }

        return new PageImpl<>(list, pageable, comments.getTotalElements());
    }


}
