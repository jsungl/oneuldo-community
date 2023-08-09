package hello.springcommunity.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    /**
     * 스프링 시큐리티에서 권한 코드에 항상 맨 앞에 ROLE_ 이 있어야 한다
     */
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN"),
    SOCIAL("ROLE_SOCIAL");

    private final String value;
}
