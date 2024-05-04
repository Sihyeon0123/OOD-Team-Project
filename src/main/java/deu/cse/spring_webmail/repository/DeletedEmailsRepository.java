package deu.cse.spring_webmail.repository;

import deu.cse.spring_webmail.entity.DeletedEmails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface DeletedEmailsRepository extends JpaRepository<DeletedEmails, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM DeletedEmails d WHERE d.user.username = :username AND d.mailID = :mailID")
    void deleteByUserUsernameAndMailID(@Param("username") String username, @Param("mailID") int mailID);

    List<DeletedEmails> findByUserUsername(String username);
}
