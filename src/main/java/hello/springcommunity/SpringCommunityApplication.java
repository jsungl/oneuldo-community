package hello.springcommunity;

import hello.springcommunity.dao.member.MemberRepositoryOld;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @EnableJpaAuditing
 * - Auditing이라는 기능을 사용해 엔티티가 생성되고, 변경되는 그 시점을 감지하여 생성시각, 수정시각, 생성한 사람, 수정한 사람을 기록할 수 있다.
 * - Application 클래스에 붙이거나, @Configuration 어노테이션이 사용된 클래스에 붙이면 된다.
 *
 * @EntityListeners(AuditingEntityListener.class)
 * - Auditing을 적용할 엔티티 클래스에 @EntityListeners 어노테이션을 적용한다.
 * - 해당 어노테이션은 엔티티의 변화를 감지하여 엔티티와 매핑된 테이블의 데이터를 조작한다.
 * - 이 어노테이션의 파라미터에 이벤트 리스너를 넣어줘야하는데, 여기에 AuditingEntityListener 클래스를 넣어준다.
 * - AuditingEntityListener 클래스는 Spring Data JPA에서 제공하는 이벤트 리스너로 엔티티의 영속, 수정 이벤트를 감지하는 역할을 한다.
 */


@SpringBootApplication //@EnableJpaAuditing
public class SpringCommunityApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCommunityApplication.class, args);
	}

//	@Bean
//	@Profile("local")
	public TestDataInit testDataInit(MemberRepositoryOld memberRepositoryOld) {
		return new TestDataInit(memberRepositoryOld);
	}

}
