package com.filemanage.config;

import com.filemanage.entity.Company;
import com.filemanage.entity.User;
import com.filemanage.repository.CompanyRepository;
import com.filemanage.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create default admin company if not exists
        Company adminCompany = companyRepository.findByName("系统管理")
                .orElseGet(() -> {
                    Company company = Company.builder()
                            .name("系统管理")
                            .build();
                    return companyRepository.save(company);
                });

        // Create default admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@filemanage.com")
                    .company(adminCompany)
                    .role(User.Role.ADMIN)
                    .status(User.UserStatus.ENABLED)
                    .build();
            userRepository.save(admin);
            System.out.println("默认管理员账号已创建: admin / admin123");
        }
    }
}
