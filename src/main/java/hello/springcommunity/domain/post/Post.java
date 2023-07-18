package hello.springcommunity.domain.post;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.web.post.form.PostSaveForm;
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

    private String title;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY) //관계를 지연로딩으로 설정.
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "reg_date")
    @CreatedDate
    private LocalDate regDate;

    @Builder
    public Post(Long id, String title, String content, Member member) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.member = member;
    }
    
    //생성 메서드
    public static Post savePost(PostSaveForm postSaveForm, Member member) {
        return Post.builder()
                .title(postSaveForm.getTitle())
                .content(postSaveForm.getContent())
                .member(member)
                .build();
    }
    
    //비즈니스 메서드
    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
    }


}
