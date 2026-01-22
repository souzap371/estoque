package com.expedicao.estoque.security;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.*;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login").permitAll()
                .requestMatchers("/usuarios/**").hasRole("MASTER")
                .requestMatchers("/produtos/**").hasAnyRole("PRODUTOS", "MASTER")
                .requestMatchers("/estoque/**").hasAnyRole("ESTOQUE", "MASTER")
                .requestMatchers("/venda/**").hasAnyRole("VENDA", "MASTER")
                .requestMatchers("/financeiro/**").hasAnyRole("FINANCEIRO", "MASTER")                
                
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
            );

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
