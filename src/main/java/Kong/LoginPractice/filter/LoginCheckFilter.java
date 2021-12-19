package Kong.LoginPractice.filter;

import Kong.LoginPractice.SessionConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.PatternMatchUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
public class LoginCheckFilter implements Filter {

    private static final String[] permitUris = {"/", "/login", "/logout", "/join", "/sessionLoginHome", "/httpSessionLogin", "/httpSessionLogout", "/sessionLogin", "httpSessionLogin"};

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();

        try {

            log.info("인증 체크 필터 시작");
            if (!isPermitUris(requestURI)) {

                HttpSession session = httpRequest.getSession(false);

                if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {

                    log.info("미로그인 사용자 진입 차단");

                    log.info("requestURI = {} ", requestURI);

                    httpResponse.sendRedirect("/httpSessionLogin");

                    return;
                }
            }

            chain.doFilter(request, response);

        } catch (Exception e) {
            throw e;
        } finally {
            log.info("인증 체크 필터 종료 {}" , requestURI);
        }
    }

    private boolean isPermitUris(String url) {

        return PatternMatchUtils.simpleMatch(permitUris, url);
    }
}
