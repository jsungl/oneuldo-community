package hello.springcommunity.domain.comment;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CommentResponseDTO {

    private Long id;
    private String content;
    private String name;
    private String loginId;
    private LocalDate regDate;
    private Long postId;

    // Entity -> DTO
    @Builder
    public CommentResponseDTO(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.name = comment.getMember().getName();
        this.loginId = comment.getMember().getLoginId();
        this.regDate = comment.getRegDate();
        this.postId = comment.getPost().getId();
    }

}
