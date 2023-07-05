package hello.springcommunity.domain.post;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * PostDTO
 * JPA 사용
 */

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_title")
    @NotBlank
    private String postTitle;

    @Column(name = "post_body")
    @NotBlank
    private String postBody;

    @Column(name = "post_author")
    @NotBlank
    private String postAuthor;

    @Column(name = "reg_date")
    @CreatedDate
    private LocalDate regDate;

    public Post() {
    }

    public Post(String postTitle, String postBody, String postAuthor) {
        this.postTitle = postTitle;
        this.postBody = postBody;
        this.postAuthor = postAuthor;
    }
}
