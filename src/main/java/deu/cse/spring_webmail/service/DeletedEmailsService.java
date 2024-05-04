package deu.cse.spring_webmail.service;

import deu.cse.spring_webmail.entity.DeletedEmails;
import deu.cse.spring_webmail.entity.Users;
import deu.cse.spring_webmail.repository.DeletedEmailsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DeletedEmailsService {
    private final DeletedEmailsRepository deletedEmailsRepository;
    
    /** 생성자 */
    @Autowired
    public DeletedEmailsService(DeletedEmailsRepository deletedEmailsRepository) {
        this.deletedEmailsRepository = deletedEmailsRepository;
    }
    
    /** 이메일을 휴지통에 저장 */
    public boolean saveDeletedEmail(String username, Date receivedDate) {
        try {
            Users user = new Users();
            user.setUsername(username);
            DeletedEmails deletedEmails = new DeletedEmails();
            deletedEmails.setUser(user);
            deletedEmails.setReceivedDate(receivedDate);
            this.deletedEmailsRepository.save(deletedEmails);
            return true; // 저장이 성공했을 경우 true 반환
        } catch (Exception e) {
            e.printStackTrace(); // 예외가 발생하면 스택 트레이스 출력
            return false; // 저장이 실패했을 경우 false 반환
        }
    }

    /** username 및 mailID를 이용하여 제거 */
    @Transactional
    public void deleteDeletedEmail(String username, Date receivedDate) {
        this.deletedEmailsRepository.deleteByUserUsernameAndReceivedDate(username, receivedDate);
    }

    /** 유저 이름을 기준으로 쓰레기통의 메일 값을 들고온다 */
    public List<DeletedEmails> findByUsername(String username) {
        return this.deletedEmailsRepository.findByUserUsername(username);
    }
}
