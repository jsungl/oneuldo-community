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
 * 유저 닉네임 중복 검사
 * 필드의 중복 검사 로직을 별도의 클래스로 분리.
 * Validator 인터페이스를 구현한다.
 */

@Component
@RequiredArgsConstructor
public class CheckNicknameValidator implements Validator {

    private final MemberRepository memberRepository;


    @Override
    public boolean supports(Class<?> clazz) {
        return MemberSaveRequestDTO.class.isAssignableFrom(clazz) || MemberProfileUpdateDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        String nickname = null;

        if(target instanceof MemberSaveRequestDTO) {

            MemberSaveRequestDTO dto = (MemberSaveRequestDTO) target;
            nickname = dto.getNickname();
            checkNickname(nickname, errors);

        } else {

            MemberProfileUpdateDTO dto = (MemberProfileUpdateDTO) target;
            nickname = dto.getNickname();

            Optional<String> username = SecurityUtil.getLoginId();
            if(username.isEmpty()) {
                throw new CustomRuntimeException("회원정보를 수정할수 없습니다.");
            }

            String loginId = username.get();

            Optional<Member> member = memberRepository.findByLoginId(loginId);
            /** 닉네임을 변경한 경우 **/
            if(!member.get().getNickname().equals(nickname)) {
                checkNickname(nickname, errors);
            }

        }

    }

    private void checkNickname(String nickname, Errors errors) {

        if(memberRepository.existsByNicknameAndActivated(nickname)) {
            errors.rejectValue("nickname", "닉네임 중복 오류", "이미 사용중인 닉네임입니다.");
        }

    }
}
