package hello.springcommunity.domain.member;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * 회원 리포지토리
 * JPA 사용
 */

@Slf4j
@Repository
public class MemberRepositoryOld {

    private final EntityManager em;

    //생성자를 통해 EntityManager 의존성 주입
    public MemberRepositoryOld(EntityManager em) {
        this.em = em;
    }
    

    public Member save(Member member) {
        em.persist(member);
        return member;
    }
    

    public Optional<Member> findOne(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

//    public Optional<Member> findByLoginId(String loginId) {
//        return findAll().stream()
//                .filter(m -> m.getLoginId().equals(loginId))
//                .findFirst();
//    }

    public Optional<Member> findByLoginId(String loginId) {
//        TypedQuery<Member> query = em.createQuery("select m from Member m where m.loginId = :loginId", Member.class);
//        query.setParameter("loginId", loginId);
//        Member foundMember = query.getSingleResult();
//
//        log.info("foundMember={}", foundMember);
//
//        return Optional.ofNullable(foundMember);

        List<Member> members = em.createQuery("select m from Member m where m.loginId = :loginId", Member.class)
                .setParameter("loginId", loginId)
                .getResultList();

        return members.stream().findAny();
    }


    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }
    
}
