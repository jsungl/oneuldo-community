package hello.springcommunity.domain.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 스프링 데이터 JPA 적용
 * 스프링 데이터 JPA 가 제공하는 JpaRepository 인터페이스를 상속받으면 기본적인 CRUD 기능을 사용할 수 있다
 *
 * SpringDataJpaItemRepository 는 스프링 데이터 JPA 가 프록시 기술로 만들어주고 스프링 빈으로도 등록해준다
 */
public interface SpringDataJpaItemRepository extends JpaRepository<Item, Long> {

    //상품 이름으로 조회
    //select i from Item i where i.name like ?
    List<Item> findByItemNameLike(String itemName);

    //상품 가격으로 조회
    //select i from Item i where i.price <= ?
    List<Item> findByPriceLessThanEqual(Integer price);

    //쿼리 메서드(아래 findItems 메서드와 같은 기능 수행)
    //select i from Item i where i.itemName like ? and i.price <= ?
//    List<Item> findByItemNameLikeAndPriceLessThanEqual(String itemName, Integer price);

    //쿼리 직접 실행 <- 메서드 이름으로 쿼리 실행(쿼리 메서드) 시 존재하는 단점들이 있으므로 간단한 경우에는 유용하지만, 복잡해지면 직접 JPQL 쿼리를 작성하는것이 좋다
    @Query("select i from Item i where i.itemName like :itemName and i.price <= :price")
    List<Item> findItems(@Param("itemName") String itemName, @Param("price") Integer price);

}
