<%-- 
    Document   : addressbook
    Author     : Taemin
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
        <%@include file="header.jspf"%>

        <div id="sidebar">
            <jsp:include page="sidebar_menu.jsp" />
        </div>

        <div id="main">
            <h2>주소록 추가</h2>
            <form action="/addressbook/add" method="post">
                <label for="name">이름:</label>
                <input type="text" id="name" name="name" required><br>
                <label for="email">이메일:</label>
                <input type="email" id="email" name="email" required><br>
                <label for="phoneNumber">전화번호:</label>
                <input type="text" id="phoneNumber" name="phoneNumber" required><br>
                <button type="submit">추가</button>
            </form>

            <hr>

            <h2>주소록 목록</h2>
            <table>
                <thead>
                    <tr>
                        <th>이름</th>
                        <th>이메일</th>
                        <th>전화번호</th>

                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${addressBook}" var="contact">
                        <tr id="contactRow_${contact.id}">
                            <td>${contact.name}</td>
                            <td>${contact.email}</td>
                            <td>${contact.phoneNumber}</td>
                            <td>
                                <button onclick="deleteContact(${contact.id})">삭제</button>
                            </td>
                        </tr>
                    </c:forEach>

                <script>
                    function deleteContact(id) {
                        if (confirm("정말로 삭제하시겠습니까?")) {
                            var xhr = new XMLHttpRequest();
                            xhr.open("POST", "/addressbook/delete?id=" + id, true);
                            xhr.onreadystatechange = function () {
                                if (xhr.readyState === 4) {
                                    if (xhr.status === 200) {
                                        var rowToDelete = document.getElementById("contactRow_" + id);
                                        if (rowToDelete) {
                                            rowToDelete.remove();
                                        }
                                    } else {
                                        alert("삭제 중 오류가 발생했습니다.");
                                    }
                                }
                            };
                            xhr.send();
                        }
                    }
                </script>
                </tbody>
            </table>
        </div>

        <%@include file="footer.jspf"%>
    </body>
</html>
