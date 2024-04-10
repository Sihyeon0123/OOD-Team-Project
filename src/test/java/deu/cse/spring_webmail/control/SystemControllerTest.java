package deu.cse.spring_webmail.control;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.ui.ModelMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SystemControllerTest {

    @Test
    @DisplayName("index 테스트")
    void index() {
        // Given
        SystemController controller = new SystemController();
        MockServletContext servletContext = new MockServletContext();
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest request = new MockHttpServletRequest();
        controller.setCtx(servletContext);
        controller.setSession(session);
        controller.setRequest(request);
        ModelMap model = new ModelMap();

        // When
        String viewName = controller.index();

        // Then
        assertEquals("/index", viewName);
        assertEquals("false", session.getAttribute("debug"));
        // 이하 session에 설정된 값들 확인
    }
}
