package hello.springcommunity.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc // MockMvc 인스턴스를 자동으로 구성합니다.
class MyControllerTest {

    @Autowired
    private MockMvc mockMvc; // Spring MVC 컨트롤러 테스트를 위한 MockMvc 인스턴스


    // 각 테스트 전에 실행할 설정 작업을 수행합니다.
    @BeforeEach
    public void setup() {
        // 여기서 필요한 초기화 작업을 수행할 수 있습니다.
    }

    @WithMockUser(roles = "ADMIN") //인증된 모의 사용자를 만들어서 사용, 이 어노테이션으로 인해 ROLE_USER 권한을 가진 사용자가 API를 요청하는 것과 같은 효과를 냄
    @Test
    @DisplayName("관리자만 접근 가능")
    public void returnProfile() throws Exception {
        // GET 요청을 보내고 응답을 검증합니다.
        mockMvc.perform(MockMvcRequestBuilders.get("/adminProfile"))
                .andExpect(MockMvcResultMatchers.status().isOk()) // HTTP 상태 코드가 200이여야 합니다.
                .andExpect(MockMvcResultMatchers.content().string("adminProfile")); // 응답 본문의 내용을 검증합니다.
    }


    @Test
    @DisplayName("로그인 성공 확인")
    public void testLoginSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/login/process")
                        .param("loginId", "test")
                        .param("password", "test123!")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/"));
    }

    @Test
    @DisplayName("로그인 실패 확인")
    public void testLoginFailure() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/login/process")
                        .param("loginId", "test")
                        .param("password", "test234")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login?error=true"));
    }
}