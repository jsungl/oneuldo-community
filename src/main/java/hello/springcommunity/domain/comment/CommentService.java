package hello.springcommunity.domain.comment;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberRepository;
import hello.springcommunity.domain.post.Post;
import hello.springcommunity.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

        //댓글 저장
        //저장하려는게 대댓글이면
        if(commentRequestDTO.getParentId() != null) {
            Comment parent = commentRepository.findById(commentRequestDTO.getParentId()).orElseThrow(
                    () -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다. id= " + commentRequestDTO.getParentId()));

            //저장하려는 대댓글의 부모 댓글을 찾아 연결
            comment.updateParent(parent);
            log.info("depth={}", commentRequestDTO.getDepth());
            comment.updateDepth(commentRequestDTO.getDepth() + 1);
        }

        return commentRepository.save(comment);

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
            commentRepository.delete(getDeletableParentComment(comment));
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


}
