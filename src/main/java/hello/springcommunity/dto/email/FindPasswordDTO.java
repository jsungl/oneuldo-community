package hello.springcommunity.dto.email;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import static hello.springcommunity.common.validation.ValidationGroups.*;

@Getter
@Setter
@NoArgsConstructor
public class FindPasswordDTO {

    @NotBlank(groups = NotBlankGroup.class)
    @Email(message = "이메일 형식에 맞지 않습니다", groups = EmailCheckGroup.class)
    private String email;

    @NotBlank(groups = NotBlankGroup.class)
    private String loginId;

}
