package com.service.releasenote.domain.company.api;

import com.service.releasenote.domain.company.application.CompanyService;
import com.service.releasenote.domain.company.dto.CompanyDTO;
import com.service.releasenote.domain.company.model.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/company")
    public ResponseEntity<Long> createCompany(@RequestBody CompanyDTO.CreateCompanyRequestDTO createCompanyRequestDTO) {
        Long companyId = companyService.createCompany(createCompanyRequestDTO);

        // TODO: 반환 데이터 협의
        return new ResponseEntity<>(companyId, HttpStatus.CREATED);
    }

    @GetMapping("/company")
    public ResponseEntity<Page<Company>> searchCompany(@RequestParam(value = "search", required = false, defaultValue = "") String name, Pageable pageable) {
        // 회사 이름이 비어있는 경우는 고려하지 않음
        Page<Company> companyList = companyService.findCompaniesByName(name, pageable);

        // TODO: 데이터 없는 경우 반환 데이터 협의
        return new ResponseEntity<>(companyList, HttpStatus.OK);
    }
}
