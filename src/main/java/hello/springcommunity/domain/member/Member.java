package hello.springcommunity.domain.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hello.springcommunity.domain.TimeEntity;
import lombok.*;

import javax.persistence.*;
import java.util.List;

/**
 * Member Entity
 */

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED) //외부에서 기본 생성자를 통해 엔티티를 생성하지 못하도록 하여 객체 생성의 안전성을 높인다. 접근레벨은 protected
@AllArgsConstructor
public class Member extends TimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    //@Column(unique = true)
    private String loginId; //로그인 ID

    //@Column(unique = true)
    private String nickname; //사용자 이름

    private String password;

    private String email;

    @Enumerated(EnumType.STRING) //enum의 값을 index가 아닌 텍스트 값 그대로 저장
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activated = true;

    @JsonIgnore
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberLikePost> likePosts;


    public String getRoleValue() {
        return this.role.getValue();
    }

    /**
     * repository를 제외한 다른 영역에서 Entity를 수정할 수 있게 만들고 싶지 않아서
     */
    
    //닉네임, 이메일 수정
    public void updateProfile(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
        this.onPreUpdate();
    }

    //비밀번호 수정
    public void updatePassword(String newPassword) {
        this.password = newPassword;
        this.onPreUpdate();
    }

    //소셜 로그인시 이미 등록된 회원인 경우 닉네임이 변경되지 않은경우 수정 날짜만 업데이트
    //기존 데이터는 유지
    public Member updateModifiedDate() {
        this.onPreUpdate();
        return this;
    }

}
