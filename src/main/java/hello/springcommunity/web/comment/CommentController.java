package hello.springcommunity.web.comment;

import hello.springcommunity.dto.comment.CommentRequestDTO;
import hello.springcommunity.dto.security.UserDetailsDTO;
import hello.springcommunity.service.comment.CommentServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST 방식을 이용해 댓글 처리
 *
 */

@Slf4j
@RestController //REST 방식 처리를 위한 에노테이션(@ResponseBody를 포함). 기본 리턴타입으로 JSON을 사용한다
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class CommentController {

    //private final CommentService commentService;
    private final CommentServiceImpl commentService;

    /**
     * 댓글 등록
     */
    @PostMapping("/{postId}/comment/add")
    public ResponseEntity<?> add(@PathVariable Long postId,
                                @RequestBody CommentRequestDTO commentRequestDTO,
                                @AuthenticationPrincipal UserDetailsDTO dto) {

        try {
            Long id = commentService.addComment(commentRequestDTO, postId, dto.getUsername());

            //dataType 이 JSON이 아니여야 한다
            //return ResponseEntity.ok("ok");
            //return ResponseEntity.status(HttpStatus.OK).body("ok");
            return ResponseEntity.status(HttpStatus.OK).body(id);

        } catch (UsernameNotFoundException e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("MEMBER_NOT_FOUND", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMap);

        } catch (IllegalArgumentException e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("INVALID_PARAMETER_INCLUDED", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);

        } catch (RuntimeException e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("UNEXPECTED_ERROR", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }


    }


    /**
     * 댓글 수정
     */
    @PutMapping("/{postId}/comment/{commentId}/edit")
    public ResponseEntity<?> update(@PathVariable Long commentId, @PathVariable Long postId, @RequestBody CommentRequestDTO commentRequestDTO) {
        try {
            commentService.updateComment(commentRequestDTO, commentId);
            //return ResponseEntity.ok(commentId);
            return ResponseEntity.status(HttpStatus.OK).body(commentId);

        } catch (IllegalArgumentException e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("INVALID_PARAMETER_INCLUDED", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
        }

    }


    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{postId}/comment/{commentId}/delete")
    public ResponseEntity<?> delete(@PathVariable Long postId, @PathVariable Long commentId) {
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.status(HttpStatus.OK).body(commentId);

        } catch (IllegalArgumentException e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("INVALID_PARAMETER_INCLUDED", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
        }

    }

}
