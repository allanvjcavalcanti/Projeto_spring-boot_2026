package br.com.oficina.seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * Quem pode acessar o quê. Tudo exige login, exceto a tela de login e os arquivos
 * estáticos. Os formulários POST são protegidos contra CSRF automaticamente.
 */
@Configuration
@EnableWebSecurity
public class SegurancaConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Os matchers usam antMatcher(...) de propósito: com o console do H2 ligado existem dois
    // servlets na aplicação e o requestMatchers("texto") fica ambíguo e derruba a inicialização.
    @Bean
    public SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                antMatcher("/login"),
                                antMatcher("/css/**"),
                                antMatcher("/js/**"),
                                antMatcher("/error")).permitAll()
                        // só o gerente mexe no catálogo, vê o console do banco e cancela ordens
                        .requestMatchers(
                                antMatcher("/servicos/**"),
                                antMatcher("/h2-console/**"),
                                antMatcher(HttpMethod.POST, "/ordens/*/cancelar")).hasRole("GERENTE")
                        // excluir qualquer coisa também é só do gerente
                        .requestMatchers(antMatcher(HttpMethod.POST, "/**/excluir")).hasRole("GERENTE")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .failureUrl("/login?erro")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?saiu")
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                // o console do H2 usa formulários próprios (sem token) e roda dentro de frames
                .csrf(csrf -> csrf.ignoringRequestMatchers(antMatcher("/h2-console/**")))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
