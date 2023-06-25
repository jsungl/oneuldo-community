package hello.springcommunity.domain.item;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
//@Commit 강제커밋
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository; //의존관계 주입

    @Autowired
    ApplicationContext applicationContext;


    @Test
    void save() {
        //given
        Item item = new Item("itemAA", 10000, 10);

        //when
        Item savedItem = itemRepository.save(item);

        //then
        Item findItem = itemRepository.findById(item.getId()).get();
        assertThat(findItem).isEqualTo(savedItem);
    }

    @Test
    void update() {
        //given
        Item item = new Item("item1", 10000, 10);
        Item savedItem = itemRepository.save(item);
        Long itemId = savedItem.getId();

        //when
        ItemUpdateDto updateParam = new ItemUpdateDto("item2", 20000, 30);
        itemRepository.update(itemId, updateParam);

        //then
        Item findItem = itemRepository.findById(itemId).get();
        assertThat(findItem.getItemName()).isEqualTo(updateParam.getItemName());
        assertThat(findItem.getPrice()).isEqualTo(updateParam.getPrice());
        assertThat(findItem.getQuantity()).isEqualTo(updateParam.getQuantity());
    }

    @Test
    void findAll() {

        //given
        Item item1 = new Item("itemA-1",10000,10);
        Item item2 = new Item("itemA-2",20000,20);
        Item item3 = new Item("itemB-1",30000,30);

        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);

        //둘 다 없음
        test(null, null, item1, item2, item3);
        test("", null, item1, item2, item3);

        //itemName
        test("itemA", null, item1, item2);
        test("temA", null, item1, item2);
        test("itemB", null, item3);

        //maxPrice
        test(null, 10000, item1);

        //둘 다 있음
        test("itemA", 10000, item1);

    }

    void test(String itemName, Integer maxPrice, Item... items) {
        List<Item> result = itemRepository.findAll(new ItemSearchCond(itemName, maxPrice));
        log.info("result={}",result);
        assertThat(result).containsExactly(items);
    }

    @Test
    @DisplayName("컨테이너에 등록된 모든 빈 조회")
    void findAllBean() {
        if(applicationContext != null) {
            String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
            for (String beanDefinitionName : beanDefinitionNames) {
                log.info("bean={}", beanDefinitionName);
//                Object bean = applicationContext.getBean(beanDefinitionName);
//                log.info("beanDefinitionName={}, bean={}", beanDefinitionName, bean);
            }
        }
    }


}