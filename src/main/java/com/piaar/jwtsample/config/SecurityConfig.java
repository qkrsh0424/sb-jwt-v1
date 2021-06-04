package com.piaar.jwtsample.config;

import com.piaar.jwtsample.config.auth.JwtAuthenticationFilter;
import com.piaar.jwtsample.config.auth.JwtAuthorizationFilter;
import com.piaar.jwtsample.config.auth.JwtLogoutSuccessHandler;
import com.piaar.jwtsample.model.refresh_token.repository.RefreshTokenRepository;
import com.piaar.jwtsample.model.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    // @Value("${app.jwt.csrf.secret}")
    // private String csrfJwtSecret;
    
    @Value("${app.jwt.access.secret}")
    private String accessTokenSecret;

    @Value("${app.jwt.refresh.secret}")
    private String refreshTokenSecret;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // .anonymous().disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .cors()
            .and()
                .authorizeRequests()
                .antMatchers("/api/v1/resource/**")
                .access("hasRole('ROLE_USER') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
                .anyRequest().permitAll()
            .and()
                .formLogin().disable()
                .logout()
                    .logoutUrl("/api/v1/logout")
                    .logoutSuccessHandler(new JwtLogoutSuccessHandler())
                    .deleteCookies("piaar_actoken")
                .and()
                .httpBasic().disable()
                .csrf()
                .disable()
                // .addFilterBefore(new CsrfHeaderFilterBefore(csrfJwtSecret), CsrfFilter.class)
                // .addFilterAfter(new CsrfHeaderFilterAfter(csrfJwtSecret), CsrfFilter.class)
                .addFilter(new JwtAuthenticationFilter(authenticationManager(), userRepository, refreshTokenRepository, accessTokenSecret, refreshTokenSecret)) // AuthenticationManager
                .addFilter(new JwtAuthorizationFilter(authenticationManager(), userRepository, refreshTokenRepository, accessTokenSecret, refreshTokenSecret)) // AuthenticationManager
                
            ;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
