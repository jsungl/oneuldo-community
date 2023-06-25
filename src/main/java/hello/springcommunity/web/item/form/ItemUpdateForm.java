package hello.springcommunity.web.item.form;

import hello.springcommunity.domain.item.ItemType;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 폼 데이터 전달을 위한 별도의 객체 사용
 * 등록과 수정은 완전히 다른 데이터가 넘어온다. 그리고 검증로직도 많이 달라진다.
 * 그래서 다음과 같이 ItemUpdateForm 이라는 별도의 객체로 데이터를 전달받는다
 * 등록, 수정용 폼 객체를 나누면 등록, 수정이 완전히 분리되기 때문에 Bean Validation 의 groups 기능을 사용하지않아도 된다
 */
@Data
public class ItemUpdateForm {

    @NotNull
    private Long id;

    @NotBlank
    private String itemName;

    @NotNull
    @Range(min = 1000, max = 1000000)
    private Integer price;

    //수정에서는 수량은 자유롭게 변경할 수 있다.
    private Integer quantity;

    private Boolean open; //판매 여부
    private List<String> regions; //등록 지역
    private ItemType itemType; //상품 종류
    private String deliveryCode; //배송 방식
}
