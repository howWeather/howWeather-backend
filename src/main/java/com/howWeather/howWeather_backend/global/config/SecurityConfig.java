package com.howWeather.howWeather_backend.global.config;

import com.howWeather.howWeather_backend.domain.member.service.CustomOAuth2UserService;
import com.howWeather.howWeather_backend.global.custom.CustomAccessDeniedHandler;
import com.howWeather.howWeather_backend.global.custom.CustomAuthEntryPoint;
import com.howWeather.howWeather_backend.global.jwt.JwtFilter;
import com.howWeather.howWeather_backend.global.jwt.JwtTokenProvider;
import com.howWeather.howWeather_backend.global.oauth.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomAuthEntryPoint customAuthEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, RedisTemplate redisTemplate) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests((authorizeRequestsConfig)->
                        authorizeRequestsConfig
                                .requestMatchers("/api/auth/email-exist-check", "/api/auth/login", "/api/auth/email/**",
                                        "/login/oauth2/**", "/api/oauth/**", "/internal-api/model/**",
                                        "/api/auth/signup", "/api/auth/loginid-exist-check", "/api/aes-test/**",
                                        "/api/auth/reset-password").permitAll()
                                .anyRequest().authenticated()
                ).oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"message\": \"OAuth2 로그인 실패: " + exception.getMessage() + "\"}");
                        })
                )
                .addFilterBefore(
                        new JwtFilter(jwtTokenProvider, redisTemplate),
                        UsernamePasswordAuthenticationFilter.class
                )
                .exceptionHandling(handler ->
                        handler
                                .authenticationEntryPoint(customAuthEntryPoint)
                                .accessDeniedHandler(customAccessDeniedHandler)
                )
                .sessionManagement((session)-> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
