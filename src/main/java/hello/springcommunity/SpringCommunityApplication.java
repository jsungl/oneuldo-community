package hello.springcommunity;

import hello.springcommunity.domain.item.ItemRepository;
import hello.springcommunity.domain.member.MemberRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class SpringCommunityApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCommunityApplication.class, args);
	}

//	@Bean
//	@Profile("local")
	public TestDataInit testDataInit(MemberRepository memberRepository, ItemRepository itemRepository) {
		return new TestDataInit(memberRepository, itemRepository);
	}

}
