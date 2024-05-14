<%-- 
    Document   : show_message.jsp
    Author     : jongmin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>메일 보기 화면</title>
        <link type="text/css" rel="stylesheet" href="css/main_style.css" />
    </head>
    <body>
        <%@include file="../header.jspf"%>

        <div id="sidebar">
            <jsp:include page="../sidebar_previous_menu.jsp" />
        </div>

        <div id="msgBody">
            받는 사람: ${receiver} <br/>
            보낸 날짜: ${sentAt} <br/>
            제 &nbsp;&nbsp;&nbsp;  목: ${subject} <br> <hr>
            ${content}
        </div>

        <%@include file="../footer.jspf"%>
    </body>
</html>
