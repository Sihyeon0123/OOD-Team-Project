package deu.cse.spring_webmail.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


@Entity
@Setter @Getter
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name; // 주소록에 저장된 이름
    private String email; // 주소록에 저장된 이메일
    private String phoneNumber; // 주소록에 저장된 전화번호
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "username", referencedColumnName = "username", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Users user;

    public Users getUser() {
        Users temp = new Users();
        temp.setUsername(user.getUsername());
        return temp;
    }
}
