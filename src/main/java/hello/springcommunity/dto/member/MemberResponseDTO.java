package hello.springcommunity.dto.member;

import hello.springcommunity.domain.member.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * repository에서 모든 정보를 찾아와 바로 member를 보내줄 수 있지만 그렇게 되면 전달하고 싶지 않은 내용도
 * 유저에게 전달하기 때문에 DTO를 새로 생성하고 한번 필터링한것을 전달한다
 */

@Getter
@NoArgsConstructor
public class MemberResponseDTO {

    private Long id;
    private String loginId;
    private String password;
    private String nickname;
    private String email;
    private String role;
    private boolean activated;
    private String createdDate;

    @Builder
    public MemberResponseDTO(Member member) {
        this.id = member.getId();
        this.loginId = member.getLoginId();
        this.password = member.getPassword();
        this.nickname = member.getNickname();
        this.email = member.getEmail();
        this.role = member.getRoleValue();
        this.activated = member.getActivated();
        this.createdDate = member.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
