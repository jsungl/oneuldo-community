package hello.springcommunity.domain.post;

import hello.springcommunity.domain.member.Member;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * PostDTO
 * JPA 사용
 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @ManyToOne(fetch = FetchType.LAZY) //관계를 지연로딩으로 설정.
    @JoinColumn(name = "member_id")
    @NotBlank
    private Member member;

    @Column(name = "reg_date")
    @CreatedDate
    private LocalDate regDate;

    @Builder
    public Post(String title, String content, Member member, LocalDate regDate) {
        this.title = title;
        this.content = content;
        this.member = member;
        this.regDate = regDate;
    }
    
    //생성 메서드
    public static Post createPost(String title, String content, Member member) {
        return Post.builder()
                .title(title)
                .content(content)
                .member(member)
                .regDate(LocalDate.now())
                .build();
    }
    
    //비즈니스 메서드
    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
    }


}
