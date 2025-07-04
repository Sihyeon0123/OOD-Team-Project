package deu.cse.spring_webmail.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Date;

@Setter
@Getter
@Entity
@NamedQuery(name = "DeletedEmails.findByUser", query = "SELECT d FROM DeletedEmails d WHERE d.user = :user")
public class DeletedEmails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "username", referencedColumnName = "username", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Users user;

    @Column(nullable = false)
    private Date receivedDate;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;
    
    /** 원본 수정 방지를 위한 객체 복사 반환 */
    public Date getCreatedAt() {
        return new Date(createdAt.getTime());
    }

    public Date getReceivedDate() {
        return new Date(receivedDate.getTime());
    }

    public Users getUser() {
        Users temp = new Users();
        temp.setUsername(user.getUsername());
        return temp;
    }
}
