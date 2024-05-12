package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.entity.Contact;
import deu.cse.spring_webmail.service.ContactService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
public class AddressBookController {
    @Autowired
    private ContactService contactService;
    @Autowired
    private HttpSession session;

    @GetMapping("/addressbook")
    public String showAddressBook(Model model) {
        log.debug("showAddressBook() called...");
        String username = (String)session.getAttribute("userid");
        model.addAttribute("addressBook", this.contactService.findByUsername(username));
        return "address/addressbook";
    }

    @GetMapping("/add_addressbook")
    public String showAddressBookAdd() {
        log.debug("showAddressBookAdd() called...");
        return "address/addAddress";
    }

    @PostMapping("/add_addressbook.do")
    public String addContact(@RequestParam String name, @RequestParam String email, @RequestParam String phoneNumber) {
        log.debug("addContact() called...");
        String username = (String)session.getAttribute("userid");
        this.contactService.saveContact(name, email, phoneNumber, username);
        return "redirect:/addressbook";
    }

    @PostMapping("/delete_address.do")
    public String deleteContact(@RequestParam Long id, RedirectAttributes attrs) {
        log.debug("deleteContact() called...");
        String username = (String)session.getAttribute("userid");
        if (contactService.deleteContact(username, id)) {
            attrs.addFlashAttribute("msg", "주소를 제거하였습니다.");
        } else {
            attrs.addFlashAttribute("msg", "주소를 제거하지 못하였습니다.");
        }
        return "redirect:/addressbook";
    }

    @GetMapping("/update_address")
    public String updateAddress(@RequestParam Long id, Model model, RedirectAttributes attrs) {
        log.debug("updateAddress() called...");
        String username = (String)session.getAttribute("userid");
        Contact contact = contactService.findById(username, id);
        if(contact != null) {
            model.addAttribute("name", contact.getName());
            model.addAttribute("email", contact.getEmail());
            model.addAttribute("phone", contact.getPhoneNumber());
            model.addAttribute("id", contact.getId());
        }else{
            attrs.addFlashAttribute("msg", "잘못된 접근입니다.");
        }
        return "address/updateAddress";
    }

    @PostMapping("/update_address.do")
    public String updateAddressDO(@RequestParam Long id, @RequestParam String name, @RequestParam String email, @RequestParam String phoneNumber, RedirectAttributes attrs) {
        log.debug("updateAddress() called...");
        String username = (String)session.getAttribute("userid");
        if(contactService.updateContact(username, id, name, email, phoneNumber)) {
            attrs.addFlashAttribute("msg", "주소를 수정 하였습니다.");
        }else{
            attrs.addFlashAttribute("msg", "주소 수정에 실패하였습니다.");
        }
        return "redirect:/addressbook";
    }

    @GetMapping("/send_mail")
    public String sendMail(@RequestParam String email, Model model) {
        log.debug("sendMail() called...");
        model.addAttribute("email", email);
        return "address/send_mail";
    }
}