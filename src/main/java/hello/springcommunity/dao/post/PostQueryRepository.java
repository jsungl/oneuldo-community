package hello.springcommunity.dao.post;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.springcommunity.domain.post.CategoryCode;
import hello.springcommunity.domain.post.Post;
import hello.springcommunity.domain.post.QNotice;
import hello.springcommunity.dto.post.PostSearchCond;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static hello.springcommunity.domain.comment.QComment.*;
import static hello.springcommunity.domain.member.QMember.*;
import static hello.springcommunity.domain.post.CategoryCode.*;
import static hello.springcommunity.domain.post.QNotice.*;
import static hello.springcommunity.domain.post.QPost.*;

/**
 * Querydsl 사용해서 복잡한 쿼리 처리 - 검색
 *
 */

@Slf4j
@Repository
public class PostQueryRepository {

    private final JPAQueryFactory query;

    /**
     * Querydsl 을 사용하려면 JPAQueryFactory 가 필요하다
     * 그리고 JPAQueryFactory 는 JPA 쿼리인 JPQL 을 만들기 때문에 EntityManager 가 필요하다
     */
    public PostQueryRepository(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }


    /**
     * 게시물 1개 조회
     * @param postId
     * @return
     */
    public Optional<Post> findOne(Long postId) {

        Post findPost = query.selectFrom(post)
                .leftJoin(post.notice, notice)
                .fetchJoin()
                .leftJoin(post.member, member)
                .fetchJoin()
                .leftJoin(post.comments, comment)
                .fetchJoin()
                .where(post.id.eq(postId))
                .fetchOne();

        return Optional.ofNullable(findPost);
    }

    /**
     * 전체조회
     * 작성자가 탈퇴한 게시물,공지 제외 모든 게시물 조회
     */
    public Page<Post> findAll(Pageable pageable) {

        Long totalCount = query.select(Wildcard.count)
                .from(post)
                .where(post.categoryCode.ne(NOTICE), isMemberActivated())
                .fetchOne();

        /**
         * 앞의 서비스 클래스에서 만든 pageable 객체의 sort 인자를 통해
         * getOrderSpecifier()의 List결과값을 반환했고, 이를 stream.toArray() 메소드를 활용해 OrderSpecifier를 배열 형태로 만들어 적용시킨다
         * OrderSpecifier[]::new 는 함수형 인터페이스에서 생성자 메소드 참조로, :: 기준 우측에 있는 생성자 메소드를 적용해 배열을 생성하겠다는 의미다
         */
        List<Post> list =  query.selectFrom(post)
                .where(post.categoryCode.ne(NOTICE), isMemberActivated())
                .orderBy(getOrderSpecifier(pageable.getSort()).stream().toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        return new PageImpl<>(list, pageable, totalCount);
    }

    public Page<Post> findAllV2(Pageable pageable) {

        Long totalCount = query.select(Wildcard.count)
                .from(post)
                .where(post.categoryCode.ne(NOTICE), isMemberActivated())
                .fetchOne();


        List<Long> ids = query.select(post.id)
                .from(post)
                .where(post.categoryCode.ne(NOTICE), isMemberActivated())
                .orderBy(getOrderSpecifier(pageable.getSort()).stream().toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<Post> list = query.selectFrom(post)
                .leftJoin(post.member).fetchJoin()
                .leftJoin(post.comments).fetchJoin()
                .where(post.id.in(ids))
                .orderBy(getOrderSpecifier(pageable.getSort()).stream().toArray(OrderSpecifier[]::new))
                .fetch().stream().distinct().collect(Collectors.toList());


        return new PageImpl<>(list, pageable, totalCount);
    }

    /**
     * 상단에 고정된 공지 조회(fixed = true)
     */
    public List<Post> findTopNotice() {
        List<Post> list = query.selectFrom(post)
                .leftJoin(post.member, member)
                .fetchJoin()
                .leftJoin(post.comments, comment)
                .fetchJoin()
                .leftJoin(post.notice, notice)
                .fetchJoin()
                .where(post.categoryCode.eq(NOTICE), post.notice.fixed.eq(true))
                .orderBy(post.notice.id.desc())
                .fetch();

        return list;
    }

    public Page<Post> findByCategory(CategoryCode category, Pageable pageable) {

        Long totalCount = query.select(Wildcard.count)
                .from(post)
                .where(post.categoryCode.eq(category), isMemberActivated())
                .fetchOne();

//        List<Post> list =  query.selectFrom(post)
//                .where(post.categoryCode.eq(category), isMemberActivated())
//                .orderBy(getOrderSpecifier(pageable.getSort()).stream().toArray(OrderSpecifier[]::new))
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();

        List<Long> ids = query.select(post.id)
                .from(post)
                .where(post.categoryCode.eq(category), isMemberActivated())
                .orderBy(getOrderSpecifier(pageable.getSort()).stream().toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<Post> list = query.selectFrom(post)
                .leftJoin(post.member).fetchJoin()
                .leftJoin(post.comments).fetchJoin()
                .where(post.id.in(ids))
                .orderBy(getOrderSpecifier(pageable.getSort()).stream().toArray(OrderSpecifier[]::new))
                .fetch().stream().distinct().collect(Collectors.toList());


        return new PageImpl<>(list, pageable, totalCount);
    }


    /**
     * 동적으로 Pageable 객체에서 sort 정보를 빼와 orderBy 를 통해 정렬하고 싶다면
     * OrderSpecifier 라는 클래스를 구현해야 한다
     *
     * OrderSpecifier 클래스를 구현하기 위해서 필요로 하는 인자들은 Sort의 order와 Expression 객체, Null값을 핸들링하기 위한 enum값 이다
     * 이때 Expression target은 정렬 기준이 되는 칼럼의 Path로, 기준이 되는 칼럼과 그 칼럼이 속한 엔티티가 합쳐진 것이다
     * 예를 들어 postId를 정렬 기준으로 정하면, postId는 당연히 Post 엔티티에 속할 것이고 이 두개가 합쳐진 post(Post객체).postId가 Path가 된다
     *
     * List형태로 반환되는 이유는 애초에 sort정보를 여러 개 넣을 수 있기 때문이고, 만약 첫번째 정렬기준에서 동일한 값을 가진 경우는 최신순으로 조회되게 설정하기 위해 sort정보를 2개 넣는다
     */
    private List<OrderSpecifier> getOrderSpecifier(Sort sort) {
        List<OrderSpecifier> orders = new ArrayList<>();
        sort.stream().forEach(order -> {
            //viewCount: DESC
            //log.info("order={}", order);

            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            //direction=DESC
            //log.info("direction={}", direction);

            String prop = order.getProperty();
            //viewCount
            //log.info("prop={}", prop);

            PathBuilder orderByExpression = new PathBuilder(Post.class, "post");
            //post
            //log.info("orderByExpression={}", orderByExpression);

            /** orders 리스트에 추가되는것은 OrderSpecifier 객체들이다 **/
            orders.add(new OrderSpecifier<>(direction, orderByExpression.get(prop)));
        });

        return orders;
    }


    /**
     * 특정 회원이 작성한 게시물 모두 조회(페이징)
     */
    public Page<Post> findByMemberId(Long id, Pageable pageable) {

        Long totalCount = query.select(Wildcard.count)
                .from(post)
                .where(post.member.id.eq(id))
                .fetchOne();

//        List<Post> list =  query.selectFrom(post)
//                .where(post.member.id.eq(id))
//                .orderBy(getOrderSpecifier(pageable.getSort()).stream().toArray(OrderSpecifier[]::new))
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();

        List<Long> ids = query.select(post.id)
                .from(post)
                .where(post.member.id.eq(id))
                .orderBy(getOrderSpecifier(pageable.getSort()).stream().toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<Post> list = query.selectFrom(post)
                .leftJoin(post.member).fetchJoin()
                .leftJoin(post.comments).fetchJoin()
                .where(post.id.in(ids))
                .orderBy(getOrderSpecifier(pageable.getSort()).stream().toArray(OrderSpecifier[]::new))
                .fetch().stream().distinct().collect(Collectors.toList());


        return new PageImpl<>(list, pageable, totalCount);
    }




    /**
     * 검색 조회
     * 파라미터로 넘어온 검색조건으로 사용하여 조회한다
     * 만약 검색조건이 없다면 전체 조회한다
     */
    public Page<Post> findAllBySearchCond(PostSearchCond cond, Pageable pageable) {

        Long totalCount = query.select(Wildcard.count)
                .from(post)
                .where(isMemberActivated(),likePostTitle(cond.getTitle()), likePostContent(cond.getContent()),
                        findPostByNickname(cond.getNickname()))
                .fetchOne();

//        List<Post> postList = query.selectFrom(post)
//                .where(isMemberActivated(),likePostTitle(cond.getTitle()), likePostContent(cond.getContent()),
//                        findPostByNickname(cond.getNickname()))
//                .orderBy(getOrderSpecifier(pageable.getSort()).stream().toArray(OrderSpecifier[]::new))
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();

        List<Long> ids = query.select(post.id)
                .from(post)
                .where(isMemberActivated(), likePostTitle(cond.getTitle()), likePostContent(cond.getContent()),
                        findPostByNickname(cond.getNickname()))
                .orderBy(getOrderSpecifier(pageable.getSort()).stream().toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<Post> postList = query.selectFrom(post)
                .leftJoin(post.member).fetchJoin()
                .leftJoin(post.comments).fetchJoin()
                .where(post.id.in(ids))
                .orderBy(getOrderSpecifier(pageable.getSort()).stream().toArray(OrderSpecifier[]::new))
                .fetch().stream().distinct().collect(Collectors.toList());


        return new PageImpl<>(postList, pageable, totalCount);

    }

    public Page<Post> findAllByMemberId(Long memberId, Pageable pageable, Long totalCount) {

        List<Long> ids = query.select(post.id)
                .from(post)
                .where(post.member.id.eq(memberId))
                .orderBy(getOrderSpecifier(pageable.getSort()).stream().toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<Post> list = query.selectFrom(post)
                .leftJoin(post.member).fetchJoin()
                .leftJoin(post.comments).fetchJoin()
                .where(post.id.in(ids))
                .orderBy(getOrderSpecifier(pageable.getSort()).stream().toArray(OrderSpecifier[]::new))
                .fetch().stream().distinct().collect(Collectors.toList());

        return new PageImpl<>(list, pageable, totalCount);
    }



    /**
     * 해당 유저 탈퇴유무
     */
    private BooleanExpression isMemberActivated() {
        return post.member.activated.eq(true);
    }


    /**
     * id 로 검색
     */
//    private BooleanExpression findPostByMemberId(Long memberId) {
//        return post.member.id.eq(memberId);
//    }


    /**
     * 작성자 닉네임으로 검색
     */
    private BooleanExpression findPostByNickname(String nickname) {
        if(StringUtils.hasText(nickname)) {
            return post.member.nickname.eq(nickname);
        }
        return null;
    }

    /**
     * 내용으로 검색
     */
    private BooleanExpression likePostContent(String content) {
        if(StringUtils.hasText(content)) {
//            return post.content.like("%" + content + "%");
            return post.content.contains(content);
        }
        return null;
    }

    /**
     * 제목으로 검색
     */
    private BooleanExpression likePostTitle(String title) {
        if(StringUtils.hasText(title)) {
//            return post.title.like("%" + title + "%");
            return post.title.contains(title);
        }
        return null;
    }


}
