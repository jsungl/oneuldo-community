package hello.springcommunity.common.validation;

/**
 * Validation 어노테이션 종류별로 인터페이스 생성
 * 각 검증 어노테이션을 지정하기 위한 인터페이스를 생성해줘야하는데,
 * 각각 생성하게 되면 파일이 늘어나므로 클래스 하나를 만들고 그 안에 중첩 인터페이스 형태로 선언한다.
 */
public class ValidationGroups {
    public interface NotBlankGroup {}
    public interface PatternGroup {}
    public interface EmailCheckGroup {}
//    public interface SizeGroup {}

}
