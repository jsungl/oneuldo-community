package hello.springcommunity;

import hello.springcommunity.domain.member.MemberRepositoryOld;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
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
