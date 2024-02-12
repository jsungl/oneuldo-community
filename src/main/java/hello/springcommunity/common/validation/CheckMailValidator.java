package hello.springcommunity.common.validation;

import hello.springcommunity.common.security.SecurityUtil;
import hello.springcommunity.dao.member.MemberRepository;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.dto.member.MemberProfileUpdateDTO;
import hello.springcommunity.dto.member.MemberSaveRequestDTO;
import hello.springcommunity.exception.CustomRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

/**
 * 이메일 중복 검사
 * 필드의 중복 검사 로직을 별도의 클래스로 분리.
 * Validator 인터페이스를 구현한다.
 */

@Component
@RequiredArgsConstructor
public class CheckMailValidator implements Validator {

    private final MemberRepository memberRepository;


    @Override
    public boolean supports(Class<?> clazz) {
        return MemberSaveRequestDTO.class.isAssignableFrom(clazz) || MemberProfileUpdateDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        String email = null;

        if(target instanceof MemberSaveRequestDTO) {

            MemberSaveRequestDTO dto = (MemberSaveRequestDTO) target;
            email = dto.getEmail();
            checkEmail(email, errors);

        } else {

            MemberProfileUpdateDTO dto = (MemberProfileUpdateDTO) target;
            email = dto.getEmail();

            Optional<String> username = SecurityUtil.getLoginId();
            if(username.isEmpty()) {
                throw new CustomRuntimeException("회원정보를 수정할수 없습니다.");
            }

            String loginId = username.get();

            Optional<Member> member = memberRepository.findByLoginId(loginId);
            String roleValue = member.get().getRoleValue();

            /** 소셜 회원이 아니고 이메일을 변경한 경우 **/
            if(!roleValue.equals("ROLE_SOCIAL") && !member.get().getEmail().equals(email)) {
                checkEmail(email, errors);
            }


        }

    }

    private void checkEmail(String email, Errors errors) {

        if(memberRepository.existsByEmailAndActivated(email)) {
            errors.rejectValue("email", "이메일 중복 오류", "이미 사용중인 메일주소입니다.");
        }
    }


}
