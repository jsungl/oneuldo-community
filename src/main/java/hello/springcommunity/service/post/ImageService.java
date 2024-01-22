package hello.springcommunity.service.post;

import com.amazonaws.services.s3.model.*;
import hello.springcommunity.config.aws.AwsS3Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageService {

    private final AwsS3Config awsS3Config;

    private String localLocation = "C:\\Users\\suver21\\Desktop\\spring\\s3-image\\";

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    public String uploadImageToS3(MultipartRequest request) throws IOException {

        //MultipartRequest 에서 이미지 파일 뽑아냄
        //ckeditor 에서 파일을 보낼 때 upload : [파일] 형식으로 해서 넘어오기 때문에 upload라는 키의 밸류를 받는다
        MultipartFile file = request.getFile("upload");

        //뽑아낸 이미지 파일에서 이름 및 확장자 추출
        String fileName = file.getOriginalFilename();
        String ext = fileName.substring(fileName.indexOf("."));

        //이미지 파일 이름의 중복 방지를 위해 uuid 생성
        String uuidFileName = UUID.randomUUID() + ext;
        
        //서버환경(로컬)에 저장할 경로 생성
        String localPath = localLocation + uuidFileName;
        
        //서버환경(로컬)에 이미지 파일 저장
        File localFile = new File(localPath);
        file.transferTo(localFile);

        //s3에 이미지 파일 업로드 후 이미지 경로 반환
        //bucket과 파일명, File을 이용하는 방식
        awsS3Config.amazonS3Client().putObject(new PutObjectRequest(bucket, uuidFileName, localFile).withCannedAcl(CannedAccessControlList.PublicRead));
        String s3Url = awsS3Config.amazonS3Client().getUrl(bucket, uuidFileName).toString();
        log.info("s3Url={}", s3Url);

        //서버환경(로컬)에 저장한 이미지 파일 삭제
        if(localFile.delete()) {
            log.info("파일 삭제 성공");
        } else {
            log.info("파일 삭제 실패");
        }

        return s3Url;

    }


    public String uploadToS3(MultipartRequest request, String dirName) throws IOException {

        //MultipartRequest 에서 이미지 파일 뽑아냄
        //ckeditor 에서 파일을 보낼 때 upload : [파일] 형식으로 해서 넘어오기 때문에 upload라는 키의 밸류를 받는다
        MultipartFile file = request.getFile("upload");

        //뽑아낸 이미지 파일에서 이름 및 확장자 추출
        String fileName = file.getOriginalFilename();
        String ext = fileName.substring(fileName.indexOf("."));

        //이미지 파일 이름의 중복 방지를 위해 uuid 생성
        String uuidFileName = UUID.randomUUID() + ext;
        uuidFileName = dirName + uuidFileName;

        //metadata contentType,contentLength 를 설정
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());

        //파일의 내용을 읽기 위한 InputStream 반환
        InputStream inputStream = file.getInputStream();

        //s3에 이미지 파일 업로드 후 이미지 경로 반환
        //bucket과 파일명, InputStream, 파일의 메타데이터를 이용하는 방식
        awsS3Config.amazonS3Client().putObject(new PutObjectRequest(bucket, uuidFileName, inputStream, objectMetadata).withCannedAcl(CannedAccessControlList.PublicRead));
        String s3Url = awsS3Config.amazonS3Client().getUrl(bucket, uuidFileName).toString();
        //log.info("s3Url={}", s3Url);

        return s3Url;

    }

    /**
     * 1. 임시폴더에서 저장폴더로 이미지 복사
     * 2. 임시폴더 이미지는 삭제
     */
    public void update(String oldSource, String newSource) {

        try {
            oldSource = URLDecoder.decode(oldSource, "UTF-8");
            newSource = URLDecoder.decode(newSource, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        //log.info("oldSource={}", oldSource);
        //log.info("newSource={}", newSource);
        
        moveS3(oldSource, newSource);
        deleteS3(oldSource);
    }

    /**
     * 삭제된 이미지 S3에서 삭제
     * @param source
     */
    public void delete(String source) {
        try {
            source = URLDecoder.decode(source, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        deleteS3(source);
    }

    private void deleteS3(String oldSource) {
        awsS3Config.amazonS3Client().deleteObject(bucket,oldSource);
    }

    private void moveS3(String oldSource, String newSource) {
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(bucket, oldSource, bucket, newSource);
        copyObjectRequest.setCannedAccessControlList(CannedAccessControlList.PublicRead);
        awsS3Config.amazonS3Client().copyObject(copyObjectRequest);
    }
}
