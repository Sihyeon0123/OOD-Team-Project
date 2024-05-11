package deu.cse.spring_webmail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import deu.cse.spring_webmail.entity.SentEmail;

@Repository
public interface SentEmailRepository extends JpaRepository<SentEmail, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM SentEmail s WHERE s.user.username = :username AND s.sentAt = :sentAt")
    void deleteByUserUsernameAndSentAt(@Param("username") String username, @Param("sentAt") Date sentAt);

    List<SentEmail> findByUserUsername(String username);
}
