package deu.cse.spring_webmail.entity;

import jakarta.persistence.*;

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
