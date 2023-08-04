package hello.springcommunity.dto.post;

import hello.springcommunity.domain.post.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 게시물 정보를 리턴한 응답 클래스
 * Entity 클래스를 생성자 파라미터로 받아서 데이터를 DTO로 변환하여 응답
 * 별도의 전달 객체를 활용해 연관관계를 맺은 엔티티간의 무한참조를 방지
 */

@Getter
@NoArgsConstructor
public class PostResponseDTO {

    private Long id;
    private String title;
    private String content;
    private LocalDate regDate;
    private int viewCount;
    private String loginId;
    private String name;

    // Entity -> DTO
    @Builder
    public PostResponseDTO(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.regDate = post.getRegDate();
        this.viewCount = post.getViewCount();
        this.loginId = post.getMember().getLoginId();
        this.name = post.getMember().getName();
    }
}
