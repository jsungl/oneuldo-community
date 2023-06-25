package hello.springcommunity.domain.item;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 배송방식
 * FAST: 빠른 배송
 * NORMAL: 일반 배송
 * SLOW: 느린 배송
 */
@Data
@AllArgsConstructor
public class DeliveryCode {
    private String code; //FAST, NORMAL, SLOW 와 같이 시스템에서 전달하는 값
    private String displayName; //빠른배송, 일반배송, 느린배송 과 같이 사용자에게 보여주는 값
}
