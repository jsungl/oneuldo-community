package hello.springcommunity.web.login;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * 로그인 폼 전송용 객체
 */

@Getter
@Setter
public class LoginForm {

    @NotBlank
    private String loginId;
    @NotBlank
    private String password;
}
