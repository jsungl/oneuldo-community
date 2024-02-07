package hello.springcommunity.service.notification;

import hello.springcommunity.dao.notification.EmitterRepository;
import hello.springcommunity.dao.notification.NotificationRepository;
import hello.springcommunity.dao.post.PostQueryRepository;
import hello.springcommunity.domain.notification.Notification;
import hello.springcommunity.domain.notification.NotificationType;
import hello.springcommunity.domain.post.Post;
import hello.springcommunity.dto.notification.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    //60L * 1000 * 60(1시간)
    private final Long DEFAULT_TIMEOUT = 1800 * 1000L; //sse 유효시간
    private final Long RECONNECTION_TIMEOUT = 3000L; //sse 연결이 끊어진 경우 재접속 하기까지 대기 시간
    private final EmitterRepository emitterRepository;
    private final PostQueryRepository postQueryRepository;
    private final NotificationRepository notificationRepository;

    /**
     * 연결
     */
    public SseEmitter connect(String loginId) {

        // 클라이언트의 sse 연결 요청에 응답하기 위한 SseEmitter 객체 생성
        // 유효시간 지정으로 시간이 지나면 클라이언트에서 자동으로 재연결 요청함
        SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(loginId, sseEmitter);

        // SseEmitter 의 완료/시간초과/에러로 인한 전송 불가 시 sseEmitter 삭제
        sseEmitter.onCompletion(() -> {
            log.info("onCompletion callback");
            emitterRepository.deleteById(loginId);
        });
        sseEmitter.onTimeout(() -> {
            log.info("onTimeout callback");
            sseEmitter.complete();
        });
        sseEmitter.onError((e) -> {
            log.error("onError callback {}", e);
            sseEmitter.complete();
        });

        // 연결 직후, 데이터 전송이 없을 시 503 에러 발생. 에러 방지 위한 더미데이터 전송
        try {
            sseEmitter.send(SseEmitter.event()
                    .name("connect") // 해당 이벤트의 이름 지정
                    .data("connected! " + loginId));
                    //.reconnectTime(RECONNECTION_TIMEOUT)); // SSE 연결이 끊어진 경우 재접속 하기까지 대기 시간
        } catch (IOException e) {
            log.error("SSE 연결 오류 발생", e);
            sseEmitter.complete();
            //throw new RuntimeException(e);
        }

        return sseEmitter;
    }

    /**
     * 새로운 댓글 알림
     */
    @Transactional
    public void notifyNewComment(Map<String, Object> param) {
        Long postId = (Long) param.get("postId");
        Post post = postQueryRepository.findOne(postId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));
        String receiver = post.getMember().getLoginId();

        // Notification 생성 및 저장
        Notification notification = createNotification(receiver, param);
        NotificationDTO dto = NotificationDTO.builder().notification(notification).build();

        //현재 알림 개수 전송
        List<Notification> notifications = notificationRepository.findAllByReceiverAndIsReadOrderByCreatedDateDesc(receiver, false);
        int count = notifications.size();

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("sender", param.get("sender"));
        eventData.put("content", param.get("content"));
        eventData.put("dto", dto);
        eventData.put("count", count);


        // 데이터 전송
        Map<String, SseEmitter> emitters = emitterRepository.findById(receiver);
        emitters.forEach((key, emitter) -> {
            log.info("key = {}", key);
            sendToClient(emitter, "newComment", eventData);
        });

    }


    private void sendToClient(SseEmitter emitter, String name, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(name)
                    .data(data));
        } catch (IOException e) {
            log.error("SSE 연결 오류 발생", e);
            emitter.complete();
            //emitterRepository.deleteById(loginId);
            //throw new RuntimeException(e);
        }
    }

    private Notification createNotification(String receiver, Map<String, Object> param) {
        Notification notification = Notification.builder()
                .receiver(receiver)
                .sender((String) param.get("sender"))
                .content((String) param.get("content"))
                .notificationType((NotificationType) param.get("notificationType"))
                .url((String) param.get("url"))
                .createdDate(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * 읽지 않은 알림 최신순으로 정렬하여 조회
     */
    @Transactional
    public List<NotificationDTO> getAll(String loginId) {
        List<Notification> notifications = notificationRepository.findAllByReceiverAndIsReadOrderByCreatedDateDesc(loginId, false);
        List<NotificationDTO> list = new ArrayList<>();
        if(notifications == null || notifications.isEmpty()) {
            return List.of();
        } else {
            for(Notification notification : notifications) {
                NotificationDTO dto = NotificationDTO.builder().notification(notification).build();
                list.add(dto);
            }
            return list;
        }

    }

    /**
     * 알림 조회
     */
    @Transactional
    public Notification getNotification(Long id) {
        return notificationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));
    }

    /**
     * 알림 삭제
     */
    public void deleteNotification(Notification notification) {
        notificationRepository.deleteOne(notification.getId());
    }
}
