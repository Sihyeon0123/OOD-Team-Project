/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.service;

import deu.cse.spring_webmail.entity.SentEmail;
import deu.cse.spring_webmail.entity.Users;
import deu.cse.spring_webmail.repository.SentEmailRepository;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 *
 * @author user
 */
@Service
public class SentEmailService {
    private final SentEmailRepository sentEmailRepository;

    @Autowired
    public SentEmailService(SentEmailRepository sentEmailRepository) {
        this.sentEmailRepository = sentEmailRepository;
    }

    /**
     * 이메일을 발신 메일함에 저장
     */
    public boolean saveSentEmail(String username, String subject, String content, Date sentAt, String receiver) {
        try {
            Users user = new Users();
            user.setUsername(username);

            SentEmail sentEmail = new SentEmail();
            sentEmail.setReceiver(receiver);
            sentEmail.setUser(user);
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

    public void deleteSentEmail(Long id) {
        sentEmailRepository.deleteById(id);
    }
    /**
     * 발신자의 사용자 이름을 기준으로 발신 메일함의 메일 값을 가져온다
     */
    public Page<SentEmail> findByUsername(String username, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page-1, pageSize);
        return this.sentEmailRepository.findByUserUsernameOrderBySentAtDesc(username, pageable);
    }

    public boolean isNameMatchingCurrentUserById(Long id, String username) {
        SentEmail sentEmail = this.sentEmailRepository.findById(id).get();
        if (sentEmail.getUser().getUsername().equals(username)) {
            return true;
        }
        return false;
    }

    public SentEmail findById(Long id) {
        return this.sentEmailRepository.findById(id).get();
    }
}

