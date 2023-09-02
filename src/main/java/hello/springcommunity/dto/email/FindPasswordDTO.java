package hello.springcommunity.dto.email;

import hello.springcommunity.common.validation.ValidationGroups;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class FindPasswordDTO {

    @NotBlank(groups = ValidationGroups.NotBlankGroup.class)
    @Email(message = "이메일 형식에 맞지 않습니다", groups = ValidationGroups.EmailCheckGroup.class)
    private String email;

    @NotBlank
    private String loginId;

}
