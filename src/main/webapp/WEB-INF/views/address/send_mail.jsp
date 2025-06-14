<%--
    Document   : send_mail
    Created on : 2024. 5. 12., 오전 12:26:40
    Author     : user
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="../checking_session.jspf"%>
<!DOCTYPE html>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>메일 쓰기 화면</title>
    <link type="text/css" rel="stylesheet" href="css/main_style.css" />
</head>
<body>
<%@include file="../header.jspf"%>

<div id="sidebar">
    <jsp:include page="../write_mail/sidebar_write_mail.jsp" />
</div>

<div id="main">
    <%-- <jsp:include page="mail_send_form.jsp" /> --%>
    <form enctype="multipart/form-data" method="POST" action="write_mail.do" >
        <table>
            <tr>
                <td> 수신 </td>
                <td> <input type="text" name="to" size="80"
                            value="${email}"> <input type="hidden" name="cc" size="5"></td>
            </tr>
            <tr>
                <td> 메일 제목 </td>
                <td> <input type="text" name="subj" size="80" required
                            value="${!empty param['sender'] ? "RE: " += sessionScope['subject'] : ''}" >  </td>
            </tr>
            <tr>
                <td colspan="2">본  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 문</td>
            </tr>
            <tr>  <%-- TextArea    --%>
                <td colspan="2">
                            <textarea rows="15" name="body" cols="80" maxlength="200">${!empty param['sender'] ?
                                    "



                                            ----
                                            " += sessionScope['body'] : ''}</textarea>
                </td>
            </tr>
            <tr>
                <td>첨부 파일</td>
                <td> <input type="file" name="file1"  size="80">  </td>
            </tr>
            <tr>
                <td colspan="2">
                    <input type="submit" value="메일 보내기">
                    <input type="reset" value="다시 입력">
                </td>
            </tr>
        </table>
    </form>
</div>

<%@include file="../footer.jspf"%>
</body>
</html>
