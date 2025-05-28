package com.howWeather.howWeather_backend.global.jwt;

import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.global.custom.CustomUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1000L * 60 * 60;
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 1000L * 60 * 60 * 24 * 30;
    private static final String BEARER_TYPE = "Bearer";

    private final Key key;
    private final RedisTemplate<String, String> redisTemplate;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, RedisTemplate<String, String> redisTemplate, CustomUserDetailsService customUserDetailsService) {
        this.redisTemplate = redisTemplate;
        this.customUserDetailsService = customUserDetailsService;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtToken generateToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = System.currentTimeMillis();
        String username = authentication.getName();
        String accessToken = makeAccessToken(now, username, authorities);
        String refreshToken = makeRefreshToken(now, username, authorities);

        return JwtToken.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken).build();
    }

    private String makeAccessToken(long now, String username, String authorities) {
        Date accessTokenExpirationTime = new Date(now + ACCESS_TOKEN_EXPIRATION_TIME);
        return Jwts.builder()
                .setSubject(username)
                .claim("auth", authorities)
                .setExpiration(accessTokenExpirationTime)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String makeRefreshToken(long now, String username, String authorities) {
        Date refreshTokenExpirationTime = new Date(now + REFRESH_TOKEN_EXPIRATION_TIME);
        return Jwts.builder()
                .claim("auth", authorities)
                .setSubject(username)
                .setExpiration(refreshTokenExpirationTime)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public JwtToken reissueAccessToken(String refreshToken) {
        String redisKey = "blacklist:" + refreshToken;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            throw new RuntimeException("이미 로그아웃된 리프레시 토큰입니다.");
        }

        if (!validateToken(refreshToken)) {
            throw new IllegalArgumentException("리프레시 토큰이 유효하지 않습니다.");
        }

        Claims claims = parseClaims(refreshToken);
        if (claims == null || claims.getSubject() == null) {
            throw new IllegalArgumentException("리프레시 토큰의 정보가 잘못되었습니다.");
        }

        String username = claims.getSubject();
        String authorities = claims.get("auth") != null ? claims.get("auth").toString() : "";

        long now = System.currentTimeMillis();
        Date accessTokenExpirationTime = new Date(now + ACCESS_TOKEN_EXPIRATION_TIME);

        String newAccessToken = Jwts.builder()
                .setSubject(username)
                .claim("auth", authorities)
                .setExpiration(accessTokenExpirationTime)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtToken.builder()
                .grantType(BEARER_TYPE)
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

//    public Authentication getAuthentication(String token) {
//        Claims claims = parseClaims(token);
//        String loginId = claims.getSubject();
//
//        UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginId);
//
//        System.out.println("userDetails = " + userDetails);
//        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
//    }


    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        if (claims == null || claims.getSubject() == null) {
            throw new IllegalArgumentException("잘못된 리프레시 토큰.");
        }

        String username = claims.getSubject(); // == loginId
        String authorities = claims.get("auth") != null ? claims.get("auth").toString() : "";

        if (username.isEmpty()) {
            throw new IllegalArgumentException("사용자 이름이 비어있습니다.");
        }
        if (authorities.isEmpty()) {
            throw new IllegalArgumentException("권한 정보가 비어있습니다.");
        }

        Member member = (Member) customUserDetailsService.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken(member, token, member.getAuthorities());
    }


    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("유효하지 않은 토큰입니다.", e);
        } catch (ExpiredJwtException e) {
            log.error("만료된 토큰입니다.", e);
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            log.error("토큰의 claims이 비어 있습니다.", e);
        }
        return false;
    }


    public Date getExpiration(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration();
    }

    public String getUsername(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    public static String extractTokenFromRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }

        return null;
    }
}
