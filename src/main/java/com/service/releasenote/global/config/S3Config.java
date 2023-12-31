package com.service.releasenote.global.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static com.service.releasenote.global.constants.S3Constants.*;


@Slf4j
@Service
@NoArgsConstructor
@Configuration
public class S3Config {

    private AmazonS3 s3Client;

    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @PostConstruct
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(this.region)
                .build();
    }

    public String upload(MultipartFile file, String dirName) throws IOException {
        log.info("file: {}", file);

        if (file == null) {
            return getDefaultUrl();
        }

        String fileName = String.valueOf(UUID.randomUUID());

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getBytes().length);
        s3Client.putObject(new PutObjectRequest(bucket + "/" + dirName, fileName, file.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        return String.valueOf(s3Client.getUrl(bucket + "/" + dirName, fileName));
    }

    public void delete(String fileUrl, String dirName) {
        if (!Objects.equals(fileUrl, getDefaultUrl())) {
            String fileName = fileUrl.replace(S3PREFIX, "");
            fileName = fileName.replace(dirName + "/", "");
            log.info(fileName);
            s3Client.deleteObject(bucket + "/" + dirName, fileName);
        }
    }

    public String getDefaultUrl() {
        return s3Client.getUrl(bucket + "/" + COMPANY_DIRECTORY, "default").toString();
    }
}
