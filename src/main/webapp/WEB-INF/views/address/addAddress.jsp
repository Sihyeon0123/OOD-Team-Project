<%--
  Created by IntelliJ IDEA.
  User: 양시현
  Date: 24. 5. 12.
  Time: 오후 2:21
  To change this template use File | Settings | File Templates.
--%>


<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>주소록</title>
    <link type="text/css" rel="stylesheet" href="css/main_style.css" />
</head>
<body>
<%@include file="../header.jspf"%>

<div id="sidebar">
    <jsp:include page="../sidebar_menu.jsp" />
</div>
<br/><br/>
<h2>주소록 추가</h2>

<form action="/webmail/add_addressbook.do" method="post">
    <table>
        <tr>
            <td><label for="name">이름:</label></td>
            <td><input type="text" id="name" name="name" required></td>
        </tr>
        <tr>
            <td><label for="email">이메일:</label></td>
            <td><input type="text" id="email" name="email" required></td>
        </tr>
        <tr>
            <td><label for="phoneNumber">전화번호:</label></td>
            <td><input type="text" id="phoneNumber" name="phoneNumber" ></td>
        </tr>
        <tr>
            <td colspan="2"><button type="submit" style="height: 100%; width: 100%;">추가</button></td>
        </tr>
    </table>
    <br>
</form>
<%@include file="../footer.jspf"%>
</body>
</html>
