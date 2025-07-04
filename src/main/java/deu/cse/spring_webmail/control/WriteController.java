/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.model.SmtpAgent;
import deu.cse.spring_webmail.service.SentEmailService;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 메일 쓰기를 위한 제어기
 * 
 * @author Prof.Jong Min Lee
 */
@Controller
@PropertySource("classpath:/system.properties")
@Slf4j
public class WriteController {
    @Value("${file.upload_folder}")
    private String UPLOAD_FOLDER;
    @Value("${file.max_size}")
    private String MAX_SIZE;
    
    @Autowired
    private ServletContext ctx;
    @Autowired
    private HttpSession session;
    @Autowired
    private SentEmailService sentEmailService;

    @GetMapping("/write_mail")
    public String writeMail() {
        log.debug("write_mail called...");
        session.removeAttribute("sender");  // 220612 LJM - 메일 쓰기 시는 
        return "write_mail/write_mail";
    }
    
    @GetMapping("/write_mail_me")
    public String writeMailMe() {
        log.debug("write_mail_me called...");
        session.removeAttribute("sender");  // 220612 LJM - 메일 쓰기 시는 
        return "write_mail/write_mail_me";
    }
    
    @PostMapping("/write_mail.do")
    public String writeMailDo(@RequestParam String to, @RequestParam String cc, 
            @RequestParam String subj, @RequestParam String body, 
            @RequestParam(name="file1") MultipartFile upFile,
            RedirectAttributes attrs) {
        log.debug("write_mail.do: to = {}, cc = {}, subj = {}, body = {}, file1 = {}",
                to, cc, subj, body, upFile.getOriginalFilename());
        // FormParser 클래스의 기능은 매개변수로 모두 넘어오므로 더이상 필요 없음.
        // 업로드한 파일이 있으면 해당 파일을 UPLOAD_FOLDER에 저장해 주면 됨.
        if (!"".equals(upFile.getOriginalFilename())) {
            String basePath = ctx.getRealPath(UPLOAD_FOLDER);
            log.debug("{} 파일을 {} 폴더에 저장...", upFile.getOriginalFilename(), basePath);
            File f = new File(basePath + File.separator + upFile.getOriginalFilename());
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f))) {
                bos.write(upFile.getBytes());
            } catch (IOException e) {
                log.error("upload.do: 오류 발생 - {}", e.getMessage());
            }
        }
        boolean sendSuccessful = sendMessage(to, cc, subj, body, upFile);
        if (sendSuccessful) {
            attrs.addFlashAttribute("msg", "메일 전송이 성공했습니다.");
        } else {
            attrs.addFlashAttribute("msg", "메일 전송이 실패했습니다.");
        }
        
        return "redirect:/main_menu";
    }
    
    /**
     * FormParser 클래스를 사용하지 않고 Spring Framework에서 이미 획득한 매개변수 정보를 사용하도록
     * 기존 webmail 소스 코드를 수정함.
     * 
     * @param to
     * @param cc
     * @param sub
     * @param body
     * @param upFile
     * @return 
     */
    
    private boolean sendMessage(String to, String cc, String subject, String body, MultipartFile upFile) {
        boolean status = false;

        String host = (String) session.getAttribute("host");
        String userid = (String) session.getAttribute("userid");

        // 4. SmtpAgent 객체에 메일 관련 정보 설정
        SmtpAgent agent = new SmtpAgent(host, userid);
        agent.setTo(to);
        agent.setCc(cc);
        agent.setSubj(subject);
        agent.setBody(body);
        String fileName = upFile.getOriginalFilename();
        
        if (fileName != null && !"".equals(fileName)) {
            log.debug("sendMessage: 파일({}) 첨부 필요", fileName);
            File f = new File(ctx.getRealPath(UPLOAD_FOLDER) + File.separator + fileName);
            agent.setFile1(f.getAbsolutePath());
        }

        // 5. 메일 전송 권한 위임
        if (agent.sendMessage()) {
            status = true;
            try {
                sentEmailService.saveSentEmail(userid, subject, body, new Date(), to);
            } catch (Exception e) {
                log.error("Failed to save sent email record: {}", e.getMessage());
                // 실패 로그 기록, 실패 처리 등 필요한 경우 추가 작업 수행
            }            
        }
        return status;
    }  // sendMessage()
}
