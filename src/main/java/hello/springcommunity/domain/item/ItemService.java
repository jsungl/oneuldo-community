package hello.springcommunity.domain.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 스프링 데이터 JPA 기능을 제공하는 리포지토리(ItemRepositoryV2) 와 Querydsl 사용해서 복잡한 쿼리 기능을 제공하는 리포지토리(ItemQueryRepository) 둘로 분리
 * 기본 CRUD 와 단순조회는 스프링 데이터 JPA 로 처리
 * 복잡한 조회 쿼리는 Querydsl 로 처리
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {

//    private final ItemRepository itemRepository;
    private final ItemRepositoryV2 itemRepositoryV2;
    private final ItemQueryRepository itemQueryRepository;

    public Item save(Item item) {
//        return itemRepository.save(item);
        return itemRepositoryV2.save(item); //스프링 데이터 JPA 가 제공하는 save() 호출
    }

    public void update(Long itemId, ItemUpdateDto updateParam) {
//        itemRepository.update(itemId, updateParam);
        Item findItem = findById(itemId).orElseThrow();
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    public Optional<Item> findById(Long id) {
//        return itemRepository.findById(id);
        return itemRepositoryV2.findById(id); //스프링 데이터 JPA 가 제공하는 findById() 메서드를 사용해서 엔티티를 찾는다
    }

    public List<Item> findItems(ItemSearchCond cond) {
//        return itemRepository.findAll(cond);
        return itemQueryRepository.findAll(cond); //복잡한 쿼리는 Querydsl 을 사용하는 itemQueryRepository 에서 처리
    }
}
