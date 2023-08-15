package com.service.releasenote.domain.company.api;

import com.service.releasenote.domain.company.application.CompanyService;
import com.service.releasenote.global.util.SecurityUtil;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.service.releasenote.domain.company.dto.CompanyDTO.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"company"})
public class CompanyController {

    private final CompanyService companyService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/companies")
    public Long createCompany (
            @RequestPart(value="image", required = false) MultipartFile image,
            @RequestPart(value="name") String name) throws IOException {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Long companyId = companyService.createCompany(image, name, currentMemberId);

        // TODO: 반환 데이터 협의
        return companyId;
    }

    @GetMapping("/api/companies")
    public Page<CompanyResponseDTO> searchCompany(@RequestParam(value = "search", required = false, defaultValue = "") String name, Pageable pageable) {
        // 회사 이름이 비어있는 경우는 고려하지 않음
        Page<CompanyResponseDTO> companyList = companyService.findCompaniesByName(name, pageable);

        // TODO: 데이터 없는 경우 반환 데이터 협의
        return companyList;
    }

    // TODO: API 수정
    @GetMapping(value = "/api/companies/member/companies")
    public Page<CompanyResponseDTO> findCompanyByMemberId(Pageable pageable) {
        // TODO: MemberCompany 고려
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Page<CompanyResponseDTO> companyList = companyService.findCompaniesByMemberId(pageable, currentMemberId);

        // TODO: 반환 데이터
        return companyList;
    }

    @PutMapping("/api/companies/{company_id}")
    public UpdateCompanyResponseDTO updateCompany(@PathVariable Long company_id,
                                                  @RequestPart(value="image", required = false) MultipartFile image,
                                                  @RequestPart(value="name", required = false) String name) throws IOException {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        UpdateCompanyResponseDTO updateCompany = companyService.updateCompany(company_id, image, name, currentMemberId);

        // TODO: 반환 데이터 협의
        return updateCompany;
    }

    @DeleteMapping("/api/companies/{company_id}")
    public Long deleteCompany(@PathVariable Long company_id) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Long deleteCompanyId = companyService.deleteCompany(company_id, currentMemberId);

        // TODO: 반환 데이터 협의
        return deleteCompanyId;
    }
}
