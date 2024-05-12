package deu.cse.spring_webmail.repository;

import deu.cse.spring_webmail.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Contact c WHERE c.id = :id")
    void deleteContactById(@Param("id") Long id);
}
