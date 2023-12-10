package hello.springcommunity.domain;

import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;


@Getter
@MappedSuperclass //공통 매핑 정보가 필요할 때, 부모 클래스에 선언하고 속성만 상속 받아서 사용하고 싶을 때 사용한다. (DB 테이블과는 상관없다)
public abstract class TimeEntity {

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    /**
     * 해당 엔티티를 저장하기 전에 실행
     */
    @PrePersist
    public void onPrePersist() {
        this.createdDate = LocalDateTime.now();
        this.modifiedDate = this.createdDate;
    }

    /**
     * 해당 엔티티를 수정하기 전에 실행
     */
    public void onPreUpdate() {
        this.modifiedDate = LocalDateTime.now();
    }

}
