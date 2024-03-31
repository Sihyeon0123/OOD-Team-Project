<%--
  Created by IntelliJ IDEA.
  User: 양시현
  Date: 2024-03-31
  Time: 오전 10:11
  To change this template use File | Settings | File Templates.
--%>
<%--
    Document   : signup
    Created on : 2024. 3. 29., 오후 4:19:43
    Author     : 양시현
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="deu.cse.spring_webmail.control.CommandType"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>

<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>회원가입 화면</title>
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

<div id="login_form">
  <h2>비밀번호 변경</h2>
  <strong>사용자: <%= session.getAttribute("userid") %></strong>
  <form method="POST" action="change_password.do">
    현재 암호: <input type="password" name="currentPassword" size="20"> <br />
    변경 암호: <input type="password" name="newPassword" size="20"> <br />
    <span style="margin-left: 20px">재입력: </span><input type="password" name="newPasswordConfirm" size="20"> <br /> <br />
    <input type="submit" value="암호변경" name="B1">&nbsp;&nbsp;&nbsp;
    <input type="reset" value="다시 입력" name="B2">
  </form>
</div>


<%@include file="footer.jspf"%>
</body>
</html>
