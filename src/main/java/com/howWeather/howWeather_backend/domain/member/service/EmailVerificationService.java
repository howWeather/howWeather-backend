package com.howWeather.howWeather_backend.domain.member.service;

import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final SpringTemplateEngine templateEngine;
    private static final long CODE_TTL_MINUTES = 10;
    private static final int MAX_REQUESTS_PER_DAY = 5;

    public void sendVerificationCode(String email) {
        String requestCountKey = "emailRequestCount:" + email;

        Long requestCount = redisTemplate.opsForValue().increment(requestCountKey);

        if (requestCount != null && requestCount == 1) {
            redisTemplate.expire(requestCountKey, 1, TimeUnit.DAYS);
        }

        if (requestCount != null && requestCount > MAX_REQUESTS_PER_DAY) {
            throw new CustomException(ErrorCode.EMAIL_CODE_REQUEST_LIMIT_EXCEEDED);
        }

        String code = generateCode();
        String key = "emailAuth:" + email;
        redisTemplate.opsForValue().set(key, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);

        Context context = new Context();
        context.setVariable("verificationCode", code);
        String htmlContent = templateEngine.process("email/verification-code", context);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("[날씨어때] 이메일 인증 코드");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAIL_PASSWORD);
        }
    }

    private String generateCode() {
        int code = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(code);
    }

    public void verifyCode(String email, String inputCode) {
        String key = "emailAuth:" + email;
        String savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            throw new CustomException(ErrorCode.EMAIL_VERDICT_CODE_NOT_FOUND);
        }

        if (!savedCode.equals(inputCode)) {
            throw new CustomException(ErrorCode.EMAIL_VERDICT_CODE_MISMATCH);
        }
        redisTemplate.delete(key);
    }
}
