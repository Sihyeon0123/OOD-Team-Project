package deu.cse.spring_webmail.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import deu.cse.spring_webmail.entity.Users;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class DeletedEmails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "username", referencedColumnName = "username", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Users user;

    @Column(nullable = false)
    private int mailID;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;
}
