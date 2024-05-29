package deu.cse.spring_webmail.service;

import deu.cse.spring_webmail.entity.Contact;
import deu.cse.spring_webmail.entity.Users;
import deu.cse.spring_webmail.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContactService {
    private final ContactRepository contactRepository;

    @Autowired
    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    /** INSERT INTO contact VALUES (name, email, phoneNumber, Users.username) */
    public void saveContact(String name, String email,
                            String phoneNumber, String username) {
        Users user = new Users();
        user.setUsername(username);

        Contact contact = new Contact();
        contact.setUser(user);
        contact.setName(name);
        contact.setEmail(email);
        contact.setPhoneNumber(phoneNumber);
        contactRepository.save(contact);
    }

    /** SELECT * FROM Contact WHERE username = Users.username */
    public List<Contact> findByUsername(String username) {
        return this.contactRepository.findByUserUsername(username);
    }

    public Contact findById(String username, long id) {
        Contact temp = contactRepository.findById(id).get();
        if(temp.getUser().getUsername().equals(username)) {
            return temp;
        }
        return null;
    }

    /** DELETE FROM contact WHERE id = id */
    @Transactional
    @Modifying
    public boolean deleteContact(String username, Long id) {
        Contact temp = contactRepository.findById(id).get();
        if(temp.getUser().getUsername().equals(username)){
            contactRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean updateContact(String username, long id, String name, String email, String phone){
        Contact temp = contactRepository.findById(id).get();
        if(temp.getUser().getUsername().equals(username)){
            temp.setName(name);
            temp.setEmail(email);
            temp.setPhoneNumber(phone);
            contactRepository.save(temp);
            return true;
        }
        return false;
    }
}
