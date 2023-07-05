package hello.springcommunity.domain.post;

import lombok.Data;

/**
 * 수정 폼 등록용 객체
 */
@Data
public class PostUpdateDto {
    private String postTitle;
    private String postBody;

    public PostUpdateDto() {
    }

    public PostUpdateDto(String postTitle, String postBody) {
        this.postTitle = postTitle;
        this.postBody = postBody;
    }
}
