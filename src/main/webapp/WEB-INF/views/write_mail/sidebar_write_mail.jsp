<%--
  Created by IntelliJ IDEA.
  User: USER
  Date: 24. 5. 12.
  Time: 오전 4:22
  To change this template use File | Settings | File Templates.
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>JSP Page</title>
</head>
<body>
<br> <br>

<span style="color: indigo">
            <strong>사용자: <%= session.getAttribute("userid") %> </strong>
        </span> <br> <br>

<p> <a href="write_mail_me"> 내게 쓰기 </a> </p>
<a href="/webmail/main_menu">뒤로가기</a>
</body>
</html>
