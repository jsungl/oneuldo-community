package hello.springcommunity.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * 회원탈퇴 처리 결과에 대한 상태
 * message : 결과 메세지
 * state : 결과 상태 (true/false)
 * isTokenExpired : refresh 토큰 만료 여부 (true/false)
 */

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberLeaveResponseDTO {

    private String message;
    private boolean state;
    private boolean isTokenExpired;

}
