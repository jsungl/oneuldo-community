package hello.springcommunity.domain.post;

import hello.springcommunity.domain.member.Member;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * PostDTO
 * JPA 사용
 */

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @ManyToOne
    @JoinColumn(name = "member_id")
    @NotBlank
    private Member member;

    @Column(name = "reg_date")
    @CreatedDate
    private LocalDate regDate;

    public Post() {
    }


}
