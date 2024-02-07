package hello.springcommunity.domain.notification;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String receiver; //알림을 받는 유저의 정보

    private String sender; //알림을 보낸 유저의 정보(닉네임)

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType; //알림 종류별 분류를 위한(NewComment, NewLike)

    private String content; //알람의 내용

    private String url; //해당 알림 클릭시 이동할 mapping url

    @Builder.Default
    private Boolean isRead = false; //알림 열람에 대한 여부

    private LocalDateTime createdDate;
}
