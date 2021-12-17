package Kong.LoginPractice.Controller;

import Kong.LoginPractice.MySession;
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
}
