package com.filemanage.service;

import com.filemanage.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendStatusChangeEmail(User user, User.UserStatus newStatus) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());

            if (newStatus == User.UserStatus.ENABLED) {
                message.setSubject("账号已启用 - 企业文件管理系统");
                message.setText(String.format(
                        "尊敬的 %s，\n\n您的账号已被管理员启用，现在可以登录系统。\n\n登录账号：%s\n\n请妥善保管您的账号信息。",
                        user.getUsername(),
                        user.getUsername()
                ));
            } else {
                message.setSubject("账号已被禁用 - 企业文件管理系统");
                message.setText(String.format(
                        "尊敬的 %s，\n\n您的账号已被管理员禁用，如有疑问请联系管理员。",
                        user.getUsername()
                ));
            }

            mailSender.send(message);
            log.info("Status change email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send status change email: {}", e.getMessage());
        }
    }

    public void sendPasswordResetEmail(User user, String newPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("密码已重置 - 企业文件管理系统");
            message.setText(String.format(
                    "尊敬的 %s，\n\n您的密码已被管理员重置。\n\n新密码：%s\n\n请尽快登录系统并修改密码。",
                    user.getUsername(),
                    newPassword
            ));

            mailSender.send(message);
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage());
        }
    }

    public void sendRegistrationSuccessEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("注册成功 - 企业文件管理系统");
            message.setText(String.format(
                    "尊敬的 %s，\n\n您的账号注册成功！\n\n登录账号：%s\n所属公司：%s\n\n请等待管理员启用账号后即可登录。",
                    user.getUsername(),
                    user.getUsername(),
                    user.getCompany().getName()
            ));

            mailSender.send(message);
            log.info("Registration success email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send registration email: {}", e.getMessage());
        }
    }
}
