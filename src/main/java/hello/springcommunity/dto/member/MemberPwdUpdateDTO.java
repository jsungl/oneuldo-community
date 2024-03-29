package hello.springcommunity.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static hello.springcommunity.common.validation.ValidationGroups.*;

@Getter
@Setter //쿼리 스트링 또는 폼 데이터로 전송되는 데이터를 바인딩하려면 Setter가 필요하다(없으면 매핑이 안되는듯)
@NoArgsConstructor
@AllArgsConstructor
public class MemberPwdUpdateDTO {

    @NotBlank(message = "기존 비밀번호를 입력해주세요.", groups = NotBlankGroup.class)
    private String currentPassword;

    @NotBlank(message = "새로운 비밀번호를 입력해주세요.", groups = NotBlankGroup.class)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,12}$", message = "비밀번호는 영문 대소문자, 숫자, 특수문자를 1개 이상 포함한 8~12자리수여야 합니다.", groups = PatternGroup.class)
    private String newPassword;

    @NotBlank(message = "다시 한번 입력해주세요.", groups = NotBlankGroup.class)
    private String rePassword;

}
