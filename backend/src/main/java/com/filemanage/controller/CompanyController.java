package com.filemanage.controller;

import com.filemanage.dto.CompanyRequest;
import com.filemanage.dto.MessageResponse;
import com.filemanage.entity.Company;
import com.filemanage.entity.User;
import com.filemanage.repository.CompanyRepository;
import com.filemanage.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Value("${file.storage.dir-b}")
    private String dirBPath;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public ResponseEntity<?> getAllCompanies(
            @RequestParam(required = false) String name) {

        Specification<Company> spec = Specification.where(null);

        if (name != null && !name.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }

        return ResponseEntity.ok(companyRepository.findAll(spec));
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

        try {
            Path dirBLocation = Paths.get(dirBPath).toAbsolutePath().normalize();
            Path companyDir = dirBLocation.resolve(request.getName());
            Files.createDirectories(companyDir);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("创建公司目录失败: " + e.getMessage()));
        }

        return ResponseEntity.ok(MessageResponse.success("公司创建成功"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCompany(@PathVariable Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公司不存在"));

        // Check if company has users
        if (!company.getUsers().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("该公司下还有用户，无法删除"));
        }

        companyRepository.delete(company);
        return ResponseEntity.ok(MessageResponse.success("公司删除成功"));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCompanyStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公司不存在"));

        String newStatus = request.get("status");
        if (newStatus == null) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("状态不能为空"));
        }

        Company.CompanyStatus status;
        try {
            status = Company.CompanyStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("无效的状态值"));
        }

        company.setStatus(status);
        companyRepository.save(company);

        if (status == Company.CompanyStatus.DISABLED) {
            List<User> users = userRepository.findByCompanyId(id);
            for (User user : users) {
                user.setStatus(User.UserStatus.DISABLED);
            }
            userRepository.saveAll(users);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "公司状态更新成功");
        response.put("status", status);
        return ResponseEntity.ok(response);
    }
}
