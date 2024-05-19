/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.model;

import deu.cse.spring_webmail.entity.DeletedEmails;
import deu.cse.spring_webmail.service.DeletedEmailsService;
import jakarta.mail.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


/**
 *
 * @author skylo
 */
@Slf4j
@NoArgsConstructor        // 기본 생성자 생성
public class Pop3Agent {
    @Getter @Setter private String host;
    @Getter @Setter private String userid;
    @Getter @Setter private String password;
    @Getter @Setter private Store store;
    @Getter @Setter private String excveptionType;
    @Getter @Setter private HttpServletRequest request;

    // 220612 LJM - added to implement REPLY
    @Getter private String sender;
    @Getter private String subject;
    @Getter private String body;

    public Pop3Agent(String host, String userid, String password) {
        this.host = host;
        this.userid = userid;
        this.password = password;
    }
    
    public boolean validate() {
        boolean status = false;

        try {
            status = connectToStore();
            store.close();
        } catch (Exception ex) {
            log.error("Pop3Agent.validate() error : " + ex);
            status = false;  // for clarity
        } finally {
            return status;
        }
    }

    /** 계정 삭제시 해당 계정의 메일함을 비운다 */
    public boolean deleteUserMessage(String userid, HttpServletRequest request, String DOWNLOAD_FOLDER) {
        boolean status = false;
        if (!connectToStore()) {
            return status;
        }
        try {
            // Folder 설정
//            Folder folder = store.getDefaultFolder();
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_WRITE);

            // 메시지 가져오기
            Message[] msgs = folder.getMessages();

            for(Message msg : msgs) {
                MessageParser parser = new MessageParser(msg, userid, request);
                parser.parse(true);

                // 파일의 이름을 가져옴
                String fileName = parser.getFileName();

                // 파일이 존재한다면
                if(fileName != null){
                    // 파일 경로 생성
                    Path path = Paths.get("src/main/webapp", DOWNLOAD_FOLDER, userid, fileName);
                    Path absolutePath = path.toAbsolutePath();

                    // 파일 객체 생성
                    File file = new File(absolutePath.toString());

                    log.debug("삭제 파일 경로: {}", absolutePath.toString());

                    // 서버에 저장된 파일 삭제
                    if (file.exists()) {
                        boolean deleted = file.delete();
                        if (deleted) {
                            log.debug("{}파일이 삭제되었습니다.", fileName);
                        } else {
                            log.error("{}파일 삭제에 실패하였습니다.", fileName);
                        }
                    } else {
                        log.error("{}파일이 존재하지 않습니다.", fileName);
                    }
                }

                // Message에 DELETED flag 설정
                msg.setFlag(Flags.Flag.DELETED, true);
            }

            folder.close(true);  // expunge == true
            store.close();
            status = true;
        } catch (Exception ex) {
            log.error("deleteMessage() error: {}", ex.getMessage());
        } finally {
            return status;
        }
    }

    public boolean deleteMessage(int msgid, boolean really_delete, String DOWNLOAD_FOLDER) {
        boolean status = false;
        if (!connectToStore()) {
            return status;
        }

        try {
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_WRITE);

            // 메시지 가져오기
            Message msg = folder.getMessage(msgid);

            MessageParser parser = new MessageParser(msg, userid, request);
            parser.parse(true);

            // 파일의 이름을 가져옴
            String fileName = parser.getFileName();
    
            // 파일이 존재한다면
            if(fileName != null){
                // 파일 경로 생성
                Path path = Paths.get("src", "main", "webapp", DOWNLOAD_FOLDER, userid, fileName);
                Path absolutePath = path.toAbsolutePath();
                // 파일 객체 생성
                File file = new File(absolutePath.toString());
                log.debug("삭제 파일 경로: {}", absolutePath);

                // 서버에 저장된 파일 삭제
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (deleted) {
                        log.debug("{}파일이 삭제되었습니다.", fileName);
                    } else {
                        log.error("{}파일 삭제에 실패하였습니다.", fileName);
                    }
                } else {
                    log.debug("{}파일이 존재하지 않습니다.", fileName);
                }
            }

            // Message에 DELETED flag 설정
            msg.setFlag(Flags.Flag.DELETED, really_delete);

            // 폴더에서 메시지 삭제
            // Message [] expungedMessage = folder.expunge();
            // <-- 현재 지원 안 되고 있음. 폴더를 close()할 때 expunge해야 함.
            folder.close(true);  // expunge == true
            store.close();
            status = true;
        } catch (Exception ex) {
            log.error("deleteMessage() error: {}", ex.getMessage());
        } finally {
            return status;
        }
    }

    /*
     * 페이지 단위로 메일 목록을 보여주어야 함.
     */
    public String getMessageList(DeletedEmailsService deletedEmailsService, int page, int pageSize) {
        String result = "";
        Message[] filteredMessages = null;
        Message[] messages = null;
        Message[] temp = null;
        List<Message> filteredMessageList = new ArrayList<>();
        List<Message> tempList = new ArrayList<>();
        // 휴지통에 들어있는 메일 ID를 가져온다.
        List<DeletedEmails> deletedEmailList = deletedEmailsService.findByUsername(userid);

        if (!connectToStore()) {  // 3.1
            log.error("POP3 connection failed!");
            return "POP3 연결이 되지 않아 메일 목록을 볼 수 없습니다.";
        }
        try {
            // 메일 폴더 열기
            Folder folder = store.getFolder("INBOX");  // 3.2
            folder.open(Folder.READ_ONLY);  // 3.3

            // 현재 수신한 메시지 모두 가져오기
            temp = folder.getMessages();      // 3.4

            for (int i = temp.length; --i >= 0; ) {
                boolean found = false;
                for (DeletedEmails deletedEmail : deletedEmailList) {
                    if (deletedEmail.getReceivedDate().compareTo(temp[i].getSentDate()) == 0) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    filteredMessageList.add(temp[i]);
                }
            }
            // filteredMessages를 Message[] 배열로 변환
            messages = filteredMessageList.toArray(new Message[0]);

            if(page*pageSize < messages.length){
                for(int j = 0, i=(page-1)*pageSize; i<page*pageSize; i++, j++) {
                    tempList.add(messages[i]);
                }
            }
            else{
                for(int j = 0, i=(page-1)*pageSize; i<messages.length; i++, j++) {
                    tempList.add(messages[i]);
                }
            }
            filteredMessages = tempList.toArray(new Message[0]);

            FetchProfile fp = new FetchProfile();
            // From, To, Cc, Bcc, ReplyTo, Subject & Date
            fp.add(FetchProfile.Item.ENVELOPE);
            folder.fetch(filteredMessages, fp);

            MessageFormatter formatter = new MessageFormatter(userid);  //3.5
            result = formatter.getMessageTable(filteredMessages, page, pageSize);   // 3.6

            folder.close(true);  // 3.7
            store.close();       // 3.8
        } catch (Exception ex) {
            log.error("Pop3Agent.getMessageList() : exception = {}", ex.getMessage());
            result = "Pop3Agent.getMessageList() : exception = " + ex.getMessage();
        } finally {
            return result;
        }
    }

    /** 현재 삭제 메시지 개수를 반환 */
    public int getDeletedMessageCount(DeletedEmailsService deletedEmailsService){
        return deletedEmailsService.findByUsername(userid).size();
    }

    /** 현재 삭제되지 않은 메시지 개수를 반환 */
    public int getMessageCount(DeletedEmailsService deletedEmailsService) {
        int result = 0;
        Message[] temp = null;
        // 휴지통에 들어있는 메일 ID를 가져온다.
        List<DeletedEmails> deletedEmailList = deletedEmailsService.findByUsername(userid);

        if (!connectToStore()) {  // 3.1
            log.error("POP3 connection failed!");
        }
        try {
            // 메일 폴더 열기
            Folder folder = store.getFolder("INBOX");  // 3.2
            folder.open(Folder.READ_ONLY);  // 3.3

            // 현재 수신한 메시지 모두 가져오기
            temp = folder.getMessages();      // 3.4

            result = temp.length - deletedEmailList.size();
            log.info("{}", result);
            folder.close(true);  // 3.7
            store.close();       // 3.8
        } catch (Exception ex) {
            log.error("Pop3Agent.getMessageList() : exception = {}", ex.getMessage());
        } finally {
            return result;
        }
    }

    public String getTrashList(DeletedEmailsService deletedEmailsService, int page, int pageSize) {
        String result = "";
        // 휴지통에 들어있는 메일 ID를 가져온다.
        List<DeletedEmails> deletedEmailList = deletedEmailsService.findByUsername(userid);
        Message[] filteredMessages = null;
        Message[] temp = null;
        List<Message> tempList = new ArrayList<>();

        if (!connectToStore()) {  // 3.1
            log.error("POP3 connection failed!");
            return "POP3 연결이 되지 않아 메일 목록을 볼 수 없습니다.";
        }

        try {
            // 메일 폴더 열기
            Folder folder = store.getFolder("INBOX");  // 3.2
            folder.open(Folder.READ_ONLY);  // 3.3

            // 모든 메일을 가져온다.
            temp = folder.getMessages();

            for (int i = temp.length; --i >= 0; ) {
                boolean found = false;
                for (DeletedEmails deletedEmail : deletedEmailList) {
                    if (deletedEmail.getReceivedDate().compareTo(temp[i].getSentDate()) == 0) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    tempList.add(temp[i]);
                }
            }

            filteredMessages = tempList.toArray(new Message[0]);

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            folder.fetch(filteredMessages, fp);

            MessageFormatter formatter = new MessageFormatter(userid);  //3.5
            result = formatter.getTrashTable(filteredMessages, page, pageSize);   // 3.6

            folder.close(true);  // 3.7
            store.close();       // 3.8
        } catch (Exception ex) {
            log.error("Pop3Agent.getTrashList() : exception = {}", ex.getMessage());
            result = "Pop3Agent.getTrashList() : exception = " + ex.getMessage();
        } finally {
            return result;
        }
    }

    public String getMessage(int n) {
        String result = "POP3  서버 연결이 되지 않아 메시지를 볼 수 없습니다.";

        if (!connectToStore()) {
            log.error("POP3 connection failed!");
            return result;
        }

        try {
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);

            Message message = folder.getMessage(n);

            MessageFormatter formatter = new MessageFormatter(userid);
            formatter.setRequest(request);  // 210308 LJM - added
            result = formatter.getMessage(message);
            sender = formatter.getSender();  // 220612 LJM - added
            subject = formatter.getSubject();
            body = formatter.getBody();

            folder.close(true);
            store.close();
        } catch (Exception ex) {
            log.error("Pop3Agent.getMessageList() : exception = {}", ex);
            result = "Pop3Agent.getMessage() : exception = " + ex;
        } finally {
            return result;
        }
    }
    
    public String getSendMeList(DeletedEmailsService deletedEmailsService, int page, int pageSize) {
        String result = "";
        Message[] messages = null;
        List<DeletedEmails> deletedEmailList = deletedEmailsService.findByUsername(userid);
        List<Message> tempList = new ArrayList<>();
        Message[] filteredMessages = null;

        if (!connectToStore()) {  // 3.1
            log.error("POP3 connection failed!");
            return "POP3 연결이 되지 않아 메일 목록을 볼 수 없습니다.";
        }           
        try {
            // 메일 폴더 열기
            Folder folder = store.getFolder("INBOX");  // 3.2
            folder.open(Folder.READ_ONLY);  // 3.3 
            // 현재 수신한 메시지 모두 가져오기
            messages = folder.getMessages();      // 3.4

            if(!deletedEmailList.isEmpty()) {
                for (int i = messages.length; --i >= 0; ) {
                    boolean found = false;
                    for (DeletedEmails deletedEmail : deletedEmailList) {
                        if (deletedEmail.getReceivedDate().compareTo(messages[i].getSentDate()) != 0) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        tempList.add(messages[i]);
                    }
                }
            } else {
                for (int i = messages.length; --i >= 0; ) {
                    tempList.add(messages[i]);
                }
            }

            filteredMessages = tempList.toArray(new Message[0]);

            FetchProfile fp = new FetchProfile();
            // From, To, Cc, Bcc, ReplyTo, Subject & Date
            fp.add(FetchProfile.Item.ENVELOPE);
            folder.fetch(filteredMessages, fp);
            MessageFormatter formatter = new MessageFormatter(userid);  //3.5
            result = formatter.getSendMeTable(filteredMessages, page, pageSize);   // 3.6
            folder.close(true);  // 3.7
            store.close();       // 3.8
        } catch (Exception ex) {
            log.error("Pop3Agent.getSendMeList() : exception = {}", ex.getMessage());
            result = "Pop3Agent.getSendMeList() : exception = " + ex.getMessage();
        } finally {
            return result;
        }
    }

    /** 나에게 보낸 메세지 개수 반환 */
    public int getSendMeCount(DeletedEmailsService deletedEmailsService) {
        int result = 0;
        Message[] temp = null;

        if (!connectToStore()) {  // 3.1
            log.error("POP3 connection failed!");
        }
        try {
            // 메일 폴더 열기
            Folder folder = store.getFolder("INBOX");  // 3.2
            folder.open(Folder.READ_ONLY);  // 3.3

            // 현재 수신한 메시지 모두 가져오기
            temp = folder.getMessages();      // 3.4

            List<DeletedEmails> deletedEmails = deletedEmailsService.findByUsername(userid);

            for (Message msg : temp) {
                String sender = msg.getFrom()[0].toString();
                // 보낸 사람과 받는 사람이 같으면
                if (sender.equals(userid)) {
                    // 삭제되지 않았다면
                    boolean found = false;
                    for(DeletedEmails deletedEmail : deletedEmails) {
                        if(deletedEmail.getReceivedDate().compareTo(msg.getSentDate()) == 0){
                            found = true;
                            break;
                        }
                    }
                    if(found){
                        result++;
                    }
                }
            }
            folder.close(true);  // 3.7
            store.close();       // 3.8
        } catch (Exception ex) {
            log.error("Pop3Agent.getMessageList() : exception = {}", ex.getMessage());
        } finally {
            return result;
        }
    }

    public String getSearchList(DeletedEmailsService deletedEmailsService, String category, String searchKeyword) {
        String result = "";
        Message[] filteredMessages = null;
        Message[] temp = null;
        List<Message> filteredMessageList = new ArrayList<>();
        List<Message> filteredMessageList2 = new ArrayList<>();
        // 휴지통에 들어있는 메일 ID를 가져온다.
        List<DeletedEmails> deletedEmailList = deletedEmailsService.findByUsername(userid);

        if (!connectToStore()) {  // 3.1
            log.error("POP3 connection failed!");
            return "POP3 연결이 되지 않아 메일 목록을 볼 수 없습니다.";
        }
        try {
            // 메일 폴더 열기
            Folder folder = store.getFolder("INBOX");  // 3.2
            folder.open(Folder.READ_ONLY);  // 3.3

            // 현재 수신한 메시지 모두 가져오기
            temp = folder.getMessages();      // 3.4
            log.error("\n\n 처음 메시지의 개수: {}",temp.length);

            for (int i = temp.length; --i >= 0; ) {
                boolean found = false;
                for (DeletedEmails deletedEmail : deletedEmailList) {
                    if (deletedEmail.getReceivedDate().compareTo(temp[i].getSentDate()) == 0) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    filteredMessageList.add(temp[i]);
                }
            }

            log.error("\n\n처음 필터된 메시지 개수: {}", filteredMessageList.size());

            if("title".equals(category)){
                for(int i=0 ;i<filteredMessageList.size(); i++){
                    if(filteredMessageList.get(i).getSubject().contains(searchKeyword)){
                       filteredMessageList2.add(filteredMessageList.get(i));
                    }
                }
            } else if("sender".equals(category)){
                for(Message msg : filteredMessageList){
                    MessageParser messageParser = new MessageParser(msg, userid);
                    messageParser.parse(false);
                    if(messageParser.getFromAddress().contains(searchKeyword)){
                        filteredMessageList2.add(msg);
                    }
                }
            }

            filteredMessages = filteredMessageList2.toArray(new Message[0]);
            FetchProfile fp = new FetchProfile();
            // From, To, Cc, Bcc, ReplyTo, Subject & Date
            fp.add(FetchProfile.Item.ENVELOPE);
            folder.fetch(filteredMessages, fp);

            MessageFormatter formatter = new MessageFormatter(userid);  //3.5
            result = formatter.getSearchTable(filteredMessages);   // 3.6

            folder.close(true);  // 3.7
            store.close();       // 3.8
        } catch (Exception ex) {
            log.error("Pop3Agent.getSearchList() : exception = {}", ex.getMessage());
            result = "Pop3Agent.getSearchList() : exception = " + ex.getMessage();
        } finally {
            return result;
        }
    }

    /**POP3 프로토콜을 사용하여 이메일 서버에 연결하는 메서드입니다.
     * 호스트, 사용자 ID 및 비밀번호를 사용하여 이메일 서버에 연결하고, 연결 상태를 반환합니다.*/
    private boolean connectToStore() {
        boolean status = false;
        Properties props = System.getProperties();
        // https://jakarta.ee/specifications/mail/2.1/apidocs/jakarta.mail/jakarta/mail/package-summary.html
        props.setProperty("mail.pop3.host", host);
        props.setProperty("mail.pop3.user", userid);
        props.setProperty("mail.pop3.apop.enable", "false");
        props.setProperty("mail.pop3.disablecapa", "true");  // 200102 LJM - added cf. https://javaee.github.io/javamail/docs/api/com/sun/mail/pop3/package-summary.html
        props.setProperty("mail.debug", "false");
        props.setProperty("mail.pop3.debug", "false");

        Session session = Session.getInstance(props);
        session.setDebug(false);

        try {
            store = session.getStore("pop3");
            store.connect(host, userid, password);
            status = true;
        } catch (Exception ex) {
            log.error("connectToStore 예외: {}", ex.getMessage());
        } finally {
            return status;
        }
    }

    public Date getMessageSentDate(int msgId) {
        Date result = null;
        if (!connectToStore()) {  // 3.1
            log.error("POP3 connection failed!");
        }
        try {
            // 메일 폴더 열기
            Folder folder = store.getFolder("INBOX");  // 3.2
            folder.open(Folder.READ_ONLY);  // 3.3

            // 현재 수신한 메시지 모두 가져오기
            Message msg = folder.getMessage(msgId);      // 3.4
            result = msg.getSentDate();
            folder.close(true);  // 3.7
            store.close();       // 3.8
        } catch (Exception ex) {
            log.error("Pop3Agent.getMessageList() : exception = {}", ex.getMessage());
        } finally {
            return result;
        }
    }
}
