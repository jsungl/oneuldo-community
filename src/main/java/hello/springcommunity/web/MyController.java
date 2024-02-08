package hello.springcommunity.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 테스트용 컨트롤러
 */
//@RestController
//@RequestMapping("/test")
public class MyController {


    //@GetMapping("/adminProfile")
    public String adminProfile() {
        return "adminProfile";
    }
}
