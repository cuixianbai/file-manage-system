package com.filemanage.controller;

import com.filemanage.dto.CompanyRequest;
import com.filemanage.dto.MessageResponse;
import com.filemanage.entity.Company;
import com.filemanage.repository.CompanyRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyRepository companyRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<Company>> getAllCompanies() {
        return ResponseEntity.ok(companyRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCompany(@Valid @RequestBody CompanyRequest request) {
        if (companyRepository.existsByName(request.getName())) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("公司名称已存在"));
        }

        Company company = Company.builder()
                .name(request.getName())
                .build();

        companyRepository.save(company);
        return ResponseEntity.ok(MessageResponse.success("公司创建成功"));
    }
}
