package hello.springcommunity.web;

import hello.springcommunity.common.SessionConst;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.dto.security.UserDetailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class HomeController {


    //@GetMapping("/")
    public String homeLoginV1(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember, Model model) {

        //로그인
        //세션에 회원 데이터가 없으면 비로그인 사용자 -> 기존 home 으로 이동
        if (loginMember == null) {
            return "home";
        }

        //세션에 회원 데이터가 있으면 로그인 사용자 -> loginhome 으로 이동
        model.addAttribute("member", loginMember);
        return "loginHome";
    }

    @GetMapping("/")
    public String homeLogin(Authentication authentication, @AuthenticationPrincipal UserDetailsDTO dto) {

        /**
         * Authentication : 인증객체
         * 2가지 용도로 사용된다. 1.인증 용도 또는 2.인증 후 세션에 담기 위한 용도
         *
         * 1. 인증시 id와 password 를 담고 인증 검증을 위해 전달되어 사용된다
         * 2. 인증 후 최종 인증 결과(user 객체, 권한 정보)를 담고 SecurityContext 에 저장되어 다음과 같은 코드로 전역적으로 참조가 가능하다
         * - Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         *
         * Authentication 구조
         * - Principal : 사용자 아이디 또는 User 객체
         * - Credenticals : 사용자 비밀번호
         * - authorities : 인증된 사용자의 권한 목록
         * - details : 인증 부과 정보(IP, 세션정보, 기타 인증 요청에서 사용했던 정보들)
         * - authenticated : 인증여부
         */

        if(authentication == null) {
            return "home";
        }

        //인증 객체에 저장된 사용자의 이름(ID)
//        log.info("authentication name={}", authentication.getName());
//        log.info("authentication details={}", authentication.getDetails());
//
//        List<String> collect = authentication.getAuthorities().stream().map(a -> a.getAuthority())
//                .collect(Collectors.toList());
//        for (String s : collect) {
//            //ROLE_USER, ROLE_ADMIN
//            log.info("authentication authority={}", s);
//        }
//
//        log.info("authentication authenticated={}", authentication.isAuthenticated());
//        log.info("authentication principal={}", authentication.getPrincipal());
//
        UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
//        log.info("authentication user loginId={}", userDetailsDTO.getUsername());
//        log.info("authentication user nickname={}", userDetailsDTO.getMember().getNickname());
//        log.info("authentication user role={}", userDetailsDTO.getMember().getRoleValue());

        /**
         * Spring Security 3.2 부터는 @AuthenticationPrincipal 어노테이션을 이용하여
         * UserDetails를 구현하여 만든 인스턴스(현재 로그인한 사용자 객체)를 가져올 수 있다
         */
//        log.info("authentication user loginId={}", dto.getUsername());
//        log.info("authentication user nickname={}", dto.getUser().getNickname());
//        log.info("authentication user role={}", dto.getUser().getRoleValue());


        if(userDetailsDTO.getMember().getRoleValue().equals("ROLE_ADMIN")) {
            return "admin/info";
        }

        return "home";
    }





}
