package hello.springcommunity.dao.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EmitterRepository {

    // thread-safe 한 컬렉션 객체로 sse emitter 객체를 관리해야 한다
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 저장
    public SseEmitter save(String loginId, SseEmitter emitter) {

        String id = loginId + "_" + System.currentTimeMillis();

        // 재연결일 경우를 대비해 삭제
        deleteById(loginId);

        emitters.put(id, emitter);

        log.info("emitters size={}", emitters.entrySet().size());
        emitters.entrySet().forEach(entry -> log.info("emitter = {}", entry.getKey()));

        return emitter;
    }

    // 삭제
    public void deleteById(String loginId) {
        emitters.forEach((key, emitter) -> {
            if(key.split("_")[0].equals(loginId)) emitters.remove(key);
        });
        log.info("emitters size={}", emitters.entrySet().size());
    }

    // 조회
    // 로그인 한 유저의 SseEmitter 모두 가져오기
    public Map<String, SseEmitter> findById(String loginId) {
        return emitters.entrySet().stream()
                .filter(entry -> entry.getKey().split("_")[0].equals(loginId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
