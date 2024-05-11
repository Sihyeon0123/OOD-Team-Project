package deu.cse.spring_webmail.security;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

@Configuration
@EnableWebSecurity
@Slf4j
public class SpringSecurityConfig {

    private final HttpSession httpSession;

    public SpringSecurityConfig(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    @Bean
    public MyUserDetailsService myUserDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager jdbcManager = new JdbcUserDetailsManager(dataSource);
        if(!jdbcManager.userExists("admin")) {
            UserDetails user = User.builder()
                    .username("admin")
                    .password(passwordEncoder().encode("admin"))
                    .roles("ADMIN")
                    .build();
            jdbcManager.createUser(user);
        }

        return new MyUserDetailsService(jdbcManager);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/js/**", "/css/**", "/images/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        http.headers(headerConfig -> headerConfig.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                .requestMatchers("/admin_menu", "/add_user").hasRole("ADMIN")
                .requestMatchers("/main_menu", "/change_password", "/change_password.do", "/show_message", "/write_mail", "/show_message", "/test", "/trash", "/show_send_me", "/show_sent_mail").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/", "/login.do", "/isUserIDDuplicate", "/signup", "/signup.do", "/login_fail").permitAll()
                .anyRequest().authenticated()
        );

        http.formLogin(formLogin -> formLogin
                .loginPage("/")
                .usernameParameter("userid")
                .passwordParameter("passwd")
                .successHandler(new MySimpleUrlAuthenticationSuccessHandler(httpSession))
//                .failureUrl("/login_fail")
                .failureHandler(new CustomAuthenticationFailureHandler(httpSession))
        );

        http.httpBasic(Customizer.withDefaults());

        http.logout(logout -> logout
                .logoutUrl("/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
        );

        return http.build();
    }

    public static class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
        private HttpSession httpSession;
        public CustomAuthenticationFailureHandler(HttpSession httpSession) { this.httpSession=httpSession; }
        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
            String username = URLEncoder.encode(request.getParameter("userid"), StandardCharsets.UTF_8);
            String loginFailURL = "/webmail/login_fail?userid=" + username;
            response.sendRedirect(loginFailURL);
        }
    }

    private static class MySimpleUrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
        private HttpSession httpSession;

        public MySimpleUrlAuthenticationSuccessHandler(HttpSession httpSession) {
            this.httpSession = httpSession;
        }

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
            log.info("Successfully authenticated user {}", authentication.getName());

            final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            log.info("Authorities {}", authorities);

            this.httpSession.setAttribute("userid", request.getParameter("userid"));
            this.httpSession.setAttribute("passwd", request.getParameter("passwd"));

            String loginURL = "/";
            for(GrantedAuthority authority : authorities) {
                String authorityName = authority.getAuthority();
                log.debug("Authority {}", authorityName);
                if(authorityName.equals("ROLE_ADMIN")) {
                    loginURL = "/webmail/login.do?menu=91";
                    break;
                }else if(authorityName.equals("ROLE_USER")) {
                    loginURL = "/webmail/login.do?menu=91";
                    break;
                }
            }

            log.debug("Login success URL {}", loginURL);
            response.sendRedirect(loginURL);
        }
    }
}


