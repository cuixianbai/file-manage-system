package com.filemanage.controller;

import com.filemanage.dto.MessageResponse;
import com.filemanage.dto.UserUpdateRequest;
import com.filemanage.entity.User;
import com.filemanage.repository.UserRepository;
import com.filemanage.security.UserDetailsImpl;
import com.filemanage.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private static final String DEFAULT_PASSWORD = "qweasdzxc";

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return ResponseEntity.ok(user);
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
}
