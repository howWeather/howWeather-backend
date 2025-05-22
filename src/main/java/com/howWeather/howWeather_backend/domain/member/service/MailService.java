package com.howWeather.howWeather_backend.domain.member.service;

import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendTemporaryPassword(String toEmail, String tempPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[YourApp] 임시 비밀번호 안내");
            message.setText("안녕하세요,\n\n요청하신 임시 비밀번호는 아래와 같습니다.\n\n" +
                    "임시 비밀번호: " + tempPassword + "\n\n" +
                    "로그인 후 반드시 비밀번호를 변경해주세요.\n\n감사합니다.");

            mailSender.send(message);
            log.info("임시 비밀번호 이메일 전송 성공: {}", toEmail);
        } catch (Exception e) {
            log.error("임시 비밀번호 이메일 전송 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.EMAIL_SEND_FAIL);
        }
    }
}