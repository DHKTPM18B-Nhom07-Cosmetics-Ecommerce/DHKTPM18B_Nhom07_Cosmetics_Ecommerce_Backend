package iuh.fit.se.cosmeticsecommercebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF (vì dùng API)
                .csrf(csrf -> csrf.disable())

                // Tắt form login trắng bóc kia
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // CHO PHÉP TẤT CẢ TRONG LÚC DEV (sau này sửa lại)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}