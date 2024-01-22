package hello.springcommunity.domain.comment;

import hello.springcommunity.domain.BaseTimeEntity;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.post.Post;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;


import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@DynamicInsert
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Comment extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    /** 댓글의 입장에선 게시글과 사용자는 다대일 관계이므로 @ManyToOne 이 된다 **/
    /** Post 엔티티와 @ManyToOne 양방향(@OneToMany 양방향) **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /** Member 엔티티와 @ManyToOne 단방향 **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;


    /**
     * 댓글 삭제 유무(true면 삭제된 댓글)
     *
     */
    @ColumnDefault("FALSE") //테이블 생성시 컬럼 기본값 설정
    @Column(nullable = false)
    private Boolean isDeleted;

    /** Comment 엔티티와 @ManyToOne 양방향(@OneToMany 양방향) **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id") //@OnDelete(action = OnDeleteAction.CASCADE)
    private Comment parent; //부모 댓글(null 이면 최상위 댓글)

    /**
     * 빌더로 엔티티 객체 생성시 해당 필드 기본값 설정은 null 이 아닌 empty list 가 된다
     * Comment 엔티티와 @ManyToOne 양방향(@OneToMany 양방향)
     */
    @Builder.Default
    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    @Builder.Default
    private Integer depth = 0;

    private Long groupId;

    @Builder.Default
    private Integer step = 0;


    /** Setter 대신 비즈니스 메서드 작성 **/

    /**
     * 댓글 수정
     */
    public void updateComment(String content) {
        this.content = content;
        //this.onPreUpdate();
    }

    /**
     * 부모 댓글 설정
     */
    public void updateParent(Comment comment) {
        this.parent = comment;
    }

    /**
     * 댓글 계층 level 설정
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
     * 댓글 그룹번호 설정
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
