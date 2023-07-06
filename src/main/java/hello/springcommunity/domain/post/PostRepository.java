package hello.springcommunity.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 스프링 데이터 JPA 적용
 *
 * 스프링 데이터 JPA 가 제공하는 JpaRepository 인터페이스를 상속받으면 기본적인 CRUD 기능을 사용할 수 있다
 * 기본 CRUD 와 단순 조회는 JpaRepository 를 상속받은 PostRepository 가 스프링 데이터 JPA 기능을 사용하여 처리
 * PostRepository 는 스프링 데이터 JPA 가 프록시 기술로 만들어주고 스프링 빈으로도 등록해준다
 *
 */

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
}
