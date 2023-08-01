package hello.springcommunity.domain.comment;

import hello.springcommunity.domain.member.Member;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 댓글 응답 전용 객체 DTO
 */

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentResponseDTO {

    private Long id;
    private String content;
    private Member member;
    private LocalDate regDate;
    private Comment parent; //부모 댓글 id
    private int depth;
    private Boolean isDeleted;
    private List<CommentResponseDTO> children = new ArrayList<>(); //자식 댓글 리스트


    public CommentResponseDTO(Long id, String content, Member member, LocalDate regDate, Comment parent, int depth, Boolean isDeleted) {
        this.id = id;
        this.content = content;
        this.member = member;
        this.regDate = regDate;
        this.parent = parent;
        this.depth = depth;
        this.isDeleted = isDeleted;
    }

    // Entity -> DTO
    public static CommentResponseDTO entityToDto(Comment comment) {
        return comment.getIsDeleted() ? new CommentResponseDTO(comment.getId(), "삭제된 댓글입니다", comment.getMember(), comment.getRegDate(), comment.getParent(), comment.getDepth(), comment.getIsDeleted())
                : new CommentResponseDTO(comment.getId(), comment.getContent(), comment.getMember(), comment.getRegDate(), comment.getParent(), comment.getDepth(), comment.getIsDeleted());
    }

//    @Builder
//    public CommentResponseDTO(Comment comment) {
//        this.id = comment.getId();
//        this.regDate = comment.getRegDate();
//        this.content = comment.getContent();
//        this.depth = comment.getDepth();
//        this.isDeleted = comment.getIsDeleted();
//        this.member = comment.getMember();
//        this.parent = comment.getParent();
//    }
}
