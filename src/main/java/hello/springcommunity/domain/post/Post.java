package hello.springcommunity.domain.post;

import hello.springcommunity.domain.TimeEntity;
import hello.springcommunity.domain.member.Member;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;

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
    @JoinColumn(name = "member_id")
    private Member member;

//    @Column(name = "reg_date")
//    @CreatedDate
//    private LocalDate regDate;

    //@Column(columnDefinition = "integer default 0", nullable = false)
    @Column(name = "view_count")
    private int viewCount;

//    @OneToMany(mappedBy = "post", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
//    @OrderBy("id asc")
//    private List<Comment> comments;

    
    //비즈니스 메서드
    //게시물 수정
    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
    }

    //조회수 증가
    public void updateViewCount() {
        this.viewCount++;
    }


}
