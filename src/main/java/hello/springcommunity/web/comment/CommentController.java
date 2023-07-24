package hello.springcommunity.web.comment;

import hello.springcommunity.domain.comment.CommentRequestDTO;
import hello.springcommunity.domain.comment.CommentService;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.web.SessionConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
//@Controller
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 등록
     */
    @PostMapping("/{postId}/comment/add")
    public String addComment(@PathVariable Long postId,
                             @RequestBody CommentRequestDTO commentRequestDTO,
                             @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember) {
//        log.info("postId={}", postId);
//        log.info("content={}", commentRequestDTO.getContent());
        commentService.addComment(commentRequestDTO, postId, loginMember.getId());


        return "ok"; //dataType 이 JSON이 아니여야 한다
    }


    /**
     * 댓글 수정
     */
    @PutMapping("/{postId}/comment/{commentId}/edit")
    public ResponseEntity<Long> updateComment(@PathVariable Long commentId, @PathVariable Long postId, @RequestBody CommentRequestDTO commentRequestDTO) {
        commentService.updateComment(commentRequestDTO, commentId);
        return ResponseEntity.ok(commentId);
    }


    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{postId}/comment/{commentId}/delete")
    public ResponseEntity<Long> deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
//        log.info("postId={}", postId);
//        log.info("commentId={}", commentId);
        commentService.deleteComment(commentId);

        return ResponseEntity.ok(commentId);
    }






















}
