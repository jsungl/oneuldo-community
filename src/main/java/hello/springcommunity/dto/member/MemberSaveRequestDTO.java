package hello.springcommunity.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static hello.springcommunity.common.validation.ValidationGroups.*;

/**
 * 회원가입 폼 등록용 객체(DTO)
 *
 * 외부로부터 전달받기 위한 DTO 로, 나중에 사용자에게 값을 return 해줘야 하는 상황에서
 * Entity 를 전부 전달하지 않고, DTO 를 통해 정보를 걸러낸 뒤 return 하는 방식을 사용한다
 */

@Getter
@Setter
@NoArgsConstructor
public class MemberSaveRequestDTO {

    @NotBlank(groups = NotBlankGroup.class) //groups 속성으로 해당 검증에 해당하는 인터페이스를 설정
    @Pattern(regexp = "^[a-z0-9]{4,15}$", message = "아이디는 영어 소문자와 숫자만 사용하여 4~15자리여야 합니다.", groups = PatternGroup.class)
    private String loginId;

    @NotBlank(groups = NotBlankGroup.class)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,16}$", message = "비밀번호는 영문 대소문자, 숫자, 특수문자를 1개 이상 포함한 8~16자리수여야 합니다.", groups = PatternGroup.class)
    private String password;

    @NotBlank(groups = NotBlankGroup.class)
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$", message = "닉네임은 특수문자를 포함하지 않은 2~10자리여야 합니다.", groups = PatternGroup.class)
    private String nickname;

//    @NotBlank(message = "이메일은 필수 입력사항입니다")
//    @Email(message = "이메일 형식에 맞지 않습니다")
//    private String userEmail;

}
