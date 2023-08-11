package hello.springcommunity.common.validation;

import javax.validation.GroupSequence;

import static hello.springcommunity.common.validation.ValidationGroups.*;

/**
 * @GroupSequence를 사용하여 그룹별 인터페이스 순서 정의
 * 검증 어노테이션의 순서를 지정
 *
 * @GroupSequence의 파라미터 배열 순서로 검증 어노테이션이 차례대로 실행되고,
 * 하나의 그룹 검증이 성공하면 그 다음 파라미터의 그룹 검증으로 이어진다.
 * 검증순서는 @NotBlank → @Pattern 또는 @NotBlank → @Email
 */
@GroupSequence({NotBlankGroup.class, PatternGroup.class, EmailCheckGroup.class})
public interface ValidationSequence {
}
