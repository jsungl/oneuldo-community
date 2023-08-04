package hello.springcommunity.dto.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * 로그인 폼 전송용 객체
 */

@Getter
//@Setter
@AllArgsConstructor
public class LoginForm {

    @NotBlank
    private String loginId;
    @NotBlank
    private String password;
}
