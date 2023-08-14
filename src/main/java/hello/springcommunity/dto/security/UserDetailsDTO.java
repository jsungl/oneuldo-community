package hello.springcommunity.dto.security;

import hello.springcommunity.domain.member.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * UserDetails 설정
 * 기존에는 폼 로그인을 위해 UserDetails 만 상속받았지만,
 * OAuth 로그인을 위해 OAuth2User 도 상속받는다
 *
 * 즉, 기존 폼 로그인과 OAuth 2.0 로그인을 따로 처리하지 않고 함께 UserDetails로 묶어 처리한다
 * 사이트에서 직접 회원가입한 회원들은 UserDetails 타입으로 저장되고, OAuth로 가입한 회원들은 OAuth2User로 저장되는데 이를 UserDetailsDTO 로 한번에 상속받아 사용한다
 * 
 */

@Getter
public class UserDetailsDTO implements UserDetails, OAuth2User {

    private Member member;

    private Map<String, Object> attributes; //oauth2 로그인을 통해서 받은 정보들을 그대로 담아 return 해주는 역할

    /**
     * 일반 로그인 생성자
     */
    public UserDetailsDTO(Member member) {
        this.member = member;
    }

    /**
     * oauth2 로그인 생성자
     */
    public UserDetailsDTO(Member member, Map<String, Object> attributes) {
        this.member = member;
        this.attributes = attributes;
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

    /**
     * OAuth2User 타입 오버라이딩
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return null;
    }
}
