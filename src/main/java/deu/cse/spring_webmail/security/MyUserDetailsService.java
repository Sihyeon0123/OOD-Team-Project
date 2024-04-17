package deu.cse.spring_webmail.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

//@Service
public class MyUserDetailsService implements UserDetailsService {
    //    private final InMemoryUserDetailsManager inMemoryManager;
    private final JdbcUserDetailsManager jdbcManager;

    MyUserDetailsService(JdbcUserDetailsManager jdbcManager) {
//        this.inMemoryManager = inMemoryManager;
        this.jdbcManager = jdbcManager;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(username.startsWith("admin")) {
//            return inMemoryManager.loadUserByUsername(username);
            return jdbcManager.loadUserByUsername(username);
        } else{
            return jdbcManager.loadUserByUsername(username);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}