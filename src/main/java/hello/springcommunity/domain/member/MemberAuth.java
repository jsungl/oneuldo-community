package hello.springcommunity.domain.member;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "member_auth")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class MemberAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String authKey; //이메일 인증 코드

    @ColumnDefault("FALSE")
    @Column(nullable = false)
    private Boolean isAuthenticated; //이메일 인증 유무

    /**
     * 인증 상태 변경
     */
    public void updateAuth(boolean result, String key) {
        this.isAuthenticated = result;
        this.authKey = key;
    }

}
