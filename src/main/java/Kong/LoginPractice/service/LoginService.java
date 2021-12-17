package Kong.LoginPractice.service;

import Kong.LoginPractice.domain.member.Member;
import Kong.LoginPractice.domain.member.MemberRepository;
import Kong.LoginPractice.dto.LoginDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;

    public Member login(LoginDTO loginDTO) {

        Member loginMember = memberRepository.findByLoginId(loginDTO.getLoginId()).orElse(null);

        return loginMember.getPassword().equals(loginDTO.getPassword()) ? loginMember : null;
    }

}
