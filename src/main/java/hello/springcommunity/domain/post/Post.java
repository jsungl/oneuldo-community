package hello.springcommunity.domain.post;

import hello.springcommunity.domain.BaseTimeEntity;
import hello.springcommunity.domain.comment.Comment;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberLikePost;
import lombok.*;

import javax.persistence.*;
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
public class Post extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    /** JPA에서 스트링 컬럼의 VARCHAR 255 제한 해제 **/
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    /** Member 엔티티와 @ManyToOne 양방향(@OneToMany 양방향) **/
    @ManyToOne(fetch = FetchType.LAZY) //관계를 지연로딩으로 설정.
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** @Column(columnDefinition = "integer default 0", nullable = false) **/
    @Builder.Default
    private Integer viewCount = 0;

    @Builder.Default
    private Integer likeCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean imageYn = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryCode categoryCode;

    /**
     * Post 테이블(주 테이블)에 외래키, Notice 테이블(대상 테이블)과 단방향 연결
     * Post 객체에서 Post.notice 를 통해 접근할 수 있지만,
     * Notice 엔티티와 @OneToOne 단방향
     * Notice 객체에서 post 객체를 접근할 수 없다
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "notice_id")
    private Notice notice;


    /**
     * @ManyToOne 과 @OneToMany 로 양방향 관계를 맺어준다
     * 게시글 UI에서 댓글을 바로 보여주기 위해 FetchType을 EAGER로 설정해준다 (펼쳐보기 같은 UI라면 Lazy로)
     * 게시글이 삭제되면 댓글 또한 삭제되어야 하기 때문에 orphanRemoval = true 속성을 사용하여 조상 댓글 삭제시 고아가 된 하위 댓글들은 연쇄적으로 삭제한다
     * @OrderBy 어노테이션을 이용하여 간단히 정렬 처리 = @OrderBy("id asc")
     */

    /** Comment 엔티티와 @ManyToOne 양방향(@OneToMany 양방향) **/
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Comment> comments;

    /** MemberLikePost 엔티티와 @ManyToOne 양방향(@OneToMany 양방향) **/
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberLikePost> likePosts;



    /**
     * 카테고리 이름 가져오기
     * 잡담, 유머, 공지,,,
     */
    public String getCategoryName() {
        return this.categoryCode.getDisplayName();
    }

    /**
     * Setter 대신 비즈니스 메서드 작성
     */

    /**
     * 게시물 수정
     */
    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
        //this.onPreUpdate();
    }


    /**
     * 조회수 증가
     */
    public void updateViewCount() {
        this.viewCount = this.viewCount + 1;
    }


    /**
     * 추천수 증가
     */
    public void plusLikeCount() {
        this.likeCount = this.likeCount + 1;
    }

    /**
     * 추천수 감소
     */
    public void minusLikeCount() {
        this.likeCount = this.likeCount - 1;
    }

    /**
     * 공지 등록
     */
    public void setNotice(Notice notice) {
        this.notice = notice;
    }

    /**
     * 이미지 여부
     * @param state
     */
    public void setImageYn(Boolean state) {
        this.imageYn = state;
    }

}
