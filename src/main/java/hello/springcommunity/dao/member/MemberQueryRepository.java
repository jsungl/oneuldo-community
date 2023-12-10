package hello.springcommunity.dao.member;

import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.List;

import static hello.springcommunity.domain.member.QMember.*;

@Slf4j
@Repository
public class MemberQueryRepository {

    private final JPAQueryFactory query;


    public MemberQueryRepository(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    /**
     * 전체 회원 조회 - 페이징
     * 탈퇴 회원, 관리자를 제외한 모든 회원
     */
    public Page<Member> findAll(Pageable pageable) {

        Long totalCount = query.select(Wildcard.count)
                .from(member)
                .where(member.activated.eq(true), member.role.ne(Role.ADMIN))
                .fetchOne();

        List<Member> list = query.selectFrom(member)
                .where(member.activated.eq(true), member.role.ne(Role.ADMIN))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(member.id.desc())
                .fetch();

        return new PageImpl<>(list, pageable, totalCount);
    }

}
