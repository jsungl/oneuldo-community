package hello.springcommunity.dto.post;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberLikePost;
import hello.springcommunity.domain.post.CategoryCode;
import hello.springcommunity.domain.post.Notice;
import hello.springcommunity.domain.post.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 게시물 정보를 리턴한 응답 클래스
 * Entity 클래스를 생성자 파라미터로 받아서 데이터를 DTO로 변환하여 응답
 * 별도의 전달 객체를 활용해 연관관계를 맺은 엔티티간의 무한참조를 방지
 */

@Getter
@NoArgsConstructor
public class PostResponseDTO {

    private Long id;
    private String title;
    private String content;
    private String createdDate;
    private Integer viewCount;
    private Integer likeCount;
    private String categoryCode; //"CHAT","NOTICE"
    private String categoryName; //"잡담","공지"
    private Boolean fixed;
    private Member member;
    private String loginId;
    private String nickname;
    private int commentNumber;
    //private List<CommentResponseDTO> comments;
    //private List<MemberLikePost> likePosts;

    /**
     * Entity -> DTO
     */
    @Builder
    public PostResponseDTO(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.createdDate = post.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();
        this.categoryCode = post.getCategoryCode().name();
        this.categoryName = post.getCategoryName();
        this.fixed = post.getNotice() != null && post.getNotice().getFixed();
        this.member = post.getMember();
        this.loginId = post.getMember().getLoginId();
        this.nickname = post.getMember().getNickname();
        this.commentNumber = post.getComments().size();
        //this.comments = post.getComments().stream().map(CommentResponseDTO::entityToDto).collect(Collectors.toList());
    }

    /**
     * 생성자의 접근제어자를 protected로 선언하면 new Post() 작성이 불가하기 때문에 객체 자체의 일관성 유지력을 높일 수 있다.
     * 하지만 DTO 클래스이기 때문에 public으로 해도 무방하다
     */
    public PostResponseDTO(String title, String content, CategoryCode categoryCode, Boolean fixed) {
        this.title = title;
        this.content = content;
        this.categoryCode = categoryCode.name();
        this.fixed = fixed;
    }

}
