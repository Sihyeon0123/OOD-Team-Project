package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.entity.DeletedEmails;
import deu.cse.spring_webmail.service.DeletedEmailsService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@Slf4j
@PropertySource("classpath:/system.properties")
public class Test {
    @Autowired
    private DeletedEmailsService deletedEmailsService;
    @Autowired
    private HttpSession session;

    @GetMapping("/insert")
    public String insert() {
        log.debug("insert() called...");
        String username = (String) session.getAttribute("userid");
        this.deletedEmailsService.saveDeletedEmail(username, 2);
        return "test";
    }

    @GetMapping("/delete")
    public String delete() {
        log.debug("delete() called...");
        String username = (String) session.getAttribute("userid");

        this.deletedEmailsService.deleteDeletedEmail(username, 2);
        this.deletedEmailsService.deleteDeletedEmail(username, 1);
        return "test";
    }

    @GetMapping("/select")
    public String select() {
        log.debug("select() called...");
        String username = (String) session.getAttribute("userid");

        List<DeletedEmails> temp = this.deletedEmailsService.findByUsername(username);
        log.info("\n\n");
        for(DeletedEmails de : temp) {
            log.info("{} {}", de.getUser().getUsername(), de.getMailID());
        }
        log.info("\n\n");
        return "test";
    }
}
