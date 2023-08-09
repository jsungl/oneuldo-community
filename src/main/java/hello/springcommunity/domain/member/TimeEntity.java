package hello.springcommunity.domain.member;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class TimeEntity {

    @Column(name = "created_date")
    @CreatedDate
    private LocalDate createdDate;

    @Column(name = "modified_date")
    @LastModifiedDate
    private LocalDate modifiedDate;

    /**
     * 해당 엔티티를 저장하기 전에 실행
     */
    @PrePersist
    public void onPrePersist() {
        this.modifiedDate = this.createdDate;
    }

    /**
     * 해당 엔티티를 수정하기 전에 실행
     */
//    @PreUpdate
//    public void onPreUpdate() {}

}
