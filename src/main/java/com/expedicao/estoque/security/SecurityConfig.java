package com.expedicao.estoque.security;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/error", "/h2-console/**").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                .requestMatchers("/usuarios/**").hasRole("MASTER")

                // Consultas e exportacoes permitidas ao perfil exclusivo de relatorios.
                // Estes matchers precisam vir antes das regras operacionais mais amplas.
                .requestMatchers("/relatorios", "/relatorios/**")
                    .hasAnyRole("RELATORIOS", "FINANCEIRO", "MASTER")
                .requestMatchers("/clientes/relatorio")
                    .hasAnyRole("RELATORIOS", "CLIENTES", "MASTER")
                .requestMatchers(
                    "/financeiro/relatorio", "/financeiro/relatorio/**",
                    "/financeiro/relatorios", "/financeiro/exportar/**",
                    "/financeiro/api/relatorio", "/financeiro/api/clientes",
                    "/financeiro/api/formas-pagamento", "/financeiro/api/pagamento/anexo/**")
                    .hasAnyRole("RELATORIOS", "FINANCEIRO", "MASTER")

                .requestMatchers("/produtos/**").hasAnyRole("PRODUTO", "MASTER")
                .requestMatchers("/estoque/**").hasAnyRole("ESTOQUE", "MASTER")
                .requestMatchers("/vendas/**").hasAnyRole("VENDAS", "MASTER")
                .requestMatchers("/clientes/**").hasAnyRole("CLIENTES", "MASTER")
                .requestMatchers("/financeiro/**", "/contas-pagar/**").hasAnyRole("FINANCEIRO", "MASTER")
                .requestMatchers("/dashboard", "/api/resumo-financeiro", "/api/jarvit/**")
                    .hasAnyRole("MASTER", "CLIENTES", "PRODUTO", "ESTOQUE", "VENDAS", "FINANCEIRO")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    boolean somenteRelatorios = authentication.getAuthorities().stream()
                        .allMatch(authority -> authority.getAuthority().equals("ROLE_RELATORIOS"));
                    String destino = somenteRelatorios ? "/relatorios" : "/dashboard";
                    response.sendRedirect(request.getContextPath() + destino);
                })
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
