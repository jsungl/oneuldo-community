package hello.springcommunity.domain.post;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class PostResponseDTO {

    private Long id;
    private String title;
    private String content;
    private LocalDate regDate;
    private String loginId;
    private String name;

    @Builder
    public PostResponseDTO(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.regDate = post.getRegDate();
        this.loginId = post.getMember().getLoginId();
        this.name = post.getMember().getName();
    }
}
