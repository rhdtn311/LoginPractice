package Kong.LoginPractice.interceptor;

import Kong.LoginPractice.SessionConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();

        HttpSession session = request.getSession();

        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {

            log.info("미로그인 사용자 접근 제한");

            log.info("requestURI int interceptor: {} ", requestURI);
            response.sendRedirect("/httpSessionLogin?redirectURL=" + requestURI);
            return false;
        }

        return true;
    }
}
