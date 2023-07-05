package hello.springcommunity.web.post.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 폼 데이터 전달을 위한 별도의 객체 사용
 * 등록과 수정은 완전히 다른 데이터가 넘어온다. 그리고 검증로직도 많이 달라진다.
 * 그래서 다음과 같이 PostUpdateForm 이라는 별도의 객체로 데이터를 전달받는다
 * 등록, 수정용 폼 객체를 나누면 등록, 수정이 완전히 분리되기 때문에 Bean Validation 의 groups 기능을 사용하지않아도 된다
 */
@Data
public class PostUpdateForm {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String author;

}
