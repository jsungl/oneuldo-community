package hello.springcommunity.dao.notification;

import hello.springcommunity.domain.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 읽지 않은 알림 최신순으로 정렬하여 조회
     */
    List<Notification> findAllByReceiverAndIsReadOrderByCreatedDateDesc(String receiver, Boolean isRead);

    /**
     * 알림 삭제
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from Notification n where n.id = :id")
    void deleteOne(@Param("id") Long id);
}
