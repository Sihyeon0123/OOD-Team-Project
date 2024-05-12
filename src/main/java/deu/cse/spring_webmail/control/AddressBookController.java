package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.entity.Contact;
import deu.cse.spring_webmail.repository.ContactRepository;
import deu.cse.spring_webmail.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AddressBookController {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ContactService contactService;

    @GetMapping("/addressbook")
    public String showAddressBook(Model model) {
        model.addAttribute("addressBook", contactRepository.findAll());
        return "addressbook";
    }
    @PostMapping("/addressbook/add")
public String addContact(@RequestParam String name, @RequestParam String email, @RequestParam String phoneNumber) {
    Contact contact = new Contact();
    contact.setName(name);
    contact.setEmail(email);
    contact.setPhoneNumber(phoneNumber);
    
    contactRepository.save(contact);
    
    return "redirect:/addressbook";
}
    @PostMapping("/addressbook/delete")
    @ResponseBody
    public void deleteContact(@RequestParam Long id) {
        contactService.deleteContact(id);     
    }
}