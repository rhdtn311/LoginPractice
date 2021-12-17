쿠키와 세션을 이용하여 로그인 기능을 구현해 보았다.

## 1. 세션 직접 구현

세션을 이해하기 위해 직접 세션을 구현하여 로그인 기능을 구현하였다.

**프로젝트 구성**

- SpringMVC 5.3.13
- ThymeLeaf 2.6.1
- Lombok

```bash
├─main
│  ├─java
│  │  └─Kong
│  │      └─LoginPractice
│  │          │  HomeController.java
│  │          │  LoginPracticeApplication.java
│  │          │  MySession.java
│  │          │  TestDataInit.java
│  │          │
│  │          ├─Controller
│  │          │      JoinController.java
│  │          │      LoginController.java
│  │          │
│  │          ├─domain
│  │          │  └─member
│  │          │          Member.java
│  │          │          MemberRepository.java
│  │          │
│  │          ├─dto
│  │          │      JoinDTO.java
│  │          │      LoginDTO.java
│  │          │
│  │          └─service
│  │                  JoinService.java
│  │                  LoginService.java
│  │
│  └─resources
│      │  application.properties
│      │
│      ├─static
│      └─templates
│              home.html
│              join.html
│              loginHome.html
│
└─test
    └─java
        └─Kong
            └─LoginPractice
                    LoginPracticeApplicationTests.java
```

<br>

### 내용

메인 화면은 다음과 같다.

![image](https://user-images.githubusercontent.com/68289543/146542165-5f710101-0a78-41bf-bca7-2a5c3dcf1640.png)

아이디, 비밀번호를 입력할 수 있으며 로그인 버튼과 회원가입 버튼이 있다.

![image](https://user-images.githubusercontent.com/68289543/146542212-3db31965-3a02-4c32-bf0f-c107a874bcd8.png)

회원 가입은 간단하게 이름, 아이디, 비밀번호를 입력하여 회원가입 한다.

<br>

### 코드

**Member.java**

```java
package Kong.LoginPractice.domain.member;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class Member {

    private Long id;

    @NotEmpty
    private String loginId;

    @NotEmpty
    private String name;

    @NotEmpty
    private String password;

    public Member(String loginId, String name, String password) {
        this.loginId = loginId;
        this.name = name;
        this.password = password;
    }

}

```

회원 객체

**MemberRepository.java**

```java
package Kong.LoginPractice.domain.member;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MemberRepository {

    private static Map<Long, Member> store = new ConcurrentHashMap<>();
    private static long sequence = 0L;

    public Member save(Member member) {

        member.setId(++sequence);

        store.put(member.getId(), member);

        return member;
    }

    public Member findById(Long id) {

        return store.get(id);
    }

    public Optional<Member> findByLoginId(String loginId) {

        return findAll().stream().filter(m -> m.getLoginId().equals(loginId)).findFirst();
    }

    public List<Member> findAll() {

        return new ArrayList<>(store.values());
    }
}

```

`Member` 객체를 저장하기 위한 Repository

**JoinService.java**

```java
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

```

회원 가입 시 `Member` 객체를 Repository에 저장하는 Service

**LoginService.java**

```java
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

```

로그인 시 해당 입력 값이 `MemberRepository`에 저장되어 있고, `password` 또한 같다면 해당 `Member`를 반환, 없다면 `null`을 반환

**JoinDTO.java**

```java
package Kong.LoginPractice.dto;

import Kong.LoginPractice.domain.member.Member;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class JoinDTO {

    @NotNull
    private String name;

    @NotNull
    private String id;

    @NotNull
    private String password;

    public Member toMember() {
        return new Member(id, name, password);
    }
}

```

회원가입을 위한 DTO

**LoginDTO.java**

```java
package Kong.LoginPractice.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LoginDTO {

    @NotNull
    private String loginId;

    @NotNull
    private String password;
}

```

로그인을 위한 DTO

**JoinController.java**

```java
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

```

회원가입 컨트롤러

**MySession.java**

```java
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

```

- 직접 구현한 세션
- `sessionStore` : 로그인에 성공했을 경우 `Member` 객체와 무작위로 생성된 `uuid`를 매칭하기 위한 저장소
- `SESSION_ID` :  쿠키에 저장되는 `key` 값
- `createSession(HttpSerlvetResponse response,  Object value)`
  - 로그인에 성공하면 실행되는 메서드로 세션을 생성한다.
  - 회원 마다 무작위의 `uuid`를 생성하고 `sessionStore`에 `key` 값을 `uuid`로, `key`에 대응하는 값을 `Member` 객체로 한다.
  - 쿠키를 생성하여 `name`을 `SESSOIN_ID`로, 대응하는 값을 생성한 `uuid`로 하여 `response`에 추가한다.
- `getSession(HttpServletRequest request)`
  - 로그인에 성공하고 클라이언트로부터 오는 `request`의 쿠키의 `name`이 `SESSION_ID`인 쿠키가 있다면 해당 쿠키의 `value`를 리턴한다.
  - 없다면 `null`을 리턴한다.
- `removeSession(HttpServletRequest request)`
  - 클라이언트로부터 오는 `request`의 쿠키의 `name`이 `SESSION_ID`인 쿠키가 있다면 `sessionStore`에서 제거한다.
  - 없다면 `null`을 리턴한다.

**LoginController.java**

```java
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

```

- `/login`
  - 입력값 검증에 실패하거나 입력한 아이디, 비밀번호에 맞는 `Member` 객체가 존재하지 않는다면 `home.html`을 리턴한다.
  - 로그인에 성공한다면 `MySession`의 `createSession()`  메서드를 호출하여 `sessionStore`에 `uuid`를 생성하여 `Member` 객체를 저장하고 쿠키를 `response`에 추가한다.
- `/logout`
  - `MySession`의 `removeSession()` 메서드를 호출하여 `request`의 쿠키에 있는 `uuid` 값으로  `sessionStore`에 저장되어 있는 `Member` 객체를 제거한다.

**HomeController.java**

```java
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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
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
}

```

- `"/"`
  - `MySession()`의 `getSession()` 메서드를 호출하여 `request`의 쿠키를 조회하여 해당 쿠키의 값(`uuid`)에 대응하는 `Member` 객체가 존재한다면 `loginHome.html`로 이동, 없다면 `home.html`로 이동한다.

<br>

### 결과

- **회원 가입**

  ![image](https://user-images.githubusercontent.com/68289543/146546696-72f38b4d-3576-48c4-8be8-a46fc3025884.png)

  

  ![image](https://user-images.githubusercontent.com/68289543/146546738-1a6a7b2b-9b01-4d58-89b7-e722386ba9a8.png)

- **로그인**

  ​	![image](https://user-images.githubusercontent.com/68289543/146548115-c6f3c1e8-0951-4654-8212-20ee6b58947f.png)

  ​	![image](https://user-images.githubusercontent.com/68289543/146546940-0da67e48-a1f0-4835-8190-ab29f07f05e6.png)

  ​	  ![image](https://user-images.githubusercontent.com/68289543/146547644-d0019e33-843d-4b19-a5ac-dad5b6f3fb8b.png)

  ResponseHeader에 `MySessoin`에서 생성한 `uuid` 값이 쿠키에 저장되어 잘 전달되었다.

  ​	![image](https://user-images.githubusercontent.com/68289543/146547722-ba9895f0-8a55-4791-8894-bc0ffb8d210a.png)

- **로그아웃**

  ![image](https://user-images.githubusercontent.com/68289543/146547802-2c2402c8-abf8-4546-b52d-972db95beef6.png)

  `MySession`의 `sessionStore`에 저장되어 있던 객체가 삭제되었다.

<br>

