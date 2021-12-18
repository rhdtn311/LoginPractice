package Kong.LoginPractice.Controller;

import Kong.LoginPractice.MySession;
import Kong.LoginPractice.SessionConst;
import Kong.LoginPractice.domain.member.Member;
import Kong.LoginPractice.dto.LoginDTO;
import Kong.LoginPractice.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
@RequiredArgsConstructor
@Controller
public class LoginController {

    private final LoginService loginService;
    private final MySession session;

    @PostMapping("/login")
    public String login(@Validated @ModelAttribute LoginDTO loginDTO,
                        BindingResult bindingResult,
                        HttpServletResponse response,
                        Model model) {

        if (bindingResult.hasErrors()) {
            return "/home";
        }

        Member loginMember = loginService.login(loginDTO);
        if (loginMember == null) {
            bindingResult.reject("loginFail", "아이디와 비밀번호를 확인해주세요.");

            return "/home";
        }

        session.createSession(response, loginMember);

        return "redirect:/";

    }

    @RequestMapping("/logout")
    public String logout(HttpServletRequest request) {

        session.removeSession(request);

        return "/home";
    }

    @PostMapping("/sessionLogin")
    public String sessionLogin(@Validated @ModelAttribute LoginDTO loginDTO,
                               BindingResult bindingResult,
                               HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        Member loginMember = loginService.login(loginDTO);
        if (loginMember == null) {
            bindingResult.reject("loginFail", "아이디와 비밀번호를 확인해주세요.");
            return "login";
        }

        // 로그인 성공
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);

        return "redirect:/sessionLoginHome";
    }

    @RequestMapping("/sessionLogout")
    public String sessionLogout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        log.info("session = {}" , session.getAttribute(SessionConst.LOGIN_MEMBER));

        if (session != null) {
            session.invalidate();

            log.info("session = {}" , session.getAttribute(SessionConst.LOGIN_MEMBER));
        }
        return "redirect:/sessionLoginHome";
    }
}
