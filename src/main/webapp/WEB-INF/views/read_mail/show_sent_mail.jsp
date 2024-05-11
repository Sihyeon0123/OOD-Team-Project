<%-- 
    Document   : show_sent_mail
    Created on : 2024. 5. 11., 오후 11:36:31
    Author     : user
--%>


<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@include file="../checking_session.jspf"%>
<!DOCTYPE html>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>발신 메일 화면</title>
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
            ${SentEmail}
        </div>
        <br/>
        <div style="text-align: center;">
            <c:forEach var="page" begin="1" end="${maxPageNumber}" varStatus="status">
                <a href="/webmail/show_sent_mail?page=${status.index}" style="font-size: 20px; display: inline-block; text-align: center;">${status.index}</a>
            </c:forEach>
        </div>
        <%@include file="../footer.jspf"%>
    </body>
</html>
