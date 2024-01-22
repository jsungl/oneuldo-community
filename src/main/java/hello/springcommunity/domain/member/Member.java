package hello.springcommunity.domain.member;

import hello.springcommunity.domain.BaseTimeEntity;
import hello.springcommunity.domain.post.Post;
import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 * Member Entity
 */

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED) //외부에서 기본 생성자를 통해 엔티티를 생성하지 못하도록 하여 객체 생성의 안전성을 높인다. 접근레벨은 protected
@AllArgsConstructor
public class Member extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    //@Column(unique = true)
    private String loginId;

    //@Column(unique = true)
    private String nickname;

    private String password;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activated = true;

    /** Post 엔티티와 @ManyToOne 양방향(@OneToMany 양방향) **/
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Post> posts;

    /** MemberLikePost 엔티티와 @ManyToOne 양방향(@OneToMany 양방향) **/
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberLikePost> likePosts;


    /**
     * 사용자 권한 이름 가져오기(ROLE_ADMIN, ROLE_USER ,,,)
     */
    public String getRoleValue() {
        return this.role.getValue();
    }

    /** Setter 대신 비즈니스 메서드 작성 **/

    /**
     * 회원정보 수정
     * 닉네임, 이메일
     */
    public void updateProfile(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
        //this.onPreUpdate();
    }


    /**
     * 비밀번호 수정
     */
    public void updatePassword(String newPassword) {
        this.password = newPassword;
        //this.onPreUpdate();
    }

    /**
     * 소셜 로그인시 이미 등록된 회원인 경우 닉네임이 변경되지 않은경우 수정 날짜만 업데이트
     * 기존 데이터는 유지
     */
    public Member updateModifiedDate() {
        this.beforeUpdate();
        return this;
    }


}
