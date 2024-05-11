package deu.cse.spring_webmail.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import deu.cse.spring_webmail.entity.SentEmail;

@Repository
public interface SentEmailRepository extends JpaRepository<SentEmail, Long> {
    Page<SentEmail> findByUserUsernameOrderBySentAtDesc(String username, Pageable pageable);
}
