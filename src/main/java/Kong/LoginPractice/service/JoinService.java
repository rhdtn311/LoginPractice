package Kong.LoginPractice.service;

import Kong.LoginPractice.domain.member.Member;
import Kong.LoginPractice.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JoinService {

    private final MemberRepository memberRepository;

    public void join(Member member) {

        log.info("join Member = {}", member);
        memberRepository.save(member);
    }
}
