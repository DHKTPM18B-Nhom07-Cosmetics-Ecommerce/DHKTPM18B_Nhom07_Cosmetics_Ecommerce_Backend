package iuh.fit.se.cosmeticsecommercebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

//Added by trang:
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF (vì dùng API)
                .csrf(csrf -> csrf.disable())

                //Added by trang: Enable CORS for FE (Vite 5173)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Tắt form login trắng bóc kia
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // CHO PHÉP TẤT CẢ TRONG LÚC DEV (sau này sửa lại)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    // Added by Trang: Custom CORS Source – Security sẽ dùng cấu hình này
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // FE của em
        config.setAllowedOriginPatterns(List.of("http://localhost:5173"));

        // Cho phép tất cả method
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Cho tất cả header
        config.setAllowedHeaders(List.of("*"));

        // Cho phép gửi cookie/x-token
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Apply cho tất cả API
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
