package com.howWeather.howWeather_backend.domain.member.service;

import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.TemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendTemporaryPassword(String toEmail, String tempPassword) {
        try {
            Context context = new Context();
            context.setVariable("tempPassword", tempPassword);

            String htmlContent = templateEngine.process("mail/temp-password", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(senderEmail, "날씨어때 고객센터");
            helper.setTo(toEmail);
            helper.setSubject("[날씨어때] 임시 비밀번호 안내");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("임시 비밀번호 이메일 전송 성공: {}", toEmail);
        } catch (Exception e) {
            log.error("임시 비밀번호 이메일 전송 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.EMAIL_SEND_FAIL_PASSWORD);
        }
    }
}