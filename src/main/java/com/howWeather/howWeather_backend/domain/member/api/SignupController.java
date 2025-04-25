package com.howWeather.howWeather_backend.domain.member.api;

import com.howWeather.howWeather_backend.domain.member.dto.DuplicateCheckDto;
import com.howWeather.howWeather_backend.domain.member.dto.SignupRequestDto;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import com.howWeather.howWeather_backend.domain.member.service.SignupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/signup")
public class SignupController {
    private final SignupService signupService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    @GetMapping("/email-exist-check")
    public ResponseEntity<?> isEmailExist(@RequestBody DuplicateCheckDto dto) {
        boolean emailAlreadyExist = signupService.isEmailAlreadyExist(dto.getData());

        if (emailAlreadyExist)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 이메일입니다.");

        return ResponseEntity.ok("사용 가능한 이메일입니다.");
    }

    @GetMapping("/loginid-exist-check")
    public ResponseEntity<?> isLoginIdExist(@RequestBody DuplicateCheckDto dto) {
        boolean loginIdAlreadyExist = signupService.isLoginIdAlreadyExist(dto.getData());

        if (loginIdAlreadyExist)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 아아디입니다.");
        return ResponseEntity.ok("사용 가능한 아이디입니다.");
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        if (memberRepository.findByEmail(signupRequestDto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("해당 이메일로 가입한 계정이 이미 존재합니다.");
        }

        if (memberRepository.findByLoginId(signupRequestDto.getLoginId()).isPresent()) {
            return ResponseEntity.badRequest().body("해당 아이디를 가진 계정이 이미 존재합니다.");
        }

        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());

        try {
            Member member = Member.builder()
                    .loginId(signupRequestDto.getLoginId())
                    .password(encodedPassword)
                    .email(signupRequestDto.getEmail())
                    .nickname(signupRequestDto.getNickname())
                    .constitution(signupRequestDto.getConstitution())
                    .ageGroup(signupRequestDto.getAgeGroup())
                    .bodyType(signupRequestDto.getBodyType())
                    .gender(signupRequestDto.getGender())
                    .sensitivity(-1)
                    .build();

            memberRepository.save(member);
            return ResponseEntity.ok("회원가입에 성공하였습니다!");
        } catch(Exception e) {
            return ResponseEntity.internalServerError().body(e);
        }
    }
}
