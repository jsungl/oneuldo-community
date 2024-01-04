package hello.springcommunity.dto.member;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static hello.springcommunity.common.validation.ValidationGroups.*;


@Getter
@Setter //쿼리 스트링 또는 폼 데이터로 전송되는 데이터를 바인딩하려면 Setter가 필요하다(없으면 매핑이 안되는듯)
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileUpdateDTO {

    @NotBlank(message = "아이디를 입력해주세요.")
    private String loginId;

    @NotBlank(message = "닉네임을 입력해주세요.", groups = NotBlankGroup.class)
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,8}$", message = "닉네임은 특수문자를 포함하지 않은 2~8자리여야 합니다.", groups = PatternGroup.class)
    private String nickname;

    @NotBlank(message = "이메일을 입력해주세요.", groups = NotBlankGroup.class)
    @Email(message = "이메일 형식에 맞지 않습니다.", groups = EmailCheckGroup.class)
    private String email;

}
