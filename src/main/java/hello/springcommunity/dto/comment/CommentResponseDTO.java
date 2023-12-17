package hello.springcommunity.dto.comment;

import hello.springcommunity.domain.comment.Comment;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.post.Post;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 댓글 응답 전용 객체 DTO
 */

@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentResponseDTO {

    private Long id;
    private String content;
    private Member member;
    private Post post;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private Comment parent;
    private Integer depth;
    private Boolean isDeleted;
    private Boolean isModified;
    private List<CommentResponseDTO> children = new ArrayList<>();


    public CommentResponseDTO(Long id, String content, Member member, Post post, LocalDateTime createdDate, LocalDateTime modifiedDate,
                              Comment parent, Integer depth, Boolean isDeleted, Boolean isModified) {
        this.id = id;
        this.content = content;
        this.member = member;
        this.post = post;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.parent = parent;
        this.depth = depth;
        this.isDeleted = isDeleted;
        this.isModified = isModified;
    }


    /**
     * Entity -> DTO
     * 조건이 많아 Builder를 통해 생성하지 않는다
     */
    public static CommentResponseDTO entityToDto(Comment comment) {

        /**
         * truncatedTo() 메소드를 사용하여 LocalDateTime 객체를 원하는 단위(일, 시간, 분)로 변환한 후 비교
         */
        LocalDateTime createdTime = comment.getCreatedDate().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime modifiedTime = comment.getModifiedDate().truncatedTo(ChronoUnit.SECONDS);
        /** 수정여부 **/
        Boolean displayModification = false;

        /** 댓글 삭제 유무 **/
        if(comment.getIsDeleted()) {
            return new CommentResponseDTO(
                    comment.getId(),
                    "[삭제된 댓글입니다]",
                    comment.getMember(),
                    comment.getPost(),
                    comment.getCreatedDate(),
                    comment.getModifiedDate(),
                    comment.getParent(),
                    comment.getDepth(),
                    comment.getIsDeleted(),
                    displayModification);
        }else {

            /** 자식댓글이 있는경우 **/
            if(createdTime.compareTo(modifiedTime) < 0 && comment.getChildren().size() != 0) {
                displayModification = true;
            }

            return new CommentResponseDTO(
                    comment.getId(),
                    comment.getContent(),
                    comment.getMember(),
                    comment.getPost(),
                    comment.getCreatedDate(),
                    comment.getModifiedDate(),
                    comment.getParent(),
                    comment.getDepth(),
                    comment.getIsDeleted(),
                    displayModification);
        }

    }

}
