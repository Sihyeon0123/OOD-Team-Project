<%--
  Created by IntelliJ IDEA.
  User: 양시현
  Date: 2024-03-30
  Time: 오후 4:23
  To change this template use File | Settings | File Templates.
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>회원 탈퇴</title>
    <link type="text/css" rel="stylesheet" href="css/main_style.css" />
    <!--메시지가 존재하는지 확인하여 존재하면 출력 아니면 무시-->
    <script>
        <c:if test="${!empty msg}">
        alert("${msg}");
        </c:if>
    </script>
</head>
<body>
<%@include file="header.jspf"%>


<div id="withdrawal_form">
    <h2>회원 탈퇴</h2>
    <strong>사용자: <%= session.getAttribute("userid") %></strong>
    <form method="POST" action="withdrawal.do">
        암&nbsp;&nbsp;&nbsp;호: <input type="password" name="passwd" size="20"> <br /> <br />
        <input type="submit" value="회원탈퇴" name="B1">&nbsp;&nbsp;&nbsp;
        <input type="reset" value="다시 입력" name="B2" style="margin-right: 15px">
    </form>
</div>


<%@include file="footer.jspf"%>
</body>
</html>
