package hello.springcommunity.domain.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.*;

@Slf4j
@Repository
@Transactional
public class ItemRepository {

    private static final Map<Long, Item> store = new HashMap<>(); //static
    private static long sequence = 0L; //static

//    private final EntityManager em;

//    public ItemRepository(EntityManager em) {
//        this.em = em;
//    }

    private final SpringDataJpaItemRepository repository; //SpringDataJpaItemRepository 를 사용

    @Autowired
    public ItemRepository(SpringDataJpaItemRepository repository) {
        this.repository = repository;
    }

//    public Item save(Item item) {
//        item.setId(++sequence);
//        store.put(item.getId(), item);
//        return item;
//    }

//    public Item save(Item item) {
//        em.persist(item); //JPA에서 객체를 저장할 때 EntityManager 가 제공하는 persist() 메서드를 사용한다
//        return item;
//    }

    public Item save(Item item) {
        return repository.save(item);
    }


//    -----------------------------------------------------------------------------------------------------


//    public Item findById(Long id) {
//        return store.get(id);
//    }

//    public Optional<Item> findById(Long id) {
//        return Optional.ofNullable(store.get(id));
//    }

//    public Optional<Item> findById(Long id) {
//        Item item = em.find(Item.class, id);//JPA에서 엔티티 객체를 PK를 기준으로 조회할 때 find() 메서드를 사용하고 조회타입과 PK 타입을 주면 된다
//        return Optional.ofNullable(item);
//    }

      public Optional<Item> findById(Long id) {
        return repository.findById(id);
      }

//    -----------------------------------------------------------------------------------------------------

//    public List<Item> findAll() {
//        return new ArrayList<>(store.values());
//    }

    /**
     * ItemSearchCond 라는 검색 조건을 받아서 내부에서 데이터를 검색한다
     * 자바 스트림을 사용
     *
     * itemName 이나 maxPrice 가 null 이거나 비었으면 해당 조건을 무시한다
     * itemName 이나 maxPrice 에 값이 있을 때만 해당 조건으로 필터링 기능을 수행한다
     */
//    public List<Item> findAll(ItemSearchCond cond) {
//        String itemName = cond.getItemName();
//        Integer maxPrice = cond.getMaxPrice();
//
//        return store.values().stream()
//                .filter(item -> {
//                    if(ObjectUtils.isEmpty(itemName)) {
//                        return true;
//                    }
//                    return item.getItemName().contains(itemName);
//                })
//                .filter(item -> {
//                    if(maxPrice == null) {
//                        return true;
//                    }
//                    return item.getPrice() <= maxPrice;
//                })
//                .collect(Collectors.toList());
//    }

    /**
     * JPA에서 단순히 PK 를 기준으로 조회하는것이 아닌, 여러 데이터를 복잡한 조건으로 데이터 조회
     * JPA는 JPQL 이라는 객체지향 쿼리 언어를 제공한다
     * 주로 여러 데이터를 복잡한 조건으로 조회할 때 사용
     *
     */
//    public List<Item> findAll(ItemSearchCond cond) {
//
//        String jpql = "select i from Item i";
//
//        String itemName = cond.getItemName();
//        Integer maxPrice = cond.getMaxPrice();
//
//        //동적쿼리
//        //모든 데이터조회, 이름 조회, 가격 조회, 이름 + 가격 조회
//        if (StringUtils.hasText(itemName) || maxPrice != null) {
//            jpql += " where";
//        }
//        boolean andFlag = false;
//        if (StringUtils.hasText(itemName)) {
//            jpql += " i.itemName like concat('%',:itemName,'%')";
//            andFlag = true;
//        }
//        if (maxPrice != null) {
//            if (andFlag) {
//                jpql += " and";
//            }
//            jpql += " i.price <= :maxPrice";
//        }
//        log.info("jpql={}", jpql);
//
//        TypedQuery<Item> query = em.createQuery(jpql, Item.class);
//        if (StringUtils.hasText(itemName)) {
//            query.setParameter("itemName", itemName);
//        }
//        if (maxPrice != null) {
//            query.setParameter("maxPrice", maxPrice);
//        }
//        return query.getResultList();
//    }

      public List<Item> findAll(ItemSearchCond cond) {

          String itemName = cond.getItemName();
          Integer maxPrice = cond.getMaxPrice();

          if(StringUtils.hasText(itemName) && maxPrice != null) {
//              return repository.findByItemNameLikeAndPriceLessThanEqual("%" + itemName + "%", maxPrice);
               return repository.findItems("%" + itemName + "%", maxPrice);
          } else if (StringUtils.hasText(itemName)) {
              return repository.findByItemNameLike("%" + itemName + "%");
          } else if (maxPrice != null) {
              return repository.findByPriceLessThanEqual(maxPrice);
          } else {
              return repository.findAll();
          }
      }



//    -----------------------------------------------------------------------------------------------------


//    public void update(Long itemId, Item updateParam) {
//        Item findItem = findById(itemId).orElseThrow();
//        findItem.setItemName(updateParam.getItemName());
//        findItem.setPrice(updateParam.getPrice());
//        findItem.setQuantity(updateParam.getQuantity());
//        findItem.setOpen(updateParam.getOpen());
//        findItem.setRegions(updateParam.getRegions());
//        findItem.setItemType(updateParam.getItemType());
//        findItem.setDeliveryCode(updateParam.getDeliveryCode());
//    }

//    public void update(Long itemId, ItemUpdateDto updateParam) {
//        Item findItem = findById(itemId).orElseThrow();
//        findItem.setItemName(updateParam.getItemName());
//        findItem.setPrice(updateParam.getPrice());
//        findItem.setQuantity(updateParam.getQuantity());
//        findItem.setOpen(updateParam.getOpen());
//        findItem.setRegions(updateParam.getRegions());
//        findItem.setItemType(updateParam.getItemType());
//        findItem.setDeliveryCode(updateParam.getDeliveryCode());
//    }

//    public void update(Long itemId, ItemUpdateDto updateParam) {
//        Item findItem = em.find(Item.class, itemId);
//        findItem.setItemName(updateParam.getItemName());
//        findItem.setPrice(updateParam.getPrice());
//        findItem.setQuantity(updateParam.getQuantity());
////        findItem.setOpen(updateParam.getOpen());
////        findItem.setRegions(updateParam.getRegions());
////        findItem.setItemType(updateParam.getItemType());
////        findItem.setDeliveryCode(updateParam.getDeliveryCode());
//    }

      public void update(Long itemId, ItemUpdateDto updateParam) {
          Item findItem = repository.findById(itemId).orElseThrow(); //스프링 데이터 JPA 가 제공하는 findById 메서드를 사용해서 엔티티를 찾는다
          findItem.setItemName(updateParam.getItemName());
          findItem.setPrice(updateParam.getPrice());
          findItem.setQuantity(updateParam.getQuantity());
      }


    public void clearStore() {
        store.clear();
    }
}
