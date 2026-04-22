package com.uab.sante.configSecurity;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // ✅ Active @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Routes publiques (sans authentification)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/consultations/**").permitAll()
                        .requestMatchers("/api/examens/**").permitAll()
                        .requestMatchers("/api/laboratoire/**").permitAll()
                        .requestMatchers("/api/assures/**").permitAll()
                        .requestMatchers("/api/medecin/**").permitAll()
                        .requestMatchers("/api/medicaments/**").permitAll()
                        .requestMatchers("/api/medicaments/import/**").permitAll()
                        .requestMatchers("/api/pharmacie/**").permitAll()
                        .requestMatchers("/api/uab/examens/**").permitAll()
                        .requestMatchers("/api/polices-taux/**").permitAll()
                        .requestMatchers("/api/structures/**").permitAll()
                        .requestMatchers("/api/polices/**").permitAll()        // ← Ajoute cette ligne
                        .requestMatchers("/api/polices-externes/**").permitAll() // ← Ajoute cette ligne
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/test/**").permitAll()
                        .requestMatchers("/api/uab/**").permitAll()
                        .requestMatchers("/api/utilisateurs/**").permitAll()
                        .requestMatchers("/api/taux-couverture/**").permitAll()
                        .requestMatchers("/api/structure/dashboard/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // ✅ Routes de test (pour déboguer)
                        .requestMatchers("/api/test/**").permitAll()

                        // ✅ Routes consultation - TOUTES nécessitent authentification
                        // Les rôles spécifiques seront gérés par @PreAuthorize
                        .requestMatchers("/api/consultations/**").authenticated()

                        // Routes laboratoire
                        .requestMatchers("/api/laboratoire/**").authenticated()

                        // Routes pharmacie
                        .requestMatchers("/api/pharmacie/**").authenticated()

                        // Routes UAB - uniquement admin UAB
                        .requestMatchers("/api/uab/**").hasRole("UAB_ADMIN")

                        // Routes utilisateurs - uniquement admin UAB
                        .requestMatchers("/api/utilisateurs/**").hasRole("UAB_ADMIN")

                        // Routes structure dashboard
                        .requestMatchers("/api/structure/dashboard/**").authenticated()

                        // ✅ Routes ouvertes pour les référentiels (GET uniquement)
                        .requestMatchers("/api/examens/**").authenticated()
                        .requestMatchers("/api/medicaments/**").authenticated()
                        .requestMatchers("/api/structures/**").authenticated()
                        .requestMatchers("/api/taux-couverture/**").authenticated()
                        .requestMatchers("/api/polices-taux/**").authenticated()

                        // Toutes les autres routes
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
