package Kong.LoginPractice;

import Kong.LoginPractice.domain.member.Member;
import Kong.LoginPractice.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class TestDataInit {

    private final MemberRepository memberRepository;

    @PostConstruct
    public void init() {

        Member member = new Member("test", "tester", "test!");
        memberRepository.save(member);
    }
}
