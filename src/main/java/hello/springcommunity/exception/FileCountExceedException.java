package hello.springcommunity.exception;

import org.springframework.web.multipart.MultipartException;

public class FileCountExceedException extends MultipartException {

    public FileCountExceedException(String msg) {
        super(msg);
    }

    public FileCountExceedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
