package hello.springcommunity.domain.item;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 기본 CRUD 와 단순 조회는 JpaRepository 를 상속받은 ItemRepositoryV2 가 스프링 데이터 JPA 기능을 사용하여 처리
 */
public interface ItemRepositoryV2 extends JpaRepository<Item, Long> {
}
