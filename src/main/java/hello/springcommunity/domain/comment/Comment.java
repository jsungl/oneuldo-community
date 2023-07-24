package hello.springcommunity.domain.comment;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.post.Post;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @CreatedDate
    private LocalDate regDate;

//    @Builder
//    public Comment(String content, Post post, Member member) {
//        this.content = content;
//        this.post = post;
//        this.member = member;
//    }

    /**
     * 댓글 수정
     * @param content
     */
    public void update(String content) {
        this.content = content;
    }

}
