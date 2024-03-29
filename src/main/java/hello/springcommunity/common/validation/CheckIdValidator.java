package hello.springcommunity.common.validation;

import hello.springcommunity.dao.member.MemberRepository;
import hello.springcommunity.dto.member.MemberSaveRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


/**
 * 로그인 아이디 중복 검사
 * 필드의 중복 검사 로직을 별도의 클래스로 분리. 
 * Validator 인터페이스를 구현한다.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckIdValidator implements Validator {

    private final MemberRepository memberRepository;

    /**
     * 여러 검증기(Validator)를 등록한다면 그 중에 어떤 검증기가 실행되어야 할지 구분이 필요하다. 이때 supports() 가 사용된다
     * supports(MemberSaveForm.class) 호출되고, 결과가 true 이므로 CheckIdValidator 의 validate() 가 호출된다
     */
    @Override
    public boolean supports(Class<?> clazz) {
//        return true;
        return MemberSaveRequestDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        MemberSaveRequestDTO memberSaveForm = (MemberSaveRequestDTO) target;

        if(memberRepository.existsByLoginIdAndActivated(memberSaveForm.getLoginId())) {
            errors.rejectValue("loginId", "아이디 중복 오류", "이미 사용중인 아이디입니다.");
        }

    }
}
