package hello.springcommunity.domain.member;

import hello.springcommunity.domain.post.Post;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class MemberLikePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Member 엔티티와 @ManyToOne 양방향(@OneToMany 양방향) **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    /** Post 엔티티와 @ManyToOne 양방향(@OneToMany 양방향) **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;


}
