package hello.springcommunity.domain.post;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 게시물 카테고리
 * CHAT : 잡담,일반
 * HUMOR : 유머
 * NOTICE : 공지
 */
@Getter
@RequiredArgsConstructor
public enum CategoryCode {

    CHAT("잡담"), HUMOR("유머"), NOTICE("공지");

    private final String displayName;

}
