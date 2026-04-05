package com.filemanage.controller;

import com.filemanage.dto.MessageResponse;
import com.filemanage.dto.UserUpdateRequest;
import com.filemanage.dto.UserCreateRequest;
import com.filemanage.dto.ChangePasswordRequest;
import com.filemanage.entity.Company;
import com.filemanage.entity.User;
import com.filemanage.repository.UserRepository;
import com.filemanage.repository.CompanyRepository;
import com.filemanage.security.UserDetailsImpl;
import com.filemanage.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private static final String DEFAULT_PASSWORD = "qweasdzxc";

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String status) {

        Specification<User> spec = Specification.where(null);

        if (username != null && !username.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
        }

        if (companyName != null && !companyName.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("company").get("name")), "%" + companyName.toLowerCase() + "%"));
        }

        if (status != null && !status.isEmpty()) {
            try {
                User.UserStatus userStatus = User.UserStatus.valueOf(status);
                spec = spec.and((root, query, cb) -> 
                    cb.equal(root.get("status"), userStatus));
            } catch (IllegalArgumentException ignored) {
            }
        }

        return ResponseEntity.ok(userRepository.findAll(spec));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // Prevent admin from disabling themselves
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();

        if (user.getId().equals(currentUser.getId()) && request.getStatus() == User.UserStatus.DISABLED) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("不能禁用当前登录的管理员账号"));
        }

        if (request.getStatus() == User.UserStatus.ENABLED) {
            Company company = user.getCompany();
            if (company != null && company.getStatus() == Company.CompanyStatus.DISABLED) {
                return ResponseEntity.badRequest()
                        .body(MessageResponse.error("公司已禁用，无法启用用户"));
            }
        }

        user.setStatus(request.getStatus());
        userRepository.save(user);

        // Send email notification if requested
        if (request.getSendEmail() != null && request.getSendEmail()) {
            emailService.sendStatusChangeEmail(user, request.getStatus());
        }

        String statusText = request.getStatus() == User.UserStatus.ENABLED ? "启用" : "禁用";
        return ResponseEntity.ok(MessageResponse.success("用户已" + statusText));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetPassword(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // Reset password to default
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        userRepository.save(user);

        // Send email notification if requested
        if (request.getSendEmail() != null && request.getSendEmail()) {
            emailService.sendPasswordResetEmail(user, DEFAULT_PASSWORD);
        }

        return ResponseEntity.ok(MessageResponse.success("密码已重置为: " + DEFAULT_PASSWORD));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/change-password")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            // 验证当前密码
            if (!passwordEncoder.matches(request.getCurrentPassword(), userDetails.getPassword())) {
                return ResponseEntity.badRequest()
                        .body(MessageResponse.error("当前密码错误"));
            }
            
            // 验证新密码长度
            if (request.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(MessageResponse.error("新密码长度不能少于6个字符"));
            }
            
            // 更新密码
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            
            return ResponseEntity.ok(MessageResponse.success("密码修改成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("修改密码失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // Prevent admin from deleting themselves
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();

        if (user.getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("不能删除当前登录的管理员账号"));
        }

        userRepository.delete(user);
        return ResponseEntity.ok(MessageResponse.success("用户删除成功"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody UserCreateRequest request) {
        try {
            // Check if username already exists
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(MessageResponse.error("用户名已存在"));
            }

            // Check if company exists
            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("公司不存在"));

            // Validate role based on company
            if (company.getName().equals("系统管理")) {
                if (request.getRole() != User.Role.ADMIN && request.getRole() != User.Role.MANAGER) {
                    return ResponseEntity.badRequest()
                            .body(MessageResponse.error("系统管理公司只能创建超级管理员或一般管理员"));
                }
            } else {
                if (request.getRole() != User.Role.USER) {
                    return ResponseEntity.badRequest()
                            .body(MessageResponse.error("非系统管理公司只能创建普通用户"));
                }
            }

            // Create new user
            User user = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                    .email(request.getEmail())
                    .company(company)
                    .role(request.getRole())
                    .status(User.UserStatus.ENABLED)
                    .build();

            userRepository.save(user);

            return ResponseEntity.ok(MessageResponse.success("用户创建成功，初始密码: " + DEFAULT_PASSWORD));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("创建用户失败: " + e.getMessage()));
        }
    }
}
