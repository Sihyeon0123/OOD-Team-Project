package deu.cse.spring_webmail.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor // 전송받은 제목과 내용을 필드에 저장하는 생성자 자동화
@NoArgsConstructor  // 디폴트 생성자 생성
@Entity(name="users")
public class Users {
    @Id
    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, columnDefinition = "tinyint(1)")
    private boolean enabled;
}
