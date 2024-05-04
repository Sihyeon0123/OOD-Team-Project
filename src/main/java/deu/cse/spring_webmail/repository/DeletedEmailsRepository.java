package deu.cse.spring_webmail.repository;

import deu.cse.spring_webmail.entity.DeletedEmails;
import org.springframework.data.repository.CrudRepository;
import deu.cse.spring_webmail.entity.Users;
import java.util.ArrayList;

public interface DeletedEmailsRepository extends CrudRepository<DeletedEmails, String> {
    ArrayList<DeletedEmails> findByUserOrderByCreatedAtDesc(Users user);
    void deleteByUserAndMailID(Users user, int mailID);
}