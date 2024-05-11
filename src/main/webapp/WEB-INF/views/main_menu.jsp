<%-- 
    Document   : main_menu
    Created on : 2022. 6. 10., 오후 3:15:45
    Author     : skylo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>주메뉴 화면</title>
        <link type="text/css" rel="stylesheet" href="css/main_style.css" />
        <script>
            <c:if test="${!empty msg}">
            alert("${msg}");
            </c:if>
        </script>
    </head>
    <body>
        <%@include file="header.jspf"%>

        <div id="sidebar">
            <jsp:include page="sidebar_menu.jsp" />
        </div>

        <!-- 메시지 삭제 링크를 누르면 바로 삭제되어 실수할 수 있음. 해결 방법은? -->
        <div id="main">
            <form action="search.do" method="POST" style="padding-left: 15px">
                <select name="searchCategory"><option value="title">제목</option><option value="sender">발신자</option></select>
                <input type="text" name="searchKeyword">
                <input type="submit" value="검색">
            </form>
            <br/>

            ${messageList}
            <br/>
            <div style="text-align: center;">
                <c:forEach var="page" begin="1" end="${maxPageNumber}" varStatus="status">
                    <a href="/webmail/main_menu?page=${status.index}" style="font-size: 20px; display: inline-block; text-align: center;">${status.index}</a>
                </c:forEach>
            </div>
        </div>

        <%@include file="footer.jspf"%>
    </body>
</html>
