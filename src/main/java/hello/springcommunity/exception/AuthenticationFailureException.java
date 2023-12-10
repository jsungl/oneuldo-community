package hello.springcommunity.exception;

import org.springframework.security.core.AuthenticationException;

public class AuthenticationFailureException extends AuthenticationException {

    public AuthenticationFailureException(String msg) {
        super(msg);
    }
}
