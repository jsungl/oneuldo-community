package hello.springcommunity.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 검색조건으로 사용
 * 제목,내용,작성자
 */

@Getter
@AllArgsConstructor
public class PostSearchCond {

    private String title;
    private String content;
    private String nickname;

}
