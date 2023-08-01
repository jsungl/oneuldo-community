package hello.springcommunity.domain.post;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;

import static hello.springcommunity.domain.post.QPost.*;

/**
 * Querydsl 사용해서 복잡한 쿼리 처리 - 검색
 *
 */

@Slf4j
@Repository
public class PostQueryRepository {

    // Querydsl 을 사용하려면 JPAQueryFactory 가 필요하다
    // 그리고 JPAQueryFactory 는 JPA 쿼리인 JPQL 을 만들기 때문에 EntityManager 가 필요하다
    private final JPAQueryFactory query;

    public PostQueryRepository(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    // 검색조건 없음 -> 전체 조회
//    public List<Post> findAll(PostSearchCond cond) {
//        return query.select(post)
//                    .from(post)
//                    .where(likePostTitle(cond.getTitle()), likePostContent(cond.getContent()), likePostByLoginId(cond.getLoginId()))
//                    .fetch();
//    }

    /**
     * 전체 조회
     * 파라미터로 넘어온 검색조건으로 사용하여 조회한다
     * 만약 검색조건이 없다면 전체 조회한다
     */
    public Page<Post> findAll(PostSearchCond cond, Pageable pageable) {
        List<Post> postList = query.select(post)
                .from(post)
                .where(likePostTitle(cond.getTitle()), likePostContent(cond.getContent()), likePostByLoginId(cond.getLoginId()))
                .offset(pageable.getOffset()) //시작지점(0부터 시작)
                .limit(pageable.getPageSize()) //페이지 사이즈(한 페이지당 몇개의 로우를 가져올지)
                .fetch();

        Long count = query.select(post.count())
                .from(post)
                .where(likePostTitle(cond.getTitle()), likePostContent(cond.getContent()), likePostByLoginId(cond.getLoginId()))
                .fetchOne();


        log.info("count={}", count);
        return new PageImpl<>(postList, pageable, count);

    }


    // 작성자 이름으로 검색
    private BooleanExpression likePostByLoginId(String loginId) {
        if(StringUtils.hasText(loginId)) {
            return post.member.loginId.eq(loginId);
        }
        return null;
    }

    // 내용으로 검색
    private BooleanExpression likePostContent(String content) {
        if(StringUtils.hasText(content)) {
//            return post.content.like("%" + content + "%");
            return post.content.contains(content);
        }
        return null;
    }

    // 제목으로 검색
    private BooleanExpression likePostTitle(String title) {
        if(StringUtils.hasText(title)) {
            return post.title.like("%" + title + "%");
        }
        return null;
    }


}
