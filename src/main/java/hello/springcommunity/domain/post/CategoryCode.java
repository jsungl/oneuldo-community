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

    FREE("자유"), HUMOR("유머"), MYSTERY("미스터리/공포"),
    DIGITAL("디지털"), FOOTBALL_WORLD("해외축구"), NOTICE("공지");

    //CHAT("잡담")

    private final String displayName;

}
