package hello.springcommunity.domain.comment;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.post.Post;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 댓글 폼 요청 전용 객체 DTO
 */

//@Getter
//@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDTO {

    private String content;

}
