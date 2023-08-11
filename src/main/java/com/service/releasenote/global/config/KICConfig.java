package com.service.releasenote.global.config;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.service.releasenote.global.constants.KICConstants.COMPANY_DIRECTORY;
import static com.service.releasenote.global.constants.KICConstants.KIC_PREFIX;

@Slf4j
@Service
@NoArgsConstructor
@Configuration
public class KICConfig {

    @Value("${cloud.access.key-id}")
    private String accessKeyId;

    @Value("${cloud.access.secret-key}")
    private String secretKey;

    private AuthenticationTokenProvider tokenProvider;

    @PostConstruct
    public void init() {
        tokenProvider = new AuthenticationTokenProvider(accessKeyId, secretKey);
    }

    class AuthenticationTokenProvider {
        private final String tokensUrl = "https://iam.kakaoicloud-kr-gov.com/identity/v3/auth/tokens";

        private String xAuthToken;

        public AuthenticationTokenProvider(String accessKeyId, String secretKey) {
            xAuthToken = getApiAuthToken(accessKeyId, secretKey);
        }

        public String getApiAuthToken(String accessKeyId, String secretKey) {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("auth", new HashMap<String, Object>() {{
                put("identity", new HashMap<String, Object>() {{
                    put("methods", new String[] {"application_credential"});
                    put("application_credential", new HashMap<String, Object>() {{
                        put("id", accessKeyId);
                        put("secret", secretKey);
                    }});
                }});
            }});

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Object> response = restTemplate.exchange(tokensUrl, HttpMethod.POST, requestEntity, Object.class);

            return response.getHeaders().getFirst("X-Subject-Token");
        }

        public String getXAuthToken() {
            return xAuthToken;
        }
    }


    public String uploadFile(MultipartFile file, String dirName) throws IOException {
        String xAuthToken = tokenProvider.getXAuthToken();
        log.info("token1: " + xAuthToken);

        if (file == null) {
            return getDefaultCompany();
        }

        String fileName = String.valueOf(UUID.randomUUID());
        String path = KIC_PREFIX + dirName + fileName;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", xAuthToken);
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));

        InputStreamResource inputStreamResource = new InputStreamResource(file.getInputStream());
        HttpEntity<InputStreamResource> requestEntity = new HttpEntity<>(inputStreamResource, headers);

        ResponseEntity<String> response = restTemplate.exchange(path, HttpMethod.PUT, requestEntity, String.class);

        log.info("Response: " + response.getStatusCode() + " " + response.getBody());

        return path;
    }

    public void deleteObject(String path) {
        String xAuthToken = tokenProvider.getXAuthToken();
        log.info("token1: " + xAuthToken);

        if(!Objects.equals(path, getDefaultCompany())) {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Auth-Token", xAuthToken);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(path, HttpMethod.DELETE, requestEntity, String.class);

            log.info("Response: " + response.getStatusCode() + " " + response.getBody());

        }
    }

    public String getDefaultCompany() {
        return KIC_PREFIX + COMPANY_DIRECTORY + "default.png";
    }
}
