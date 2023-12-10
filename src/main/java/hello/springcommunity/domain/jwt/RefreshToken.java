package hello.springcommunity.domain.jwt;

import hello.springcommunity.domain.member.Member;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "refresh_token")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class RefreshToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long id;

    private String value;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String loginId;

    public RefreshToken(Member member, String refreshToken) {
        this.member = member;
        this.value = refreshToken;
        this.loginId = member.getLoginId();
    }

    public void update(String refreshToken) {
        this.value = refreshToken;
    }
}
