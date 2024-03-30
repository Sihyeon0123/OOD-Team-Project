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
    <script type="text/javascript">
        document.addEventListener("DOMContentLoaded", function() {
            document.getElementById("id").addEventListener("blur", function() {
                var id = this.value.trim(); // 입력 값 가져오기
                // AJAX 요청 생성
                var xhr = new XMLHttpRequest();
                xhr.open("POST", "isUserIDDuplicate", true); // POST 메서드로 /test 엔드포인트에 요청을 보냄
                xhr.setRequestHeader("Content-Type", "application/json"); // 요청 헤더 설정
                xhr.onreadystatechange = function() {
                    if (xhr.readyState === 4 && xhr.status === 200) { // 요청이 완료되고 응답이 정상적으로 받아졌을 때
                        var response = JSON.parse(xhr.responseText); // 서버에서 받은 JSON 응답을 파싱하여 객체로 변환

                        // 응답에 따른 처리
                        if (response.duplicate === true) {
                            document.getElementById("dupelabel").innerText = "ID를 사용할 수 없습니다!";
                            document.getElementById("dupelabel").style.color = "red";
                            var button = document.getElementById("submitbutt");
                            button.disabled = true;
                        } else {
                            document.getElementById("dupelabel").innerText = "ID가 사용가능합니다.";
                            document.getElementById("dupelabel").style.color = "blue";
                            var button = document.getElementById("submitbutt");
                            button.disabled = false;
                        }
                    }
                };
                // JSON 형식으로 데이터를 전송
                xhr.send(JSON.stringify({userid: id}));
            });
        });
    </script>
</head>
<body>
<%@include file="header.jspf"%>

<div id="login_form">
    <h2>회원가입</h2>
    <form method="POST" action="signup.do" onsubmit="return false;">
        사용자: <input type="text" name="userid" size="20" id="id" autofocus><br /><label id="dupelabel"></label><br/>
        암&nbsp;&nbsp;&nbsp;호: <input type="password" name="passwd" size="20"> <br />
        재입력: <input type="password" name="repasswd" size="20"> <br /> <br />
        <input type="submit" value="회원가입" name="B1", id="submitbutt" disabled>&nbsp;&nbsp;&nbsp;
        <input type="reset" value="다시 입력" name="B2" style="margin-right: 15px">
    </form>
</div>


<%@include file="footer.jspf"%>
</body>
</html>
