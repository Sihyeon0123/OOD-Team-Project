/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.entity;

/**
 *
 * @author user
 */
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class SentEmail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 송신자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "username", referencedColumnName = "username", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Users user;
    
    // 수신자
    @Column(nullable = false)
    private String receiver;

    // 제목
    @Column(nullable = false)
    private String subject;

    // 내용
    @Column(nullable = false)
    private String content;

    // 발신일
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sent_at", nullable = false)
    private Date sentAt;
}
