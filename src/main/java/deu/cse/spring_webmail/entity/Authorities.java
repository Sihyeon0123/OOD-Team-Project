package deu.cse.spring_webmail.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor // 전송받은 제목과 내용을 필드에 저장하는 생성자 자동화
@NoArgsConstructor  // 디폴트 생성자 생성
@Entity
@Table(name = "authorities", uniqueConstraints = @UniqueConstraint(columnNames = {"username", "authority"}))
public class Authorities {
    @Id
    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 50)
    private String authority;

    @OneToOne
    @MapsId
    @JoinColumn(name = "username")
    private Users userInfo;
}
