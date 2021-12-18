package Kong.LoginPractice;

import Kong.LoginPractice.domain.member.Member;
import Kong.LoginPractice.domain.member.MemberRepository;
import Kong.LoginPractice.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Controller
public class HomeController {

    private final MemberRepository memberRepository;
    private final MySession session;

    @RequestMapping("/")
    public String home(HttpServletRequest request, Model model) {

        Member member = (Member) session.getSession(request);
        if (member == null) {
            return "home";
        }

        model.addAttribute("member", member);
        log.info("member = {}" , member);
        return "loginHome";
    }

    @RequestMapping("/join")
    public String join() {

        return "/join";
    }

    @RequestMapping("/sessionLoginHome")
    public String sessionLoginHome(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember
            , Model model) {

        if (loginMember == null) {
            return "/login";
        }

        model.addAttribute("member", loginMember);
        return "loginHome";

    }


    @RequestMapping("/httpSessionLogin")
    public String httpSessionLogin() {
        return "/login";
    }
}
