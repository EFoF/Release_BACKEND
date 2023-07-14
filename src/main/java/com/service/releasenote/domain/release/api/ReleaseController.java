package com.service.releasenote.domain.release.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "태그 지정 시 무시됨", tags = {"swagger", "v1", "api"})
@Slf4j
@RestController
@RequiredArgsConstructor
public class ReleaseController {

    @ApiOperation(value="this is a test!", notes = "this is a note")
    @GetMapping("/test/swagger")
    public ResponseEntity<String> swaggerTest() {
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }
}
