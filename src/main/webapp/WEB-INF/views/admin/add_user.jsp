<%-- 
    Document   : add_user.jsp
    Author     : jongmin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="deu.cse.spring_webmail.control.CommandType" %>

<!DOCTYPE html>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>사용자 추가 화면</title>
        <link type="text/css" rel="stylesheet" href="css/main_style.css" />
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
                                document.getElementById("result").innerText = "ID를 사용할 수 없습니다!";
                                document.getElementById("result").style.color = "red";
                                var button = document.getElementById("submitbutt");
                                button.disabled = true;
                            } else {
                                document.getElementById("result").innerText = "ID가 사용가능합니다.";
                                document.getElementById("result").style.color = "blue";
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
        <%@ include file="../header.jspf" %>

        <div id="sidebar">
            <jsp:include page="sidebar_admin_previous_menu.jsp" />
        </div>

        <div id="main">
            추가로 등록할 사용자 ID와 암호를 입력해 주시기 바랍니다. <br><br>
            <form name="AddUser" action="add_user.do" method="POST">
                <table border="0" align="left">
                    <tr>
                        <td>사용자 ID</td> <label id="result"></label>
                        <td> <input type="text" name="id" value="" size="20" id="id"/>  </td>
                    </tr>
                    <tr>
                        <td>암호 </td>
                        <td> <input type="password" name="password" value="" /> </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <input type="submit" value="등록" name="register" id="submitbutt" disabled/>
                            <input type="reset" value="초기화" name="reset" />
                        </td>
                    </tr>
                </table>

            </form>
        </div>

        <%@include file="../footer.jspf" %>
    </body>
</html>
