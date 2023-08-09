package hello.springcommunity.web.comment;

import hello.springcommunity.domain.comment.Comment;
import hello.springcommunity.dto.comment.CommentRequestDTO;
import hello.springcommunity.dto.security.UserDetailsDTO;
import hello.springcommunity.service.comment.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST 방식을 이용해 댓글 처리
 *
 */

@Slf4j
@RestController //REST 방식 처리를 위한 에노테이션(@ResponseBody를 포함). 기본 리턴타입으로 JSON을 사용한다
//@Controller
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 등록
     */
    @PostMapping("/{postId}/comment/add")
    public ResponseEntity<String> addComment(@PathVariable Long postId,
                                            @RequestBody CommentRequestDTO commentRequestDTO,
                                             @AuthenticationPrincipal UserDetailsDTO dto) {

        //Comment comment = commentService.add(commentRequestDTO, postId, loginMember.getId());
        Comment comment = commentService.add(commentRequestDTO, postId, dto.getUsername());

        Long groupId = commentService.updateGroupId(comment, comment.getId());
        log.info("comment groupId={}", groupId);

        //dataType 이 JSON이 아니여야 한다
        //return "ok";
        return ResponseEntity.ok("ok");
    }


    /**
     * 댓글 수정
     */
    @PutMapping("/{postId}/comment/{commentId}/edit")
    public ResponseEntity<Long> updateComment(@PathVariable Long commentId, @PathVariable Long postId, @RequestBody CommentRequestDTO commentRequestDTO) {
        commentService.update(commentRequestDTO, commentId);
        return ResponseEntity.ok(commentId);
    }


    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{postId}/comment/{commentId}/delete")
    public ResponseEntity<Long> deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        commentService.delete(commentId);
        return ResponseEntity.ok(commentId);
    }

}
