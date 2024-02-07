package hello.springcommunity.dto.notification;


import hello.springcommunity.domain.notification.Notification;
import hello.springcommunity.domain.notification.NotificationType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Getter
@NoArgsConstructor
public class NotificationDTO {

    private Long id;
    private String receiver;
    private String sender;
    private NotificationType notificationType;
    private String content;
    private String url;
    private Boolean isRead;
    private String createdDate;


    @Builder
    public NotificationDTO(Notification notification) {
        this.id = notification.getId();
        this.receiver = notification.getReceiver();
        this.sender = notification.getSender() + "님 댓글: ";
        this.notificationType = notification.getNotificationType();
        this.content = "\"" + notification.getContent() + "\"";
        this.url = notification.getUrl().split("\\?")[0] + "?notificationId=" + notification.getId() + "&" + notification.getUrl().split("\\?")[1];
        this.isRead = notification.getIsRead();
        this.createdDate = getTimeAgo(notification.getCreatedDate());

    }

    private String getTimeAgo(LocalDateTime createdAt) {

        LocalDateTime currentDateTime = LocalDateTime.now();
        Duration duration = Duration.between(createdAt, currentDateTime);

        long seconds = duration.getSeconds();
        String timeAgo = "";

        if (seconds < 60) {
            timeAgo = seconds + "초 전";
        } else if (seconds < 60 * 60) {
            long minutes = seconds / 60;
            timeAgo = minutes + "분 전";
        } else if (seconds < 60 * 60 * 24) {
            long hours = seconds / (60 * 60);
            timeAgo = hours + "시간 전";
        } else {
            long days = seconds / (60 * 60 * 24);
            timeAgo = days + "일 전";
        }

        return timeAgo;
    }

}
