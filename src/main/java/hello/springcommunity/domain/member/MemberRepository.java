package hello.springcommunity.domain.member;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JPA 사용
 */
@Slf4j
@Repository
@Transactional
public class MemberRepository {

//    private static Map<Long, Member> store = new ConcurrentHashMap<>();
//    private static long sequence = 0L;

    private final EntityManager em;

    public MemberRepository(EntityManager em) {
        this.em = em;
    }

//    public Member save(Member member) {
//        member.setId(++sequence);
//        log.info("save: member={}", member);
//        store.put(member.getId(), member);
//        return member;
//    }

    public Member save(Member member) {
        em.persist(member);
        return member;
    }

//    public Member findById(Long id) {
//        return store.get(id);
//    }

    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

//    public Optional<Member> findByLoginId(String loginId) {
//        return findAll().stream()
//                .filter(m -> m.getLoginId().equals(loginId))
//                .findFirst();
//    }

    public Optional<Member> findByLoginId(String loginId) {
        TypedQuery<Member> query = em.createQuery("select m from Member m where m.loginId = :loginId", Member.class);
        query.setParameter("loginId", loginId);
        Member foundMember = query.getSingleResult();

        log.info("foundMember={}", foundMember);

        return Optional.ofNullable(foundMember);
    }

//    public List<Member> findAll() {
//        return new ArrayList<>(store.values());
//    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

//    public void clearStore() {
//        store.clear();
//    }
}
