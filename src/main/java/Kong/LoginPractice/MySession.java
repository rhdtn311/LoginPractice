package Kong.LoginPractice;

import Kong.LoginPractice.domain.member.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MySession {

    private static Map<String, Object> sessionStore = new ConcurrentHashMap<>();
    private static final String SESSION_ID = "sessionMemberId";

    // 세션 생성
    public void createSession(HttpServletResponse response, Object value) {

        String uuid = UUID.randomUUID().toString();
        sessionStore.put(uuid, value);

        Cookie cookie = new Cookie(SESSION_ID, uuid);
        response.addCookie(cookie);
    }

    // 세션 조회
    public Object getSession(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(SESSION_ID)) {
                Object member = sessionStore.get(cookie.getValue());

                return member;
            }
        }

        return null;
    }

    // 세션 삭제
    public void removeSession(HttpServletRequest request) {

        log.info("sessionStore : {} " , sessionStore);
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(SESSION_ID)) {
                sessionStore.remove(cookie.getValue());
            }
        }

        log.info("sessionStore : {} " , sessionStore);
    }

}
