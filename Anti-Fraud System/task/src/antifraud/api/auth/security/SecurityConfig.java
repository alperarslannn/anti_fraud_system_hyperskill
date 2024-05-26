package antifraud.api.auth.security;

import antifraud.domain.UserAccount;
import antifraud.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Optional;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserAccountRepository userAccountRepository;
    private final CustomBCryptPasswordEncoder encoder;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Optional<UserAccount> user = userAccountRepository.findByUsernameEqualsIgnoreCase(username);
            if (user.isEmpty()) {
                throw new UsernameNotFoundException(username);
            }
            CustomUserDetails customUserDetails = new CustomUserDetails(user.get().getId(), user.get().getUsername(), user.get().getPassword(), user.get().getSalt(), user.get().getUserAuthority().getName(), user.get().isLocked());
            SecurityContext context = SecurityContextHolder.getContext();
            Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails, customUserDetails.getPassword());
            context.setAuthentication(authentication);
            return customUserDetails;
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http
                .getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
                .userDetailsService(userDetailsService())
                .passwordEncoder(encoder);

        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
        http
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling()
                .accessDeniedHandler(customAccessDeniedHandler)
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .and()
                .csrf(AbstractHttpConfigurer::disable) // For Postman
                .headers(headers -> headers.frameOptions().disable()) // For the H2 console
                .authorizeHttpRequests(auth -> auth  // manage access
                                .requestMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/auth/user/").permitAll()
                                .requestMatchers(HttpMethod.POST, "/actuator/shutdown").permitAll()
                                .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                                .requestMatchers(HttpMethod.DELETE, "/api/auth/user/**").hasRole(Role.ADMINISTRATOR.name())
                                .requestMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole(Role.ADMINISTRATOR.name(), Role.SUPPORT.name())
                                .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole(Role.MERCHANT.name())
                                .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction/").hasRole(Role.MERCHANT.name())
                                .requestMatchers(HttpMethod.PUT, "/api/auth/access").hasRole(Role.ADMINISTRATOR.name())
                                .requestMatchers(HttpMethod.PUT, "/api/auth/access/").hasRole(Role.ADMINISTRATOR.name())
                                .requestMatchers(HttpMethod.PUT, "/api/auth/role").hasRole(Role.ADMINISTRATOR.name())
                                .requestMatchers(HttpMethod.PUT, "/api/auth/role/").hasRole(Role.ADMINISTRATOR.name())
                                .requestMatchers(HttpMethod.POST, "/api/antifraud/suspicious-ip").hasRole(Role.SUPPORT.name())
                                .requestMatchers(HttpMethod.DELETE, "/api/antifraud/suspicious-ip/**").hasRole(Role.SUPPORT.name())
                                .requestMatchers(HttpMethod.GET, "/api/antifraud/suspicious-ip").hasRole(Role.SUPPORT.name())
                                .requestMatchers(HttpMethod.POST, "/api/antifraud/stolencard").hasRole(Role.SUPPORT.name())
                                .requestMatchers(HttpMethod.DELETE, "/api/antifraud/stolencard/**").hasRole(Role.SUPPORT.name())
                                .requestMatchers(HttpMethod.GET, "/api/antifraud/stolencard").hasRole(Role.SUPPORT.name())
                                .requestMatchers(HttpMethod.GET, "/api/antifraud/history").hasRole(Role.SUPPORT.name())
                                .requestMatchers(HttpMethod.GET, "/api/antifraud/history/**").hasRole(Role.SUPPORT.name())
                                .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction").hasRole(Role.SUPPORT.name())
                                .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction/").hasRole(Role.SUPPORT.name())
                                .anyRequest().authenticated()
                        // other matchers
                )
                .sessionManagement(sessions -> sessions
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                )
                .authenticationManager(authenticationManager)
                .securityContext();

        return http.build();
    }

}
