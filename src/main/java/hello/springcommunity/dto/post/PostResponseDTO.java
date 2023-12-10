package hello.springcommunity.dto.post;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberLikePost;
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
    private Member member;
    private String loginId;
    private String nickname;
    private int commentNumber;
    //private List<CommentResponseDTO> comments;
    //private List<MemberLikePost> likePosts;

    // Entity -> DTO
    @Builder
    public PostResponseDTO(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.createdDate = post.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();
        this.member = post.getMember();
        this.loginId = post.getMember().getLoginId();
        this.nickname = post.getMember().getNickname();
        //this.nickname = post.getMember().getActivated() ? post.getMember().getNickname() : "알수없음";
        this.commentNumber = post.getComments().size();
        //this.comments = post.getComments().stream().map(CommentResponseDTO::entityToDto).collect(Collectors.toList());
    }
}
