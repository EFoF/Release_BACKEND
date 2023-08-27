package com.service.releasenote.domain.company.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.releasenote.domain.company.application.CompanyService;
import com.service.releasenote.domain.release.api.ReleaseController;
import com.service.releasenote.global.log.CommonLog;
import com.service.releasenote.global.util.SecurityUtil;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

import static com.service.releasenote.domain.company.dto.CompanyDTO.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"company"})
public class CompanyController {

    private final CompanyService companyService;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/companies")
    public Long createCompany (
            @RequestPart(value="image", required = false) MultipartFile image,
            @RequestPart(value="name") String name) throws IOException {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Long companyId = companyService.createCompany(image, name, currentMemberId);
        CommonLog commonLog = new CommonLog(name + " 회사가 생성됨", "Post", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));
        // TODO: 반환 데이터 협의
        return companyId;
    }

    @GetMapping("/api/companies")
    public Page<CompanyResponseDTO> searchCompany(@RequestParam(value = "search", required = false, defaultValue = "") String name, Pageable pageable) throws JsonProcessingException {
        // 회사 이름이 비어있는 경우는 고려하지 않음
        Page<CompanyResponseDTO> companyList = companyService.findCompaniesByName(name, pageable);
        CommonLog commonLog = new CommonLog(name + " 회사가 검색됨", "Get", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));
        // TODO: 데이터 없는 경우 반환 데이터 협의
        return companyList;
    }

    // TODO: API 수정
    @GetMapping(value = "/api/companies/member/companies")
    public Page<CompanyResponseDTO> findCompanyByMemberId(Pageable pageable) throws JsonProcessingException {
        // TODO: MemberCompany 고려
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Page<CompanyResponseDTO> companyList = companyService.findCompaniesByMemberId(pageable, currentMemberId);
        CommonLog commonLog = new CommonLog( "개발자 - "+ " 회사가 검색됨", "Get", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));
        // TODO: 반환 데이터
        return companyList;
    }

    @PutMapping("/api/companies/{company_id}")
    public UpdateCompanyResponseDTO updateCompany(@PathVariable Long company_id,
                                                  @RequestPart(value="image", required = false) MultipartFile image,
                                                  @RequestPart(value="name", required = false) String name) throws IOException {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        UpdateCompanyResponseDTO updateCompany = companyService.updateCompany(company_id, image, name, currentMemberId);
        CommonLog commonLog = new CommonLog(name + " 회사가 수정됨", "Put", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));

        // TODO: 반환 데이터 협의
        return updateCompany;
    }

    @DeleteMapping("/api/companies/{company_id}")
    public Long deleteCompany(@PathVariable Long company_id) throws JsonProcessingException {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Long deleteCompanyId = companyService.deleteCompany(company_id, currentMemberId);
        CommonLog commonLog = new CommonLog( company_id+ "번 회사가 삭제됨", "Get", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));

        // TODO: 반환 데이터 협의
        return deleteCompanyId;
    }
}
