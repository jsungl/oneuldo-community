package hello.springcommunity.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@AutoConfigureMockMvc
class GlobalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // 각 테스트 전에 실행할 설정 작업을 수행합니다.
    @BeforeEach
    public void setup() {
        // 여기서 필요한 초기화 작업을 수행할 수 있습니다.
    }



    @Test
    @DisplayName("회원가입")
    @Transactional //테스트 완료후 롤백
    public void testRegistration() throws Exception {
        String loginId = "testuser";
        String password = "test123!";
        String nickname = "테스트용유저";
        String email = "sasil33932@fkcod.com";

        mockMvc.perform(MockMvcRequestBuilders.post("/signup")
                        .param("loginId", loginId)
                        .param("password", password)
                        .param("rePassword", password)
                        .param("nickname", nickname)
                        .param("email", email)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())) // CSRF 토큰 추가
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login")); // 회원가입 후 로그인 페이지로 리다이렉션되는지 확인
    }

}