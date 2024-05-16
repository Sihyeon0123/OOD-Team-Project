/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.entity.SentEmail;
import deu.cse.spring_webmail.model.MessageFormatter;
import deu.cse.spring_webmail.model.Pop3Agent;
import deu.cse.spring_webmail.service.DeletedEmailsService;
import deu.cse.spring_webmail.service.SentEmailService;
import jakarta.mail.internet.MimeUtility;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author Prof.Jong Min Lee
 */
@Controller
@PropertySource("classpath:/system.properties")
@Slf4j
public class ReadController {

    @Autowired
    private ServletContext ctx;
    @Autowired
    private HttpSession session;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private DeletedEmailsService deletedEmailsService;
    @Autowired
    private SentEmailService sentEmailService;

    @Value("${file.download_folder}")
    private String DOWNLOAD_FOLDER;
    @Value("${page.size}")
    private int pageSize;


    @GetMapping("/show_message")
    public String showMessage(@RequestParam Integer msgid, Model model) {
        log.debug("download_folder = {}", DOWNLOAD_FOLDER);
        
        Pop3Agent pop3 = new Pop3Agent();
        pop3.setHost((String) session.getAttribute("host"));
        pop3.setUserid((String) session.getAttribute("userid"));
        pop3.setPassword((String) session.getAttribute("password"));
        pop3.setRequest(request);
        
        String msg = pop3.getMessage(msgid);
        session.setAttribute("sender", pop3.getSender());  // 220612 LJM - added
        session.setAttribute("subject", pop3.getSubject());
        session.setAttribute("body", pop3.getBody());
        model.addAttribute("msg", msg);
        return "read_mail/show_message";
    }
    
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam("userid") String userId,
            @RequestParam("filename") String fileName) {
        log.debug("userid = {}, filename = {}", userId, fileName);
        try {
            log.debug("userid = {}, filename = {}", userId, MimeUtility.decodeText(fileName));
        } catch (UnsupportedEncodingException ex) {
            log.error("error");
        }
        
        // 1. 내려받기할 파일의 기본 경로 설정
        String basePath = ctx.getRealPath(DOWNLOAD_FOLDER) + File.separator + userId;

        // 2. 파일의 Content-Type 찾기
        Path path = Paths.get(basePath + File.separator + fileName);
        String contentType = null;
        try {
            contentType = Files.probeContentType(path);
            log.debug("File: {}, Content-Type: {}", path.toString(), contentType);
        } catch (IOException e) {
            log.error("downloadDo: 오류 발생 - {}", e.getMessage());
        }

        // 3. Http 헤더 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.builder("attachment").filename(fileName, StandardCharsets.UTF_8).build());
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);

        // 4. 파일을 입력 스트림으로 만들어 내려받기 준비
        Resource resource = null;
        try {
            resource = new InputStreamResource(Files.newInputStream(path));
        } catch (IOException e) {
            log.error("downloadDo: 오류 발생 - {}", e.getMessage());
        }
        if (resource == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @GetMapping("/main_menu")
    public String mainMenu(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        log.debug("mainMenu() called...");
        Pop3Agent pop3 = new Pop3Agent();
        pop3.setHost((String) session.getAttribute("host"));
        pop3.setUserid((String) session.getAttribute("userid"));
        pop3.setPassword((String) session.getAttribute("password"));
        String messageList = pop3.getMessageList(this.deletedEmailsService, page, pageSize);

        // 최대 페이지수 반환
        int maxPageNumber = (int) Math.ceil((double) pop3.getMessageCount(this.deletedEmailsService) / pageSize);

        model.addAttribute("maxPageNumber", maxPageNumber);
        // 현재 페이지
        model.addAttribute("pageNumber", page);
        model.addAttribute("messageList", messageList);
        return "main_menu";
    }

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
    
    @GetMapping("/show_sent_mail")
    public String showSent(@RequestParam(name = "page", defaultValue = "1") int page,Model model) {
        log.debug("show_sent_mail called...");

        // 현재 로그인한 사용자의 이름을 동적으로 가져옴
        String username = (String) session.getAttribute("userid");

        Page<SentEmail> pageSentEmail = sentEmailService.findByUsername(username, page, pageSize);
        List<SentEmail> sentEmail = pageSentEmail.getContent();

        MessageFormatter msgFormatter = new MessageFormatter(username);
        // 조회한 발신 메일 목록을 모델에 추가하여 뷰에 전달
        model.addAttribute("SentEmail", msgFormatter.getSentTable(sentEmail, page, pageSize));
        model.addAttribute("maxPageNumber", pageSentEmail.getTotalPages());

        return "read_mail/show_sent_mail";
    }

    @PostMapping("/read_sent_mail")
    public String readSentMail(@RequestParam(name = "id") Long id, Model model) {
        log.debug("read_sent_mail called...");
        // 현재 로그인한 사용자의 이름을 동적으로 가져옴
        String username = (String) session.getAttribute("userid");
        if(sentEmailService.isNameMatchingCurrentUserById(id, username)){
            SentEmail mail = sentEmailService.findById(id);
            model.addAttribute("receiver", mail.getReceiver());
            model.addAttribute("sentAt", mail.getSentAt());
            model.addAttribute("subject", mail.getSubject());
            model.addAttribute("content", mail.getContent());
        }else{
            log.error("잘못된 접근입니다.");
            return "show_sent_mail";
        }
        return "read_mail/read_sent_mail";
    }

    @PostMapping("/delete_sent_mail")
    public String deleteSentMail(@RequestParam(name = "id") Long id) {
        log.debug("delete_sent_mail called...");
        // 현재 로그인한 사용자의 이름을 동적으로 가져옴
        String username = (String) session.getAttribute("userid");
        if(sentEmailService.isNameMatchingCurrentUserById(id, username)){
            log.debug("Sent Mail Delete success");
            sentEmailService.deleteSentEmail(id);
        }else{
            log.error("Sent Mail Delete failed");
        }

        return "redirect:show_sent_mail";
    }
    
    @GetMapping("/show_send_me")
    public String showSendMe(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        log.debug("show_send_me called...");
        Pop3Agent pop3 = new Pop3Agent();
        pop3.setHost((String) session.getAttribute("host"));
        pop3.setUserid((String) session.getAttribute("userid"));
        pop3.setPassword((String) session.getAttribute("password"));

        // 최대 페이지수 반환
        int maxPageNumber = (int) Math.ceil((double) pop3.getSendMeCount(deletedEmailsService) / pageSize);
        model.addAttribute("maxPageNumber", maxPageNumber);
        String sendMeList = pop3.getSendMeList(deletedEmailsService, page, pageSize);
        model.addAttribute("sendMeList", sendMeList);

        return "read_mail/show_send_me";
    }

    @GetMapping("/trash")
    public String trash(@RequestParam(name = "page", defaultValue = "1") int page,Model model) {
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
        log.debug("delete_mail.do: msgid = {}", msgId);

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

    @PostMapping("/search.do")
    public String search(@RequestParam String searchKeyword, @RequestParam String searchCategory, Model model,  RedirectAttributes attrs) {
        log.debug("search() called...");
        if(searchKeyword.isEmpty()){
            attrs.addFlashAttribute("msg", "검색 키워드는 1자 이상이어야 합니다.");
            return "redirect:main_menu";
        }

        log.debug("\n\n{} {}\n\n",searchKeyword, searchCategory);
        Pop3Agent pop3 = new Pop3Agent();
        pop3.setHost((String) session.getAttribute("host"));
        pop3.setUserid((String) session.getAttribute("userid"));
        pop3.setPassword((String) session.getAttribute("password"));
        String messageList = pop3.getSearchList(this.deletedEmailsService, searchCategory, searchKeyword);
        model.addAttribute("messageList", messageList);

        return "main_menu";
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
