package hello.springcommunity.dto.comment;

import hello.springcommunity.domain.comment.Comment;
import hello.springcommunity.domain.member.Member;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
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
//@AllArgsConstructor
public class CommentResponseDTO {

    private Long id;
    private String content;
    private Member member;
    private String createdDate;
    private String modifiedDate;
    private Comment parent; //부모 댓글 id
    private int depth;
    private Boolean isDeleted;
    private Boolean isModified;
    private List<CommentResponseDTO> children = new ArrayList<>(); //자식 댓글 리스트


    public CommentResponseDTO(Long id, String content, Member member, LocalDateTime createdDate, LocalDateTime modifiedDate, Comment parent, int depth, Boolean isDeleted, Boolean isModified) {
        this.id = id;
        this.content = content;
        this.member = member;
        this.createdDate = createdDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.modifiedDate = modifiedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.parent = parent;
        this.depth = depth;
        this.isDeleted = isDeleted;
        this.isModified = isModified;
    }



    // Entity -> DTO
    public static CommentResponseDTO entityToDto(Comment comment) {

        /**
         * truncatedTo() 메소드를 사용하여 LocalDateTime 객체를 원하는 단위(일, 시간, 분)로 변환한 후 비교
         */
        LocalDateTime createdTime = comment.getCreatedDate().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime modifiedTime = comment.getModifiedDate().truncatedTo(ChronoUnit.MINUTES);
        Boolean displayModification = false;
        log.info("createdTime={}", createdTime);
        log.info("modifiedTime={}", modifiedTime);

        //댓글 삭제 유무
        if(comment.getIsDeleted()) {
            return new CommentResponseDTO(
                    comment.getId(),
                    "[삭제된 댓글입니다]",
                    comment.getMember(),
                    comment.getCreatedDate(),
                    comment.getModifiedDate(),
                    comment.getParent(),
                    comment.getDepth(),
                    comment.getIsDeleted(),
                    displayModification);
        }else {

            //자식댓글이 있는경우
            if(createdTime.compareTo(modifiedTime) < 0 && comment.getChildren().size() != 0) {
                displayModification = true;
            }

            return new CommentResponseDTO(
                    comment.getId(),
                    comment.getContent(),
                    comment.getMember(),
                    comment.getCreatedDate(),
                    comment.getModifiedDate(),
                    comment.getParent(),
                    comment.getDepth(),
                    comment.getIsDeleted(),
                    displayModification);
        }

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
