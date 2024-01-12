package hello.springcommunity.dao.post;

import hello.springcommunity.domain.post.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 스프링 데이터 JPA 적용
 *
 * 스프링 데이터 JPA 가 제공하는 JpaRepository 인터페이스를 상속받으면 기본적인 CRUD 기능을 사용할 수 있다
 * 기본 CRUD 와 단순 조회는 JpaRepository 를 상속받은 PostRepository 가 스프링 데이터 JPA 기능을 사용하여 처리
 * PostRepository 는 스프링 데이터 JPA 가 프록시 기술로 만들어주고 스프링 빈으로도 등록해준다
 *
 * 스프링 데이터 JPA 는 proxy 패턴으로 구현되어 bean 으로 등록하기 때문에 사용자로 하여금 bean 객체로 등록되지 않기 위해 @Repository 를 설정하지 않는다
 *
 */

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * n+1 문제를 해결하기위해 @EntityGraph 사용
     */
//    @EntityGraph(attributePaths = {"member","comments","notice"})
//    Optional<Post> findById(Long id);

    //제목으로 검색
//    Page<Post> findByTitleContaining(String keyword, Pageable pageable);

    //내용으로 검색
//    Page<Post> findByContentContaining(String keyword, Pageable pageable);

    //닉네임으로 검색
//    Page<Post> findByNicknameContaining(String keyword, Pageable pageable);


    /**
     * 게시물 1개 삭제
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from Post p where p.id = :postId")
    void deleteOne(@Param("postId") Long id);

    /**
     * 특정 회원이 작성한 게시물 모두 삭제
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from Post p where p.member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long id);

}
