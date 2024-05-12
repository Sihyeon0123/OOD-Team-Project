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
        <script>
            function deleteContact(id) {
                if (confirm("정말로 삭제하시겠습니까?")) {
                    var form = document.createElement("form");
                    form.setAttribute("method", "post");
                    form.setAttribute("action", "/webmail/delete_address.do");

                    var hiddenField = document.createElement("input");
                    hiddenField.setAttribute("type", "hidden");
                    hiddenField.setAttribute("name", "id");
                    hiddenField.setAttribute("value", id);
                    form.appendChild(hiddenField);

                    document.body.appendChild(form);
                    form.submit();
                }
            }

            <c:if test="${!empty msg}">
            alert("${msg}");
            </c:if>
        </script>
    </head>
    <body>
    <%@include file="../header.jspf" %>

    <div id="sidebar">
            <jsp:include page="../sidebar_menu.jsp" />
        </div>
        <br/><br/>
        <h2>주소록 목록</h2>
        <table>
            <thead>
                <tr>
                    <th>이름</th>
                    <th>이메일</th>
                    <th>전화번호</th>
                    <th>메일쓰기</th>
                    <th>수정</th>
                    <th>삭제</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${addressBook}" var="contact">
                    <tr id="contactRow_${contact.id}">
                        <td>${contact.name}</td>
                        <td>${contact.email}</td>
                        <td>${contact.phoneNumber}</td>
                        <td><a href="/webmail/send_mail?email=${contact.email}">메일쓰기</a></td>
                        <td><a href="/webmail/update_address?id=${contact.id}">수정</a></td>
                        <td><a href="javascript:void(0);" onclick="deleteContact(${contact.id})">삭제</a></td>
                    </tr>
                </c:forEach>
                <tr>
                    <td colspan="6">
                        <button type="button" onclick="location.href='/webmail/add_addressbook';" style="height: 100%; width: 100%;">주소 추가</button>
                    </td>
                </tr>
            </tbody>
        </table>
        <%@include file="../footer.jspf"%>
    </body>
</html>
