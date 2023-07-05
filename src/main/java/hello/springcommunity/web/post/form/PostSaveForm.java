package hello.springcommunity.web.post.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 폼 데이터 전달을 위한 별도의 객체 사용
 * 실무에서는 회원 등록시 회원과 관련된 데이터만 전달받는것이 아니라, 추가로 많은 부가 데이터가 넘어온다
 * 그래서 보통 Post 을 직접 전달받는것이 아니라, 복잡한 폼의 데이터를 컨트롤러까지 전달할 별도의 객체를 만들어서 전달한다
 * 여기서는 PostSaveForm 이라는 폼을 전달받는 전용 객체를 만들어서 @ModelAttribute 로 사용한다
 * 이것을 통해 컨트롤러에서 폼 데이터를 전달받고, 이후 컨트롤러에서 필요한 데이터를 사용해서 Post 을 생성한다
 */
@Data
public class PostSaveForm {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String author;

}
