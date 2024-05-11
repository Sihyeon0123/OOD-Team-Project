/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.service;

import deu.cse.spring_webmail.entity.SentEmail;
import deu.cse.spring_webmail.entity.Users;
import deu.cse.spring_webmail.repository.SentEmailRepository;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author user
 */
@Service
public class SentEmailService {

    private final SentEmailRepository sentEmailRepository;

    /**
     * 생성자
     */
    @Autowired
    public SentEmailService(SentEmailRepository sentEmailRepository) {
        this.sentEmailRepository = sentEmailRepository;
    }

    /**
     * 이메일을 발신 메일함에 저장
     */
    public boolean saveSentEmail(String Username, String subject, String content, Date sentAt) {
        try {
            Users temp = new Users();
            temp.setUsername(Username);
           
            SentEmail sentEmail = new SentEmail();
            sentEmail.setUser(temp);
            
//            sentEmail.setRecipient(recipient);
            //sentEmail.setCc(cc);
            sentEmail.setSubject(subject);
            sentEmail.setContent(content);
            sentEmail.setSentAt(sentAt);
            this.sentEmailRepository.save(sentEmail); // SentEmail 객체 저장
            return true; // 저장이 성공했을 경우 true 반환
        } catch (Exception e) {
            e.printStackTrace(); // 예외가 발생하면 스택 트레이스 출력
            return false; // 저장이 실패했을 경우 false 반환
        }
    }

    /** username 및 mailID를 이용하여 제거 */
    @Transactional
    public void deleteSentEmail(String username, Date sentAt) {
        this.sentEmailRepository.deleteByUserUsernameAndSentAt(username, sentAt);
    }
    /**
     * 발신자의 사용자 이름을 기준으로 발신 메일함의 메일 값을 가져온다
     */
    public List<SentEmail> findByUsername(String username) {
        return this.sentEmailRepository.findByUserUsername(username);
    }
}

