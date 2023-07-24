package com.service.releasenote.domain.company.api;

import com.service.releasenote.domain.company.application.CompanyService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.service.releasenote.domain.company.dto.CompanyDTO.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"release"})
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/companies")
    public ResponseEntity<Long> createCompany(@RequestBody CreateCompanyRequestDTO createCompanyRequestDTO) {
        Long companyId = companyService.createCompany(createCompanyRequestDTO);

        // TODO: 반환 데이터 협의
        return new ResponseEntity<>(companyId, HttpStatus.CREATED);
    }

    @GetMapping("/companies")
    public ResponseEntity<Page<CompanyResponseDTO>> searchCompany(@RequestParam(value = "search", required = false, defaultValue = "") String name, Pageable pageable) {
        // 회사 이름이 비어있는 경우는 고려하지 않음
        Page<CompanyResponseDTO> companyList = companyService.findCompaniesByName(name, pageable);

        // TODO: 데이터 없는 경우 반환 데이터 협의
        return new ResponseEntity<>(companyList, HttpStatus.OK);
    }
}
