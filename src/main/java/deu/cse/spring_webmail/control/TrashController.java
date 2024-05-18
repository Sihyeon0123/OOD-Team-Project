package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.model.Pop3Agent;
import deu.cse.spring_webmail.service.DeletedEmailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;

@Controller
@PropertySource("classpath:/system.properties")
@Slf4j
public class TrashController {
    @Autowired
    private HttpSession session;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private DeletedEmailsService deletedEmailsService;

    @Value("${file.download_folder}")
    private String DOWNLOAD_FOLDER;
    @Value("${page.size}")
    private int pageSize;


    @GetMapping("/delete_mail.do")
    public String deleteMailDo(@RequestParam("msgid") Integer msgId, RedirectAttributes attrs) {
        log.debug("delete_mail.do: msgid = {}", msgId);

        String host = (String) session.getAttribute("host");
        String userid = (String) session.getAttribute("userid");
        String password = (String) session.getAttribute("password");

        Pop3Agent pop3 = new Pop3Agent(host, userid, password);
        Date sentDate = pop3.getMessageSentDate(msgId);

        boolean deleteSuccessful = pop3.deleteMessage(msgId, true, userid, request, DOWNLOAD_FOLDER);
        if (deleteSuccessful) {
            this.deletedEmailsService.deleteDeletedEmail(userid, sentDate);
            attrs.addFlashAttribute("msg", "메시지 삭제를 성공하였습니다.");
        } else {
            attrs.addFlashAttribute("msg", "메시지 삭제를 실패하였습니다.");
        }

        return "redirect:trash";
    }

    @GetMapping("/trash")
    public String trash(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        log.debug("trash() called...");
        Pop3Agent pop3 = new Pop3Agent();
        pop3.setHost((String) session.getAttribute("host"));
        pop3.setUserid((String) session.getAttribute("userid"));
        pop3.setPassword((String) session.getAttribute("password"));

        // 최대 페이지수 반환
        int maxPageNumber = (int) Math.ceil((double) pop3.getDeletedMessageCount(this.deletedEmailsService) / pageSize);
        model.addAttribute("maxPageNumber", maxPageNumber);
        log.info("{}",maxPageNumber);

        // 현재 페이지
        model.addAttribute("pageNumber", page);
        String messageList = pop3.getTrashList(this.deletedEmailsService, page, pageSize);
        model.addAttribute("messageList", messageList);
        return "trash"; // change_password.jsp로 이동
    }

    @GetMapping("/trash.do")
    public String trashDo(@RequestParam("msgid") int msgId, RedirectAttributes attrs) {
        log.debug("trash.do: msgid = {}", msgId);

        String username = (String) session.getAttribute("userid");
        Pop3Agent pop3 = new Pop3Agent();
        pop3.setHost((String) session.getAttribute("host"));
        pop3.setUserid((String) session.getAttribute("userid"));
        pop3.setPassword((String) session.getAttribute("password"));

        Date sentDate =  pop3.getMessageSentDate(msgId);

        boolean deleteSuccessful = this.deletedEmailsService.saveDeletedEmail(username, sentDate);
        if (deleteSuccessful) {
            attrs.addFlashAttribute("msg", "메시지를 휴지통에 버렸습니다.");
        } else {
            attrs.addFlashAttribute("msg", "메시지를 버리지 못하였습니다.");
        }

        return "redirect:main_menu";
    }

    @GetMapping("/restore_mail.do")
    public String restoreMailDo(@RequestParam("msgid") int msgId, RedirectAttributes attrs) {
        log.debug("restore_mail.do: msgid = {}", msgId);
        String username = (String) session.getAttribute("userid");

        Pop3Agent pop3 = new Pop3Agent();
        pop3.setHost((String) session.getAttribute("host"));
        pop3.setUserid((String) session.getAttribute("userid"));
        pop3.setPassword((String) session.getAttribute("password"));

        Date sentDate =  pop3.getMessageSentDate(msgId);

        this.deletedEmailsService.deleteDeletedEmail(username, sentDate);
        attrs.addFlashAttribute("msg", "메시지를 복구하였습니다.");
        return "redirect:trash";
    }

}
