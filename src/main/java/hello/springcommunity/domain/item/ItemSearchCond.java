package hello.springcommunity.domain.item;

import lombok.Data;

/**
 * 검색조건으로 사용 -> 상품명, 최대 가격
 *
 */
@Data
public class ItemSearchCond {

    private String itemName;
    private Integer maxPrice;

    public ItemSearchCond() {
    }

    public ItemSearchCond(String itemName, Integer maxPrice) {
        this.itemName = itemName;
        this.maxPrice = maxPrice;
    }
}
