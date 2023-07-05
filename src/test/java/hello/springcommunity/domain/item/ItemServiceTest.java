package hello.springcommunity.domain.item;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;


@Slf4j
@SpringBootTest
class ItemServiceTest {

    @Autowired
    ItemRepositoryV2 itemRepositoryV2;
    @Autowired
    ItemService itemService;

    @Test
    void save() {
        //given
        Item item = new Item("itemAA", 100, 10);

        //when
        Item savedItem = itemService.save(item);

        //then
        Item findItem = itemService.findById(item.getId()).get();
        assertThat(findItem).isEqualTo(savedItem);

//        Optional<Item> itemOptional = itemService.findById(item.getId());
//        assertThat(itemOptional.isEmpty()).isTrue();

    }

    @Test
    void update() {
        //given
        Item item = new Item("itemBB", 10000, 10);
        Item savedItem = itemService.save(item);
        Long itemId = savedItem.getId();

        //when
        ItemUpdateDto updateParam = new ItemUpdateDto("itemCC", 100, 30);
        itemService.update(itemId, updateParam);
//        assertThatThrownBy(()->itemService.update(itemId, updateParam)).isInstanceOf(RuntimeException.class);

        //then
        Item findItem = itemService.findById(itemId).get();
        assertThat(findItem.getItemName()).isEqualTo(updateParam.getItemName());
        assertThat(findItem.getPrice()).isEqualTo(updateParam.getPrice());
        assertThat(findItem.getQuantity()).isEqualTo(updateParam.getQuantity());
    }

}