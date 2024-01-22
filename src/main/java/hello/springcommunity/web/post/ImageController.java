package hello.springcommunity.web.post;

import hello.springcommunity.dto.security.UserDetailsDTO;
import hello.springcommunity.service.post.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartRequest;

import java.io.IOException;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;


    @PostMapping("/api/image/upload")
    public Map<String, Object> uploadImage(MultipartRequest request, @AuthenticationPrincipal UserDetailsDTO dto) {

        Map<String, Object> responseData = new HashMap<>();
        String dirName = "temp/" + dto.getMember().getId().toString() + "/";
        try {
            String s3Url = imageService.uploadToS3(request, dirName);
            responseData.put("uploaded", true);
            responseData.put("url", s3Url);

        } catch (IOException e) {
            responseData.put("uploaded", false);
        } catch (MultipartException e) {
            /**
             * 파일 사이즈가 넘으면 스프링 MVC에 넘어오기 전에 이미 해당 예외(MaxUploadSizeExceededException)가 터져버린다.
             * 따라서 스프링 MVC가 사용하는 @ExceptionHandler에서 해당 예외를 잡을 수 없다.
             *
             * 그래서 'spring.servlet.multipart.resolve-lazily=true' 옵션을 주면 실제 해당 파일에 접근하는 시점에 파일을 체크한다.
             * 즉, 스프링 MVC에 충분히 들어와서 @ExceptionHandler에서 해당 예외를 잡을 수 있는 시점에 체크하고 파싱한다.
             *
             * 에러 메시지는 { "error" : { "message" : "The image upload failed because the image was too big" } } 식으로 응답한다.
             */
            Map<String, Object> message = new HashMap<>();
            message.put("message","파일 크키가 커서 업로드할 수 없습니다(최대 1.0MB).");
            responseData.put("uploaded", false);
            responseData.put("error", message);
        }

        return responseData;
    }
}
