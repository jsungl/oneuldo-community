package hello.springcommunity.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/error")
public class ErrorController {

    /**
     * 인증 X
     */
    @GetMapping("/redirect")
    public String authenticationFailed(){
        return "error/redirect";
    }

    /**
     * 접근 권한 X
     */
    @GetMapping("/denied")
    public String accessDenied(){
        return "error/redirect";
    }
}
