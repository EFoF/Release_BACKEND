package com.service.releasenote.domain.company.api;

import com.service.releasenote.domain.company.application.CompanyService;
import com.service.releasenote.domain.company.dto.CompanyDTO;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/newCompany")
    public ResponseEntity<Long> createCompany(@RequestBody CompanyDTO.CreateCompanyRequestDTO createCompanyRequestDTO) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Company company = companyService.createCompany(createCompanyRequestDTO, currentMemberId);

        return new ResponseEntity<>(company.getId(), HttpStatus.CREATED);
    }
}
