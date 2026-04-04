package com.filemanage.controller;

import com.filemanage.dto.*;
import com.filemanage.entity.Company;
import com.filemanage.entity.User;
import com.filemanage.repository.CompanyRepository;
import com.filemanage.repository.UserRepository;
import com.filemanage.security.JwtUtil;
import com.filemanage.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Check if user is enabled
            if (!userDetails.isEnabled()) {
                return ResponseEntity.badRequest()
                        .body(MessageResponse.error("账号已被禁用，请联系管理员"));
            }

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    userDetails.getCompanyId(),
                    userDetails.getCompanyName(),
                    userDetails.getRole(),
                    userDetails.getStatus()
            ));
        } catch (DisabledException e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("账号已被禁用，请联系管理员"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("用户名或密码错误"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // Check if username exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("用户名已存在"));
        }

        // Check if email exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("邮箱已被注册"));
        }

        // Find or create company
        Company company = companyRepository.findByName(registerRequest.getCompanyName())
                .orElseGet(() -> {
                    Company newCompany = Company.builder()
                            .name(registerRequest.getCompanyName())
                            .build();
                    return companyRepository.save(newCompany);
                });

        // Create new user (default status: DISABLED)
        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .company(company)
                .role(User.Role.USER)
                .status(User.UserStatus.DISABLED)
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(MessageResponse.success("注册成功，请等待管理员启用账号"));
    }
}
