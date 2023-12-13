package hello.springcommunity.domain.post;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hello.springcommunity.domain.TimeEntity;
import hello.springcommunity.domain.comment.Comment;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberLikePost;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

/**
 * PostDTO
 * JPA 사용
 */

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends TimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY) //관계를 지연로딩으로 설정.
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    //@Column(columnDefinition = "integer default 0", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Builder.Default
    private Integer likeCount = 0;

    //@ManyToOne 과 @OneToMany 로 양방향 관계를 맺어준다
    //게시글 UI에서 댓글을 바로 보여주기 위해 FetchType을 EAGER로 설정해준다 (펼쳐보기 같은 UI라면 Lazy로)
    //게시글이 삭제되면 댓글 또한 삭제되어야 하기 때문에 orphanRemoval = true 속성을 사용하여 조상 댓글 삭제시 고아가 된 하위 댓글들은 연쇄적으로 삭제한다
    //@OrderBy 어노테이션을 이용하여 간단히 정렬 처리 = @OrderBy("id asc")
    @OneToMany(mappedBy = "post", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Comment> comments;


    @JsonIgnore
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberLikePost> likePosts;

    
    //비즈니스 메서드
    //게시물 수정
    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
        this.onPreUpdate();
    }

    //조회수 증가
    public void updateViewCount() {
        this.viewCount = this.viewCount + 1;
    }

    //추천수 증가
    public void plusLikeCount() {
        this.likeCount = this.likeCount + 1;
    }

    //추천수 감소
    public void minusLikeCount() {
        this.likeCount = this.likeCount - 1;
    }
}
