/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deu.cse.spring_webmail.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author jongmin
 */
@Slf4j
public class UserAdminAgent {

    private String server;
    private int port;
    Socket socket = null;
    InputStream is = null;
    OutputStream os = null;
    boolean isConnected = false;
    private String ROOT_ID;
    private String ROOT_PASSWORD;
    private String ADMIN_ID;
    // private final String EOL = "\n";
    private final String EOL = "\r\n";
    private String cwd;

    public UserAdminAgent(String server, int port, String cwd,
            String root_id, String root_pass, String admin_id) {
        log.debug("UserAdminAgent created: server = " + server + ", port = " + port);
        this.server = server;  // 127.0.0.1
        this.port = port;  // 4555
        this.cwd = cwd;
        this.ROOT_ID = root_id;
        this.ROOT_PASSWORD = root_pass;
        this.ADMIN_ID = admin_id;

        log.debug("isConnected = {}, root.id = {}", isConnected, ROOT_ID);

        try {
            socket = new Socket(server, port);
            is = socket.getInputStream();
            os = socket.getOutputStream();
        } catch (Exception e) {
            log.error("UserAdminAgent 생성자 예외: {}", e.getMessage());
        }

        isConnected = connect();
    }
    
    /**아이디의 중복 여부를 확인하기 위한 메서드
     * 중복되면 true 반환
     */
    public boolean isUserIDDuplicate(String userid){
        List <String> users = getUserList();
        // 아이디 중복 확인
        if (users.contains(userid) || "admin".equals(userid)) {
            return true;
        } else if(userid.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    /**
     *
     * @param userId
     * @param password
     * @return a boolean value as follows: - true: addUser operation successful
     * - false: addUser operation failed
     */
    // return value:
    //   - true: addUser operation successful
    //   - false: addUser operation failed
    public boolean addUser(String userId, String password) {
        boolean status = false;
        byte[] messageBuffer = new byte[1024];

        log.debug("addUser() called");
        if (!isConnected) {
            return status;
        }

        try {
            // 1: "adduser" command
            String addUserCommand = "adduser " + userId + " " + password + EOL;
            os.write(addUserCommand.getBytes(StandardCharsets.UTF_8));

            // 2: response for "adduser" command
            java.util.Arrays.fill(messageBuffer, (byte) 0);
            int bytesRead = is.read(messageBuffer);
            if (bytesRead == -1) {
                log.error("adduser: End of stream reached");
                throw new IOException("End of stream reached");
            }
            String recvMessage = new String(messageBuffer, StandardCharsets.UTF_8);
            log.debug(recvMessage);

            // 3: 기존 메일사용자 여부 확인
            if (recvMessage.contains("added")) {
                status = true;
            } else {
                status = false;
            }
            // 4: 연결 종료
            quit();
            System.out.flush();  // for test
            socket.close();
        } catch (Exception ex) {
            log.error("addUser 예외: {}", ex.getMessage());
            status = false;
        } finally {
            // 5: 상태 반환
            return status;
        }
    }  // addUser()

    public boolean changePassword(String userid, String currentPassword, String sessionPassword, String newPassword, String newPasswordConfirm){
        boolean result = false;
        byte[] messageBuffer = new byte[1024];

        if(newPassword.equals(newPasswordConfirm) && currentPassword.equals(sessionPassword)){
            try {
                // 1: 명령 송신
                String command = "setpassword " + userid + " " + newPassword + EOL;
                os.write(command.getBytes(StandardCharsets.UTF_8));
                os.flush(); // 버퍼 비우기

                // 2: 명령에 대한 응답 수신
                StringBuilder responseBuilder = new StringBuilder();
                int bytesRead = 0;
                while ((bytesRead = is.read(messageBuffer)) != -1) {
                    String receivedData = new String(messageBuffer, 0, bytesRead, StandardCharsets.UTF_8);
                    responseBuilder.append(receivedData);
                    if (receivedData.contains("\n")) {
                        // 읽은 데이터에 개행 문자가 포함되어 있다면 읽기를 중단합니다.
                        break;
                    }
                }

                // 3: 응답 메시지 처리
                String recvMessage = responseBuilder.toString().trim();
                log.debug("recvMessage = {}", recvMessage);

                quit();
                result = true;
            } catch (Exception ex) {
                log.error("getUserList(): 예외 = {}", ex.getMessage());
            }
        }
        return result;
    }

    public List<String> getUserList() {
        List<String> userList = new LinkedList<String>();
        byte[] messageBuffer = new byte[1024];

        log.info("root.id = {}, root.password = {}", ROOT_ID, ROOT_PASSWORD);

        if (!isConnected) {
            return userList;
        }

        try {
            // 1: "listusers" 명령 송신
            String command = "listusers " + EOL;
            os.write(command.getBytes(StandardCharsets.UTF_8));

            // 2: "listusers" 명령에 대한 응답 수신
            java.util.Arrays.fill(messageBuffer, (byte) 0);
            int bytesRead = is.read(messageBuffer);
            if (bytesRead == -1) {
                throw new IOException("End of stream reached");
            }

            // 3: 응답 메시지 처리
            String recvMessage = new String(messageBuffer, StandardCharsets.UTF_8);
            log.debug("recvMessage = {}", recvMessage);
            userList = parseUserList(recvMessage);

            quit();
        } catch (Exception ex) {
            log.error("getUserList(): 예외 = {}", ex.getMessage());
        } finally {
            return userList;
        }
    }  // getUserList()

    private List<String> parseUserList(String message) {
        List<String> userList = new LinkedList<String>();

        // UNIX 형식을 윈도우 형식으로 변환하여 처리
        message = message.replace("\r\n", "\n");

        // 1: 줄 단위로 나누기
        String[] lines = message.split("\n");
        // 2: 첫 번째 줄에는 등록된 사용자 수에 대한 정보가 있음.
        //    예) Existing accounts 7
        String[] firstLine = lines[0].split(" ");
        int numberOfUsers = Integer.parseInt(firstLine[2]);

        // 3: 두 번째 줄부터는 각 사용자 ID 정보를 보여줌.
        //    예) user: admin
        for (int i = 1; i <= numberOfUsers; i++) {
            // 3.1: 한 줄을 구분자 " "로 나눔.
            String[] userLine = lines[i].split(" ");
            // 3.2 사용자 ID가 관리자 ID와 일치하는 지 여부 확인
            if (!userLine[1].equals(ADMIN_ID)) {
                userList.add(userLine[1]);
            }
        }
        return userList;
    } // parseUserList()

    public boolean deleteUser(String userid) {
        byte[] messageBuffer = new byte[1024];
        boolean status = false;
        String command;
        String recvMessage;

        if (!isConnected) {
            return status;
        }

        try {

            command = "deluser " + userid + EOL;
            os.write(command.getBytes(StandardCharsets.UTF_8));
            log.debug(command);

            // 2: 응답 메시지 수신
            java.util.Arrays.fill(messageBuffer, (byte) 0);
            int bytesRead = is.read(messageBuffer);
            if (bytesRead == -1) {
                throw new IOException("End of stream reached");
            }

            // 3: 응답 메시지 분석
            recvMessage = new String(messageBuffer, StandardCharsets.UTF_8);
            log.debug("recvMessage = {}", recvMessage);
            if (recvMessage.contains("deleted")) {
                status = true;
            }

            quit();
        } catch (Exception ex) {
            log.error("deleteUsers(): 예외 = {}", ex.getMessage());
        } finally {
            return status;
        }
    }

    public boolean deleteUsers(String[] userList) {
        byte[] messageBuffer = new byte[1024];
        String command;
        String recvMessage;
        boolean status = false;

        if (!isConnected) {
            return status;
        }
        try {
            for (String userId : userList) {
                // 1: "deluser" 명령 송신
                command = "deluser " + userId + EOL;
                os.write(command.getBytes(StandardCharsets.UTF_8));
                log.debug(command);

                // 2: 응답 메시지 수신
                java.util.Arrays.fill(messageBuffer, (byte) 0);
                int bytesRead = is.read(messageBuffer);
                if (bytesRead == -1) {
                    throw new IOException("End of stream reached");
                }

                // 3: 응답 메시지 분석
                recvMessage = new String(messageBuffer, StandardCharsets.UTF_8);
                log.debug("recvMessage = {}", recvMessage);
                if (recvMessage.contains("deleted")) {
                    status = true;
                }
            }
            quit();
        } catch (Exception ex) {
            log.error("deleteUsers(): 예외 = {}", ex.getMessage());
        } finally {
            return status;
        }
    }  // deleteUsers()

    /** userid 의 비밀번호를 1234로 초기화 */
    public boolean resetPassword(String[] userid) {
        byte[] messageBuffer = new byte[1024];
        boolean status = false;
        String command;
        String recvMessage;

        if (!isConnected) {
            return status;
        }

        try {
            for(String user : userid) {
                command = "setpassword " + user + " 1234" + EOL;
                os.write(command.getBytes(StandardCharsets.UTF_8));
                log.debug(command);

                // 2: 응답 메시지 수신
                java.util.Arrays.fill(messageBuffer, (byte) 0);
                int bytesRead = is.read(messageBuffer);
                if (bytesRead == -1) {
                    throw new IOException("End of stream reached");
                }

                // 3: 응답 메시지 분석
                recvMessage = new String(messageBuffer, StandardCharsets.UTF_8);
                log.debug("recvMessage = {}", recvMessage);
                if (recvMessage.contains("reset")) {
                    status = true;
                }
            }
            quit();
        } catch (Exception ex) {
            log.error("deleteUsers(): 예외 = {}", ex.getMessage());
        } finally {
            return status;
        }
    }

    public boolean verify(String userid) {
        boolean status = false;
        byte[] messageBuffer = new byte[1024];

        try {
            // --> verify userid
            String verifyCommand = "verify " + userid;
            os.write(verifyCommand.getBytes(StandardCharsets.UTF_8));

            // read the result for verify command
            // <-- User userid exists   or
            // <-- User userid does not exist
            int bytesRead = is.read(messageBuffer);
            if (bytesRead == -1) {
                throw new IOException("End of stream reached");
            }

            String recvMessage = new String(messageBuffer, StandardCharsets.UTF_8);
            if (recvMessage.contains("exists")) {
                status = true;
            }

            quit();  // quit command
        } catch (IOException ex) {
            log.error("verify(): 예외 = {}", ex.getMessage());
        } finally {
            return status;
        }
    }

    private boolean connect() {
        byte[] messageBuffer = new byte[1024];
        boolean returnVal = false;
        String sendMessage;
        String recvMessage;

        log.info("connect() : root.id = {}, root.password = {}", ROOT_ID, ROOT_PASSWORD);

        // root 인증: id, passwd - default: root
        // 1: Login Id message 수신
        try {
            int bytesRead = is.read(messageBuffer);
            if (bytesRead == -1) {
                throw new IOException("End of stream reached");
            }
//            recvMessage = new String(messageBuffer); // 현재 사용하지 않아서 주석처리 수행

            // 2: rootId 송신
            sendMessage = ROOT_ID + EOL;
            os.write(sendMessage.getBytes(StandardCharsets.UTF_8));

            // 3: Password message 수신
            java.util.Arrays.fill(messageBuffer, (byte) 0);
            bytesRead = is.read(messageBuffer);
            if (bytesRead == -1) {
                throw new IOException("End of stream reached");
            }
            //recvMessage = new String(messageBuffer);

            // 4: rootPassword 송신
            sendMessage = ROOT_PASSWORD + EOL;
            os.write(sendMessage.getBytes(StandardCharsets.UTF_8));

            // 5: welcome message 수신
            java.util.Arrays.fill(messageBuffer, (byte) 0);
            // if (is.available() > 0) {
            bytesRead = is.read(messageBuffer);
            if (bytesRead == -1) {
                throw new IOException("End of stream reached");
            }
            recvMessage = new String(messageBuffer, StandardCharsets.UTF_8);

            if (recvMessage.contains("Welcome")) {
                returnVal = true;
            } else {
                returnVal = false;
            }
        } catch (Exception e) {
            log.error("connect() 예외: {}", e.getMessage());
        }

        return returnVal;
    }  // connect()

    public boolean quit() {
        byte[] messageBuffer = new byte[1024];
        boolean status = false;
        // quit
        try {
            // 1: quit 명령 송신
            String quitCommand = "quit" + EOL;
            os.write(quitCommand.getBytes(StandardCharsets.UTF_8));
            // 2: quit 명령에 대한 응답 수신
            java.util.Arrays.fill(messageBuffer, (byte) 0);
            //if (is.available() > 0) {
            int bytesRead = is.read(messageBuffer);
            if (bytesRead == -1) {
                throw new IOException("End of stream reached");
            }
            // 3: 메시지 분석
            String recvMessage = new String(messageBuffer, StandardCharsets.UTF_8);
            if (recvMessage.contains("closed")) {
                status = true;
            } else {
                status = false;
            }
        } catch (IOException ex) {
            log.error("quit() 예외: {}", ex);
        } finally {
            return status;
        }
    }
}
