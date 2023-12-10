package hello.springcommunity.domain.comment;

import hello.springcommunity.domain.TimeEntity;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.post.Post;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@DynamicInsert
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Comment extends TimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Column(nullable = false)
    private String content;

    /** 댓글의 입장에선 게시글과 사용자는 다대일 관계이므로 @ManyToOne 이 된다 **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

//    @CreatedDate
//    private LocalDate regDate;

    /**
     * 댓글 삭제 유무(true면 삭제된 댓글)
     *
     */
    @ColumnDefault("FALSE") //테이블 생성시 컬럼 기본값 설정
    @Column(nullable = false)
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id") //@OnDelete(action = OnDeleteAction.CASCADE)
    private Comment parent; //부모 댓글(null 이면 최상위 댓글)

    /**
     * 빌더로 엔티티 객체 생성시 해당 필드 기본값 설정은 null 이 아닌 empty list 가 된다
     */
    @Builder.Default
    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    @Builder.Default
    private Integer depth = 0;

    private Long groupId;

    @Builder.Default
    private Integer step = 0;

    /**
     * 댓글 수정
     */
    public void updateComment(String content) {
        this.content = content;
        this.onPreUpdate();
    }

    /**
     * 부모 댓글 연결
     */
    public void updateParent(Comment comment) {
        this.parent = comment;
    }

    /**
     * 댓글 계층 level 연결
     */
    public void updateDepth(Integer depth) {
        this.depth = depth;
    }

    /**
     * 그룹 내 순서 정하기
     */
    public void updateStep(Integer step) {
        this.step = step;
    }

    /**
     * 댓글 그룹 연결
     */
    public void updateGroupId(Long groupId) {
        this.groupId = groupId;
    }

    /**
     * 삭제 유무 변경
     * true - 삭제됨
     */
    public void changeIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

}
