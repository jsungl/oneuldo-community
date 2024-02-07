package hello.springcommunity.domain.post;


import hello.springcommunity.domain.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    /**
     * 상단에 고정됐는지 여부
     */
    @Column(nullable = false)
    private Boolean fixed;

//    @OneToOne(mappedBy = "notice")
//    private Post post;

    /**
     * 비즈니스 메서드
     */
    public void changeFixed(Boolean value) {
        this.fixed = value;
    }

}
