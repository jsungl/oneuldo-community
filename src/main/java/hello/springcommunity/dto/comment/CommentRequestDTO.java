package hello.springcommunity.dto.comment;

import lombok.*;

/**
 * 댓글 폼 요청 전용 객체 DTO
 */

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentRequestDTO {

    private Long parentId;
    private String content;
    private int depth;

    public CommentRequestDTO(String content) {
        this.content = content;
    }

}
