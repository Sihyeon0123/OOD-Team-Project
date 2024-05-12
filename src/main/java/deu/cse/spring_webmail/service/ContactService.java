package deu.cse.spring_webmail.service;

import deu.cse.spring_webmail.entity.Contact;
import deu.cse.spring_webmail.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactService {

    private final ContactRepository contactRepository;

    @Autowired
    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public void addContact(String name, String email, String phoneNumber) {
        Contact contact = new Contact();
        contact.setName(name);
        contact.setEmail(email);
        contact.setPhoneNumber(phoneNumber);
        contactRepository.save(contact);
    }

    @Transactional
    @Modifying
    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
    }
}
