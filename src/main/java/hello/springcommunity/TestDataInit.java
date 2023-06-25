package hello.springcommunity;

import hello.springcommunity.domain.item.Item;
import hello.springcommunity.domain.item.ItemRepository;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 테스트용 초기 데이터 추가
 */
@Slf4j
@RequiredArgsConstructor
public class TestDataInit {

    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * 스프링 컨테이너가 완전히 초기화를 다 끝내고, 실행준비가 되었을 때 발생하는 이벤트
     * 스프링이 이 시점에 해당 에노테이션이 붙은 메서드를 호출해준다
     *
     * @EventListener 대신 @PostConstruct 를 사용할 경우 AOP 같은 부분이 아직 다 처리되지 않은 시점에 호출될 수 있기 때문에
     * 간혹 문제가 발생할 수 있다. 예를들어 @Transactional 과 관련된 AOP 가 적용되지 않은 상태로 호출될 수 있다
     *
     * @EventListener를 통해 이벤트를 처리해주기 위해서는 해당 에노테이션이 붙은 메서드를 가진 클래스가 스프링 빈으로 등록되어야 한다
     */
//    @PostConstruct
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("test data init~!");
//        itemRepository.save(new Item("itemA", 10000, 10));
//        itemRepository.save(new Item("itemB", 20000, 20));
//
//        Member member = new Member();
//        member.setLoginId("test");
//        member.setPassword("test!");
//        member.setName("테스터");
//        memberRepository.save(member);
    }
    
}
