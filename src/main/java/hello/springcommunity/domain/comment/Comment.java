package hello.springcommunity.domain.comment;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.post.Post;
import hello.springcommunity.domain.reply.Reply;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
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

    //@Column(updatable = false) : 생성시각은 수정되어서는 안되므로
    @CreatedDate
    private LocalDate regDate;

    @ColumnDefault("FALSE")
    @Column(nullable = false)
    private Boolean isDeleted; //댓글 삭제 유무(true 면 삭제된 댓글)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent; //부모 댓글(null 이면 최상위 댓글)

    @Builder.Default //빌더로 객체 생성시 해당 필드 기본값 설정 -> null 이 아닌 empty list
    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private List<Comment> children = new ArrayList<>(); //자식 댓글 리스트

    @Builder.Default
    private int depth = 0;

    private Long groupId;

    @Builder.Default
    private Integer step = 0;

    /**
     * 댓글 수정
     */
    public void updateComment(String content) {
        this.content = content;
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
    public void updateDepth(int depth) {
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
    public void updateGroupId(Long id) {
        this.groupId = id;
    }

    /**
     * 삭제 유무 변경
     * true - 삭제됨
     */
    public void changeIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

}
