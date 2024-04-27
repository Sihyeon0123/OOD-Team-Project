package deu.cse.spring_webmail.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.beans.Transient;

@Slf4j
@Configuration
public class UserService {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    /** 데이터베이스 정보 객체 반환 */
    @Bean
    public DataSource dataSource() {
        log.debug("{}\n{}\n{}\n{}", url, username, password, driverClassName);
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    /** 회원가입 기능 */
    public void addUser(String userid, String passwd) {
        JdbcUserDetailsManager jdbcManager = new JdbcUserDetailsManager(dataSource());
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        UserDetails user = User.builder()
                .username(userid)
                .password(passwordEncoder.encode(passwd))
                .roles("USER")
                .build();
        jdbcManager.createUser(user);
    }

    /** 비밀번호 변경 */
    public void changePassword(String userid, String oldPasswd, String newPasswd) {
        JdbcUserDetailsManager jdbcManager = new JdbcUserDetailsManager(dataSource());
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if(passwordEncoder.matches(oldPasswd, jdbcManager.loadUserByUsername(userid).getPassword())) {
            jdbcManager.changePassword(userid, passwordEncoder.encode(newPasswd));
        }
    }

    /** 회원탈퇴 기능 */
    public void deleteUser(String userid, String password) {
        JdbcUserDetailsManager jdbcManager = new JdbcUserDetailsManager(dataSource());
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if(passwordEncoder.matches(password, jdbcManager.loadUserByUsername(userid).getPassword())) {
            jdbcManager.deleteUser(userid);
        }
    }

    public void deleteUsers(String[] userid) {
        JdbcUserDetailsManager jdbcManager = new JdbcUserDetailsManager(dataSource());
        for(String uid : userid) {
            jdbcManager.deleteUser(uid);
        }
    }
}