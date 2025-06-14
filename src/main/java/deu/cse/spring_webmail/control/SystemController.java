/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.model.Pop3Agent;
import deu.cse.spring_webmail.model.UserAdminAgent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

import deu.cse.spring_webmail.model.UserService;
import deu.cse.spring_webmail.security.MyUserDetailsService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * 초기 화면과 관리자 기능(사용자 추가, 삭제)에 대한 제어기
 *
 * @author skylo
 */
@Controller
@PropertySource("classpath:/system.properties")
@Slf4j
public class SystemController {

    @Autowired
    private ServletContext ctx;
    @Autowired
    private HttpSession session;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private UserService userService;
    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Value("${root.id}")
    private String ROOT_ID;
    @Value("${root.password}")
    private String ROOT_PASSWORD;
    @Value("${admin.id}")
    private String ADMINISTRATOR;  //  = "admin";
    @Value("${james.control.port}")
    private Integer JAMES_CONTROL_PORT;
    @Value("${james.host}")
    private String JAMES_HOST;
    @Value("${file.download_folder}")
    private String DOWNLOAD_FOLDER;


    @GetMapping("/")
    public String index() {
        log.debug("index() called...");
        session.setAttribute("host", JAMES_HOST);
        session.setAttribute("debug", "false");

        return "index";
    }

    @GetMapping("/test")
    public String test() {
        log.debug("test() called...");
        return "test";
    }

    @GetMapping("/change_password")
    public String changePassword() {
        return "change_password"; // change_password.jsp로 이동
    }

    @PostMapping("/change_password.do")
    public String changePasswordDo(RedirectAttributes attrs) {
        String userid = (String)session.getAttribute("userid");
        String currentPassword = request.getParameter("currentPassword");
        String sessionPassword = (String)session.getAttribute("password");;
        String newPassword = request.getParameter("newPassword");
        String newPasswordConfirm = request.getParameter("newPasswordConfirm");
        boolean result = false;

        if(newPassword.length() < 8){
            attrs.addFlashAttribute("msg", "비밀번호는 8자 이상이어야 합니다.");
            return "redirect:change_password";
        }

        try{
            String cwd = ctx.getRealPath(".");
            UserAdminAgent agent = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT, cwd,
                    ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);
            result = agent.changePassword(userid, currentPassword, sessionPassword, newPassword, newPasswordConfirm);
        } catch (Exception ex) {
            log.error("change_password.do : 예외 = {}", ex);
        }

        userService.changePassword(userid, currentPassword, newPassword);

        if(result){
            attrs.addFlashAttribute("msg", String.format("사용자(%s) 비밀번호 변경에 성공하였습니다.", userid));
            session.invalidate();
            return "redirect:/";
        }else{
            attrs.addFlashAttribute("msg", String.format("사용자(%s) 비밀번호 변경에 실패하였습니다.", userid));
            return "redirect:change_password";
        }
    }

    @GetMapping("/withdrawal")
    public String withdrawal() {
        return "withdrawal"; // withdrawal.jsp로 이동
    }

    @PostMapping("/withdrawal.do")
    public String withdrawalDo(RedirectAttributes attrs) {
        String userid = (String)session.getAttribute("userid");
        String sessionPassword = (String)session.getAttribute("password");
        String userPasswd = request.getParameter("passwd");
        String host = (String) session.getAttribute("host");
        log.debug("withdrawal.do called...");

        Pop3Agent pop3 = new Pop3Agent(host, userid, userPasswd);
        boolean deleteSuccessful = pop3.deleteUserMessage(userid, request, DOWNLOAD_FOLDER);

        if(deleteSuccessful){
            log.debug("{}의 메일함을 삭제하였습니다.", userid);
        }else{
            log.error("{}의 메일함 삭제에 실패하였습니다.", userid);
        }

        if(sessionPassword.equals(userPasswd)){
            log.debug("({})withdrawal.do succeeded.", userid);
            try {
                String cwd = ctx.getRealPath(".");
                UserAdminAgent agent = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT, cwd,
                        ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);
                agent.deleteUser(userid);  // 수정!!!
            } catch (Exception ex) {
                log.error("withdrawal.do : 예외 = {}", ex);
            }

            // 스프링 시큐리티 계정 삭제
            userService.deleteUser(userid, userPasswd);

            attrs.addFlashAttribute("msg", String.format("사용자(%s) 회원 탈퇴 성공하였습니다.", userid));
            session.invalidate();
            return "redirect:/";
        }
        else{
            log.debug("({})withdrawal.do failed...", userid);
            attrs.addFlashAttribute("msg", String.format("사용자(%s) 회원 탈퇴를 실패하였습니다.", userid));
            return "redirect:/withdrawal"; // withdrawal.jsp로 이동
        }
    }

    /** 회원가입 페이지 반환 메서드 */
    @GetMapping("/signup")
    public String signUp() {
        return "signup"; // signup.jsp로 이동
    }

    /**ID의 중복확인 AJAX 처리 메서드*/
    @PostMapping("/isUserIDDuplicate")
    @ResponseBody
    public Map<String, Boolean> isUserIDDuplicate(@RequestBody Map<String, String> request){
        Map<String, Boolean> result = new HashMap<>();

        String userid = request.get("userid");
        String cwd = ctx.getRealPath(".");
        boolean isDuplicate;
        
        UserAdminAgent agent = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT, cwd,
                                                  ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);
        isDuplicate = agent.isUserIDDuplicate(userid);
        result.put("duplicate", isDuplicate);
        return result;
    }

    /** 회원가입 수행 메서드*/
    @PostMapping("/signup.do")
    public String signUpDo(RedirectAttributes attrs) {
        String userid = request.getParameter("userid");
        String passwd = request.getParameter("passwd");
        String repasswd = request.getParameter("repasswd");
        log.debug("signUpDo() called...\n{}",attrs);


        if(passwd.length() < 8){
            attrs.addFlashAttribute("msg", "비밀번호는 8자 이상이어야 합니다.");
            return "redirect:/signup";
        }

        // 비밀번호 확인
        if(passwd.equals(repasswd)){
            attrs.addFlashAttribute("msg", "회원가입에 성공하였습니다.");
            // 중복 제거 가능성
            try {
                String cwd = ctx.getRealPath(".");
                UserAdminAgent agent = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT, cwd,
                        ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);
                // 메일서버 계정 추가
                if (agent.addUser(userid, passwd)) {
                    userService.addUser(userid, passwd);
                    attrs.addFlashAttribute("msg", String.format("사용자(%s) 회원가입에 성공하였습니다.", userid));
                } else {
                    attrs.addFlashAttribute("msg", String.format("사용자(%s) 회원가입에 실패하였습니다.", userid));
                }
            } catch (Exception ex) {
                log.error("signup.do: 시스템 접속에 실패했습니다. 예외 = {}", ex.getMessage());
            }

            return "redirect:/";
        }else{
            attrs.addFlashAttribute("msg", "비밀번호가 일치하지 않습니다.");
            return "redirect:/signup";
        }
    }

    @RequestMapping(value = "/login.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String loginDo(@RequestParam Integer menu) {
        String url = "";
        log.debug("로그인 처리: menu = {}", menu);
        switch (menu) {
            case CommandType.LOGIN:
                String host = (String) request.getSession().getAttribute("host");
//                String userid = request.getParameter("userid");
//                String password = request.getParameter("passwd");
                String userid = (String) session.getAttribute("userid");
                String password = (String) session.getAttribute("passwd");

                // Check the login information is valid using <<model>>Pop3Agent.
                Pop3Agent pop3Agent = new Pop3Agent(host, userid, password);
                boolean isLoginSuccess = pop3Agent.validate();

                // Now call the correct page according to its validation result.
                if (isLoginSuccess) {
                    if (isAdmin(userid)) {
                        // HttpSession 객체에 userid를 등록해 둔다.
                        session.setAttribute("userid", userid);
                        // response.sendRedirect("admin_menu.jsp");
                        url = "redirect:/admin_menu";
                    } else {
                        // HttpSession 객체에 userid와 password를 등록해 둔다.
                        session.setAttribute("userid", userid);
                        session.setAttribute("password", password);
                        // response.sendRedirect("main_menu.jsp");
                        url = "redirect:/main_menu";  // URL이 http://localhost:8080/webmail/main_menu 이와 같이 됨.
                        // url = "/main_menu";  // URL이 http://localhost:8080/webmail/login.do?menu=91 이와 같이 되어 안 좋음
                    }
                } else {
                    // RequestDispatcher view = request.getRequestDispatcher("login_fail.jsp");
                    // view.forward(request, response);
                    url = "redirect:/login_fail";
                }
                break;
            case CommandType.LOGOUT:
                session.invalidate();
                url = "redirect:/";  // redirect: 반드시 넣어야만 컨텍스트 루트로 갈 수 있음
                break;
            default:
                break;
        }
        return url;
    }

    @GetMapping("/login_fail")
    public String loginFail() {

        log.debug("로그인 실패 {} ", request.getParameter("userid"));
        return "login_fail";
    }

    protected boolean isAdmin(String userid) {
        boolean status = false;

        if (userid.equals(this.ADMINISTRATOR)) {
            status = true;
        }

        return status;
    }

    @GetMapping("/admin_menu")
    public String adminMenu(Model model) {
        log.debug("root.id = {}, root.password = {}, admin.id = {}",
                ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);

        model.addAttribute("userList", getUserList());
        return "admin/admin_menu";
    }

    @GetMapping("/add_user")
    public String addUser() {
        return "admin/add_user";
    }

    @PostMapping("/add_user.do")
    public String addUserDo(@RequestParam String id, @RequestParam String password,
            RedirectAttributes attrs) {
        log.debug("add_user.do: id = {}, password = {}, port = {}",
                id, password, JAMES_CONTROL_PORT);

        if(password.length() < 8){
            attrs.addFlashAttribute("msg", "비밀번호는 8자 이상이어야 합니다.");
            return "redirect:/add_user";
        }

        try {
            String cwd = ctx.getRealPath(".");
            UserAdminAgent agent = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT, cwd,
                    ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);

            // if (addUser successful)  사용자 등록 성공 팦업창
            // else 사용자 등록 실패 팝업창
            if (agent.addUser(id, password)) {
                userService.addUser(id, password);
                attrs.addFlashAttribute("msg", String.format("사용자(%s) 추가를 성공하였습니다.", id));
            } else {
                attrs.addFlashAttribute("msg", String.format("사용자(%s) 추가를 실패하였습니다.", id));
            }
        } catch (Exception ex) {
            log.error("add_user.do: 시스템 접속에 실패했습니다. 예외 = {}", ex.getMessage());
        }

        return "redirect:/admin_menu";
    }

    @GetMapping("/delete_user")
    public String deleteUser(Model model) {
        log.debug("delete_user called");
        model.addAttribute("userList", getUserList());
        return "admin/delete_user";
    }

    /**
     *
     * @param selectedUsers <input type=checkbox> 필드의 선택된 이메일 ID. 자료형: String[]
     * @param attrs
     * @return
     */
    @PostMapping("/delete_user.do")
    public String deleteUserDo(@RequestParam String[] selectedUsers, RedirectAttributes attrs) {
        String host = (String) session.getAttribute("host");
        log.debug("delete_user.do: selectedUser = {}", List.of(selectedUsers));

        try {
            String cwd = ctx.getRealPath(".");
            UserAdminAgent agent = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT, cwd,
                    ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);

            // 비밀번호를 초기화
            agent.resetPassword(selectedUsers);
            // 계정의 메일함 비우기
            for(String user : selectedUsers) {
                Pop3Agent pop3 = new Pop3Agent(host, user, "1234");
                if(pop3.deleteUserMessage(user, request, DOWNLOAD_FOLDER)){
                    log.debug("메일함 비움");
                }else{
                    log.debug("메일함 비우기 실패");
                }
            }
            UserAdminAgent agent2 = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT, cwd,
                    ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);
            // 이메일 서버의 계정 삭제
            agent2.deleteUsers(selectedUsers);
            // 스프링 시큐리티에서 계정제거
            userService.deleteUsers(selectedUsers);
        } catch (Exception ex) {
            log.error("delete_user.do : 예외 = {}", ex);
        }

        return "redirect:/admin_menu";
    }


    private List<String> getUserList() {
        String cwd = ctx.getRealPath(".");
        UserAdminAgent agent = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT, cwd,
                ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);
        List<String> userList = agent.getUserList();
        log.debug("userList = {}", userList);

        //(주의) root.id와 같이 '.'을 넣으면 안 됨.
        userList.sort((e1, e2) -> e1.compareTo(e2));
        return userList;
    }

    @GetMapping("/img_test")
    public String imgTest() {
        return "img_test/img_test";
    }

    /**
     * https://34codefactory.wordpress.com/2019/06/16/how-to-display-image-in-jsp-using-spring-code-factory/
     * 
     * @param imageName
     * @return 
     */
    @RequestMapping(value = "/get_image/{imageName}")
    @ResponseBody
    public byte[] getImage(@PathVariable String imageName) {
        try {
            String folderPath = ctx.getRealPath("/WEB-INF/views/img_test/img");
            return getImageBytes(folderPath, imageName);
        } catch (Exception e) {
            log.error("/get_image 예외: {}", e.getMessage());
        }
        return new byte[0];
    }

    private byte[] getImageBytes(String folderPath, String imageName) {
        ByteArrayOutputStream byteArrayOutputStream;
        BufferedImage bufferedImage;
        byte[] imageInByte;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            bufferedImage = ImageIO.read(new File(folderPath + File.separator + imageName) );
            String format = imageName.substring(imageName.lastIndexOf(".") + 1);
            ImageIO.write(bufferedImage, format, byteArrayOutputStream);
            byteArrayOutputStream.flush();
            imageInByte = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return imageInByte;

        } catch (FileNotFoundException e) {
            log.error("getImageBytes 예외: {}", e.getMessage());
        } catch (Exception e) {
            log.error("getImageBytes 예외: {}", e.getMessage());
        }
        return null;
    }

}
