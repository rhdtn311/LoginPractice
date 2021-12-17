package Kong.LoginPractice.Controller;

import Kong.LoginPractice.domain.member.Member;
import Kong.LoginPractice.dto.JoinDTO;
import Kong.LoginPractice.service.JoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class JoinController {

    private final JoinService joinService;

    @PostMapping("/join")
    public String join(@Validated @ModelAttribute JoinDTO joinDTO,
                       BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "/join";
        }

        Member joinMember = joinDTO.toMember();
        joinService.join(joinMember);

        log.info("joinMember = {}", joinMember);

        return "home";
    }
}
