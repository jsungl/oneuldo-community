package hello.springcommunity.dto.security;

import hello.springcommunity.domain.member.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * UserDetails 설정
 * 
 */

@Getter
public class UserDetailsDTO implements UserDetails {

    private Member member;

    /**
     * 일반 로그인 생성자
     */
    public UserDetailsDTO(Member member) {
        this.member = member;
    }

    /**
     * 유저의 권한 목록, 권한 반환
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collections = new ArrayList<>();
        collections.add(() -> member.getRoleValue());
        return collections;
    }


    @Override
    public String getPassword() {
        return member.getPassword();
    }


    @Override
    public String getUsername() {
        return member.getLoginId();
    }

    /**
     * 계정이 만료 되었는지 (true: 만료X)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정이 잠겼는지 (true: 잠기지 않음)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 비밀번호가 만료되었는지 (true: 만료X)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정이 활성화(사용가능)인지 (true: 활성화)
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
