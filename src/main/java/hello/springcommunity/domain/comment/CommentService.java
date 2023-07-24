package hello.springcommunity.domain.comment;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberRepository;
import hello.springcommunity.domain.post.Post;
import hello.springcommunity.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    /**
     * 댓글 조회
     */
    public List<CommentResponseDTO> getCommentList(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다"));

        List<Comment> comments = commentRepository.findByPost(post);

        /**
         * 리스트로 만든 엔티티를 DTO로 만들어 다시 리스트로 저장해 반환
         *
         * map - 데이터를 변형하는데 사용
         * 데이터에 해당 함수가 적용된 결과물을 제공하는 stream을 반환한다
         */
        // Entity -> DTO
        List<CommentResponseDTO> dto = comments.stream()
                .map(comment -> CommentResponseDTO.builder()
                        .comment(comment)
                        .build())
                .collect(Collectors.toList());

        return dto;
    }

    /**
     * 댓글 등록
     */
    public Long addComment(CommentRequestDTO commentRequestDTO, Long postId, Long id) {
        Member findMember = memberRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재하지 않습니다. id=" + id));
        Post findPost = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다. id=" + postId));

        // DTO -> Entity
        Comment result = Comment.builder()
                .content(commentRequestDTO.getContent())
                .post(findPost)
                .member(findMember)
                .build();

        //댓글 저장
        commentRepository.save(result);

        //댓글 id 반환
        return result.getId();

    }

    /**
     * 댓글 수정
     */
    public void updateComment(CommentRequestDTO commentRequestDTO, Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다. id=" + commentId));
        comment.update(commentRequestDTO.getContent());
        commentRepository.save(comment);
    }

    /**
     * 댓글 삭제
     */
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다. id=" + commentId));
        commentRepository.delete(comment);
        //commentRepository.deleteById(commentId);
    }
}
