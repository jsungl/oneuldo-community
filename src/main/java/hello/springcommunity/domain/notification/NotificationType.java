package hello.springcommunity.domain.notification;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    NEW_COMMENT("댓글"), NEW_LIKE("추천");

    private final String value;
}
