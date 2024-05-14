/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.model;

import deu.cse.spring_webmail.entity.SentEmail;
import jakarta.mail.Message;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author skylo
 */
@Slf4j
@RequiredArgsConstructor
public class MessageFormatter {
    @NonNull private String userid;  // 파일 임시 저장 디렉토리 생성에 필요
    private HttpServletRequest request = null;
    
    // 220612 LJM - added to implement REPLY
    @Getter private String sender;
    @Getter private String subject;
    @Getter private String body;

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public String getMessageTable(Message[] messages, int page, int pageSize) {
        StringBuilder buffer = new StringBuilder();

        // 메시지 제목 보여주기
        buffer.append("<table>");  // table start
        buffer.append("<tr> "
                + " <th> No. </td> "
                + " <th> 보낸 사람 </td>"
                + " <th> 제목 </td>     "
                + " <th> 보낸 날짜 </td>   "
                + " <th> 삭제 </td>   "
                + " </tr>");

//        for (int i = messages.length - 1, no=1; i >= 0; i--, no++) {
        for(int i=0; i<messages.length; i++) {
            MessageParser parser = new MessageParser(messages[i], userid);
            parser.parse(false);  // envelope 정보만 필요
            // 메시지 헤더 포맷
            // 추출한 정보를 출력 포맷 사용하여 스트링으로 만들기
            buffer.append("<tr> "
                    + " <td id=no>" + (((page-1)*pageSize)+i+1) + " </td> "
                    + " <td id=sender>" + parser.getFromAddress() + "</td>"
                    + " <td id=subject> "
                    + " <a href=show_message?msgid=" + messages[i].getMessageNumber() + " title=\"메일 보기\"> "
                    + parser.getSubject() + "</a> </td>"
                    + " <td id=date>" + parser.getSentDate() + "</td>"
                    + " <td id=delete>"
                    + "<a href=trash.do"
                    + "?msgid=" + messages[i].getMessageNumber() + "> 삭제 </a>" + "</td>"
                    + " </tr>");
        }
        buffer.append("</table>");

        return buffer.toString();
//        return "MessageFormatter 테이블 결과";
    }

    public String getTrashTable(Message[] messages, int page, int pageSize) {
        StringBuilder buffer = new StringBuilder();

        // 메시지 제목 보여주기
        buffer.append("<table>");  // table start
        buffer.append("<tr> "
                + " <th> No. </td> "
                + " <th> 보낸 사람 </td>"
                + " <th> 제목 </td>     "
                + " <th> 보낸 날짜 </td>   "
                + " <th> 복구 </td>   "
                + " <th> 삭제 </td>   "
                + " </tr>");
        for(int i=0; i<messages.length; i++) {
            MessageParser parser = new MessageParser(messages[i], userid);
            parser.parse(false);  // envelope 정보만 필요
            // 메시지 헤더 포맷
            // 추출한 정보를 출력 포맷 사용하여 스트링으로 만들기
            if((page-1)*pageSize <= i && i < page * pageSize) {
                buffer.append("<tr> "
                        + " <td id=no>" + (i+1) + " </td> "
                        + " <td id=sender>" + parser.getFromAddress() + "</td>"
                        + " <td id=subject> "
                        + " <a href=show_message?msgid=" + messages[i].getMessageNumber() + " title=\"메일 보기\"> "
                        + parser.getSubject() + "</a> </td>"
                        + " <td id=date>" + parser.getSentDate() + "</td>"
                        + " <td id=delete>"
                        + "<a href=restore_mail.do"
                        + "?msgid=" + messages[i].getMessageNumber() + "> 복구 </a>" + "</td>"
                        + " <td id=delete>"
                        + "<a href=delete_mail.do"
                        + "?msgid=" + messages[i].getMessageNumber() + "> 삭제 </a>" + "</td>"
                        + " </tr>");
                if(i == page * pageSize) break;
            }
        }
        buffer.append("</table>");

        return buffer.toString();
//        return "MessageFormatter 테이블 결과";
    }

    public String getMessage(Message message) {
        StringBuilder buffer = new StringBuilder();

        // MessageParser parser = new MessageParser(message, userid);
        MessageParser parser = new MessageParser(message, userid, request);
        parser.parse(true);
        
        sender = parser.getFromAddress();
        subject = parser.getSubject();
        body = parser.getBody();

        buffer.append("보낸 사람: " + parser.getFromAddress() + " <br>");
        buffer.append("받은 사람: " + parser.getToAddress() + " <br>");
        buffer.append("Cc &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; : " + parser.getCcAddress() + " <br>");
        buffer.append("보낸 날짜: " + parser.getSentDate() + " <br>");
        buffer.append("제 &nbsp;&nbsp;&nbsp;  목: " + parser.getSubject() + " <br> <hr>");

        buffer.append(parser.getBody());

        String attachedFile = parser.getFileName();
        if (attachedFile != null) {
            buffer.append("<br> <hr> 첨부파일: <a href=download"
                    + "?userid=" + this.userid
                    + "&filename=" + attachedFile.replaceAll(" ", "%20")
                    + " target=_top> " + attachedFile + "</a> <br>");
        }

        return buffer.toString();
    }

    public String getSendMeTable(Message[] messages, int page, int pageSize) {
        StringBuilder buffer = new StringBuilder();

        buffer.append("<table>");
        buffer.append("<tr> "
                + " <th> No. </td> "
                + " <th> 제목 </td>     "
                + " <th> 보낸 날짜 </td>   "
                + " <th> 삭제 </td>   "
                + " </tr>");
        int count = 0;
        for (int i = messages.length - 1; i >= 0; i--) {
            MessageParser parser = new MessageParser(messages[i], userid);
            parser.parse(false);

            // 자기 자신에게 보낸경우에만 출력
            if (parser.getFromAddress().equals(userid)) {
//                log.info("min: {}, max: {}, count: {}",(page-1)*pageSize,page * pageSize, count);
                // 현재 페이지만 출력 되도록 수정
                if((page-1)*pageSize <= count && count < page * pageSize){
                    buffer.append("<tr> "
                            + " <td id=no>" + (count+1) + " </td> "
                            + " <td id=subject> "
                            + " <a href=show_message?msgid=" + (i + 1) + " title=\"메일 보기\"> "
                            + parser.getSubject() + "</a> </td>"
                            + " <td id=date>" + parser.getSentDate() + "</td>"
                            + " <td id=delete>"
                            + "<a href=delete_mail.do"
                            + "?msgid=" + (i + 1) + "> 삭제 </a>" + "</td>"
                            + " </tr>");
                }
                count++;
                if(count == page * pageSize) break;
            }
        }
        buffer.append("</table>");

        return buffer.toString();
    }
    public String getSentTable(List<SentEmail> msg, int page, int pageSize) {
        StringBuilder buffer = new StringBuilder();

        // 메시지 제목 보여주기
        buffer.append("<table>");  // table start
        buffer.append("<tr> "
                + " <th> No. </td> "
                + " <th> 수신자 </td> "
                + " <th> 제목 </td>     "
                + " <th> 보낸 날짜 </td>   "
                + " <th> 삭제 </td>   "
                + " </tr>");
            int i=1;
            for (SentEmail sent : msg) {
                Date date = sent.getSentAt();
                SimpleDateFormat outputFormat = new SimpleDateFormat("E MMM dd HH:mm:ss", Locale.ENGLISH);
                String formattedDateTime = outputFormat.format(date);
                buffer.append("<tr> "
                        + " <td id=sender>" + (((page-1)*pageSize) + i++) + "</td>" + " <td>" + sent.getReceiver() + "</td>"
                        + " <td id=subject> ");
                // JavaScript를 사용하여 폼을 동적으로 생성하고 POST 요청을 수행합니다.
                buffer.append("<form id=\"readForm\" method=\"post\" action=\"read_sent_mail\">");
                buffer.append("<input type=\"hidden\" name=\"id\" value=\"" + sent.getId() + "\">");
                buffer.append("<a href=\"#\" onclick=\"document.getElementById('readForm').submit();\" title=\"메일 보기\">" + sent.getSubject() + "</a>");
                buffer.append("</form>");

                buffer.append("</td>"
                        + " <td id=date>" + formattedDateTime + "</td>"
                        + " <td id=delete>");
                buffer.append("<form id=\"deleteForm\" method=\"post\" action=\"delete_sent_mail\">");
                buffer.append("<input type=\"hidden\" name=\"id\" value=\"" + sent.getId() + "\">");
                buffer.append("<a href=\"#\" onclick=\"confirmDelete();\"> 삭제" +
                        "<script>\n" +
                        "        function confirmDelete() {\n" +
                        "            if (confirm(\"정말로 삭제하시겠습니까?\")) {\n" +
                        "                document.getElementById('deleteForm').submit();\n" +
                        "            }\n" +
                        "        }\n" +
                        "    </script>" +
                        " </a>");
                buffer.append("</form>");

                buffer.append("</td>"
                        + " </tr>");

            }
        buffer.append("</table>");

        return buffer.toString();
    }

    public String getSearchTable(Message[] messages) {
        StringBuilder buffer = new StringBuilder();

        // 메시지 제목 보여주기
        buffer.append("<table>");  // table start
        buffer.append("<tr> "
                + " <th> No. </td> "
                + " <th> 보낸 사람 </td>"
                + " <th> 제목 </td>     "
                + " <th> 보낸 날짜 </td>   "
                + " <th> 삭제 </td>   "
                + " </tr>");

//        for (int i = messages.length - 1, no=1; i >= 0; i--, no++) {
        for(int i=0; i<messages.length; i++) {
            MessageParser parser = new MessageParser(messages[i], userid);
            parser.parse(false);  // envelope 정보만 필요
            // 메시지 헤더 포맷
            // 추출한 정보를 출력 포맷 사용하여 스트링으로 만들기
            buffer.append("<tr> "
                    + " <td id=no>" + (i+1) + " </td> "
                    + " <td id=sender>" + parser.getFromAddress() + "</td>"
                    + " <td id=subject> "
                    + " <a href=show_message?msgid=" + messages[i].getMessageNumber() + " title=\"메일 보기\"> "
                    + parser.getSubject() + "</a> </td>"
                    + " <td id=date>" + parser.getSentDate() + "</td>"
                    + " <td id=delete>"
                    + "<a href=trash.do"
                    + "?msgid=" + messages[i].getMessageNumber() + "> 삭제 </a>" + "</td>"
                    + " </tr>");
        }
        buffer.append("</table>");

        return buffer.toString();
//        return "MessageFormatter 테이블 결과";
    }

}
