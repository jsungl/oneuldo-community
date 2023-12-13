package hello.springcommunity.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.NoSuchElementException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler{

    //RuntimeException 발생
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected void handleRuntimeEx(RuntimeException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("[handleRuntimeEx 실행]");
        String requestURI = request.getRequestURI();
        log.info("requestURI={}", requestURI);

        request.getSession().setAttribute("errorMessage", "서버내부에서 오류가 발생하였습니다.");
        response.sendRedirect("/");

    }

    //Exception 발생
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(IllegalArgumentException e, HttpServletRequest request) {
        log.info("[handleException 실행]");
        String requestURI = request.getRequestURI();
        log.info("requestURI={}", requestURI);

        return "error/500";
    }


    //유저가 존재하지 않을 때 발생
    /*
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUsernameNotFoundEx(UsernameNotFoundException e, HttpServletRequest request) {
        log.info("[handleUsernameNotFoundEx 실행]");
        String requestURI = request.getRequestURI();
        log.info("requestURI={}", requestURI);

        return "error/404";
    }
    */

    //요구한 원소가 없을 때 발생
    /*
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoSuchElementFoundEx(NoSuchElementException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("[handleNoSuchElementFoundEx 실행]");
        String requestURI = request.getRequestURI();
        log.info("requestURI={}", requestURI);

        return "error/404";

    }
     */

    //메소드에 부적절한 인수를 전달할 때 발생
    /*
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentEx(IllegalArgumentException e, HttpServletRequest request) {
        log.info("[handleIllegalArgumentEx 실행]");
        String requestURI = request.getRequestURI();
        log.info("requestURI={}", requestURI);

        return "error/400";
    }
     */

    /*
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected String handleRuntimeEx(RuntimeException e, HttpServletRequest request) throws IOException {
        log.info("[handleRuntimeEx 실행]");
        String requestURI = request.getRequestURI();
        log.info("requestURI={}", requestURI);
        return "error/500";
    }
     */


}
