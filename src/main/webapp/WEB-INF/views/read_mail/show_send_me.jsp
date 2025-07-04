<%-- 
    Document   : show_send_me
    Created on : 2024. 5. 6., 오전 12:23:58
    Author     : user
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@include file="../checking_session.jspf"%>
<!DOCTYPE html>

<jsp:useBean id="pop3" scope="page" class="deu.cse.spring_webmail.model.Pop3Agent" />
<%
            pop3.setHost((String) session.getAttribute("host"));
            pop3.setUserid((String) session.getAttribute("userid"));
            pop3.setPassword((String) session.getAttribute("password"));
%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>내게 보낸 메일</title>
        <link type="text/css" rel="stylesheet" href="css/main_style.css" />
        <script>
            <c:if test="${!empty msg}">
            alert("${msg}");
            </c:if>
        </script>
    </head>
    <body>
        <%@include file="../header.jspf"%>

        <div id="sidebar">
            <jsp:include page="../sidebar_menu.jsp" />
        </div>

        <div id="main">
            <h2>내게 보낸 메일</h2>
            ${sendMeList}
            <br/>
            <div style="text-align: center;">
                <c:forEach var="page" begin="1" end="${maxPageNumber}" varStatus="status">
                    <a href="/webmail/show_send_me?page=${status.index}" style="font-size: 20px; display: inline-block; text-align: center;">${status.index}</a>
                </c:forEach>
            </div>
        </div>

        <%@include file="../footer.jspf"%>
    </body>
</html>
