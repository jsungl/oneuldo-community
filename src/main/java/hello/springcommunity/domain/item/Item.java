package hello.springcommunity.domain.item;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity //JPA 가 사용하는 객체. 이것이 있어야 JPA가 인식할 수 있다
public class Item {

    //테이블의 PK와 해당 필드(id)를 매핑
    //PK 생성값을 데이터베이스에서 생성하는 IDENTITY 방식을 사용한다 (auto increment)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //객체 필드(itemName)를 테이블의 컬럼(item_name)과 매핑, JPA 매핑정보로 컬럼의 길이 값이 활용된다
    //스프링부트와 통합해서 사용하면 필드 이름을 테이블 컬럼명으로 변경할 때 객체 필드의 카멜 케이스를 테이블 컬럼의 언더스코어로 자동 변환해주므로
    //@Column 에노테이션은 생략가능하다
    @Column(name = "item_name", length = 10)
    private String itemName;

    private Integer price;
    private Integer quantity;

//    private Boolean open; //판매 여부
//    private List<String> regions; //등록 지역
//    private ItemType itemType; //상품 종류
//    private String deliveryCode; //배송 방식

    //JPA 는 public 또는 protected 의 기본 생성자가 필수이다
    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
