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
        
        if (cookies == null) {
            return null;
        }

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
        
        if (request.getCookies() == null) {
            return;
        }
        
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

## 2. 서블릿 HTTP 세션 사용

스프링에서 제공하는 세션 기능을 사용하였다.

### 프로젝트 구성

위와 같음

```bash
├─main
│  ├─java
│  │  └─Kong
│  │      └─LoginPractice
│  │          │  HomeController.java
│  │          │  LoginPracticeApplication.java
│  │          │  MySession.java
│  │          │  SessionConst.java
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
│              login.html
│              loginHome.html
│
└─test
    └─java
        └─Kong
            └─LoginPractice
                    LoginPracticeApplicationTests.java
```

직접 구현한 세션 프로젝트에 일부 추가하였다.

<br>

### 내용

메인 화면에 '서블릿 HTTP 세션 로그인 페이지로 이동' 버튼이 추가되었다. 해당 버튼을 누르면 서블릿 HTTP 세션을 사용한 로그인 페이지로 이동한다.

![image](https://user-images.githubusercontent.com/68289543/146642649-3e159139-0803-41e3-846f-d72e45b817f4.png)

### 코드

나머지는 위와 같고 새로 추가된 코드만 작성한다.

**SessionConst.java**

```java
package Kong.LoginPractice;

public interface SessionConst {
    public static final String LOGIN_MEMBER = "loginMember";
}
```

세션에 `Member` 객체를 저장하기 위한 `key` 값은 모두 같기 때문에 상수로 지정하기 위해 생성한 인터페이스

**login.html**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
    <form th:action="@{/sessionLogin}" th:method="post">
        <div>
            <label for="loginId"> 아이디 </label>
            <input type="text" id="loginId" name="loginId" placeholder="아이디를 입력하세요.">
        </div>
        <div>
            <label for="password">비밀번호</label>
            <input type="password" id="password" name="password" placeholder="비밀번호를 입력하세요.">
        </div>
        <button type="submit"> 로그인 </button>
    </form>
</body>
</html>
```

서블릿 HTTP 세션 기능을 사용하는 url로 Form 메시지를 전송할 화면

**LoginController.java - 추가**

```java
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

    // 추가
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

    // 추가
    @PostMapping("/sessionLogout")
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

```

기존 코드에서 `/sessionLogin`과 `/sessionLogout`이 추가되었다.

- `sessionLogin()`
  - 로그인에 성공하면 `request.getSession()` 메서드를 통해 세션을 생성한다.
    - 이때, 서버에서 `JSESSIONID`을 무작위로 생성하여 생성한 세션의 id로 지정한다.
    - `getSession()`의 옵션값을 `true`로 설정했기 때문에 로그인 성공 시 해당 클라이언트에 세션이 없었어도 새로운  세션을 서버에서 생성한다.
  - 세션을 생성하고 해당 세션에 키(`name`)는 `SessionConst.LOGIN_MEMBER`로 하고 값은 `Member` 객체로 갖는 값을 저장한다.
- `sessionLogout()`
  - `session.invalidate()` : 세션을 제거한다.

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

    // 추가
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

```

- `@SessionAttribute()`
  - 세션에서 `name`이 `LoginMember`인 `Member`를 찾아 매핑한다.  로그인을 하지 않은 사용자도 해당 `url`에 접근할 수 있기 때문에 `required = false`로 지정한다.

<br>

### 결과

- **로그인**

  ![image](https://user-images.githubusercontent.com/68289543/146643169-218d5227-090f-44e1-b789-266b91739aa0.png)

  ![image-20211218230641061](C:\Users\태현\AppData\Roaming\Typora\typora-user-images\image-20211218230641061.png)

  정상적으로 `JSESSIONID`가 생성되어 쿠키에 저장되었다.

  ![image-20211218230620686](C:\Users\태현\AppData\Roaming\Typora\typora-user-images\image-20211218230620686.png)

- 로그아웃

  ![image](https://user-images.githubusercontent.com/68289543/146643882-7fa8bbf3-ba09-478b-b18d-84164c88a696.png)

  로그아웃 전에는 `session.getAttribute(loginMember)`를 로그로 찍으면 위와 같이 세션에 저장되어 있는 `Member` 객체가 출력되었으나 로그아웃 후에는

  ![image-20211218230814361](C:\Users\태현\AppData\Roaming\Typora\typora-user-images\image-20211218230814361.png)

  위와 같이 에러가 발생하고 세션이 이미 무효화 되었다는 메세지가 출력된다.

<br>

## 3. 로그인한 사용자만 호출할 수 있는 URI - 필터 

로그인한 사용자만 들어갈 수 있는 페이지를 만들어 필터를 사용하여 로그인 한 사용자만 특정 url을 호출할 수 있는 기능을 구현하였다. 

**필터 흐름**

```
HTTP 요청 -> WAS -> 필터1 -> 필터2 .. -> 서블릿 -> 컨트롤러
```

로그인 실패 시 필터 부분 후에 서블릿을 호출하지 않는다.

<br>

### 프로젝트 구성

위와 같음

```bash
├─main
│  ├─java
│  │  └─Kong
│  │      └─LoginPractice
│  │          │  HomeController.java
│  │          │  LoginPracticeApplication.java
│  │          │  MySession.java
│  │          │  SessionConst.java
│  │          │  TestDataInit.java
│  │          │
│  │          ├─config
│  │          │      WebConfig.java
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
│  │          ├─filter
│  │          │      LoginCheckFilter.java
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
│              login.html
│              loginHome.html
│              loginMemberPage.html
│
└─test
    └─java
        └─Kong
            └─LoginPractice
                    LoginPracticeApplicationTests.java
```

<br>

### 내용 - 추가

로그인한 사용자만 들어갈 수 있는 페이지를 만들었다.

![image](https://user-images.githubusercontent.com/68289543/146668297-9d2619ef-51a3-41e1-a2d7-cd6ad7934cc9.png)

<br>

### 코드  - 추가

**LoginCheckFilter.java**

```java
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

```

- `implements Filter`
  - 필터를 사용하기 위해서 필터 인터페이스를 구현해야 한다.
- `String[] permitUris`
  - 로그인을 하지 않아도 호출해야 하는 `uri`를 배열에 저장하였다.
- `doFilter()`
  - 요청 URI가 로그인한 사용자만 호출할 수 있는 URI인 경우 로그인 여부를 확인하여 로그인을 하지 않은 사용자라면 로그인을 할 수 있도록 `/httpSessionLogin`으로 리다이렉트한다.
  - `chain.doFilter()` : 다음 필터가 있으면 필터를 호출하고, 없으면 서블릿을 호출한다. 이 메서드를 호출하지 않으면 다음 단계로 넘어가지 않는다.

**WebConfig.java**

```java
package Kong.LoginPractice.config;

import Kong.LoginPractice.filter.LoginCheckFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean loginCheckFilter() {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new LoginCheckFilter());
        filterRegistrationBean.setOrder(1);
        filterRegistrationBean.addUrlPatterns("/*");

        return filterRegistrationBean;
    }
}

```

- `FilterRegistrationBean`을 이용하여 필터를 등록한다.
- `addUrlPattrens("/")` : 모든 URL에 필터를 적용한다.

**HomeController.java - 추가**

```java
@RequestMapping("/loginMemberPage")
public String loginMemberPage() {
    return "loginMemberPage";
}
```

로그인한 사용자만 접근할 수 있는 페이지

<br>

### 결과

- **로그인을 하지 않았을 경우 - `/loginMemberPage` 호출 **
  ![GIF](https://user-images.githubusercontent.com/68289543/146668588-c28b3b83-0445-401c-8e8e-c74e969dc1d8.gif)

  정상적으로 `httpSessionLogin` url을 호출하여 로그인 창으로 이동

  <img src="https://user-images.githubusercontent.com/68289543/146669704-9804362a-af1b-4cbb-bde5-ca08d96a2c06.png" alt="image" style="zoom:67%;" />

- **로그인을 했을 경우 - `/loginMemerPage` 호출**

  ![GIF](https://user-images.githubusercontent.com/68289543/146668654-47ce1cb8-afa1-41c9-b6f6-5c5199a5d187.gif)

  로그인에 성공하여 정상적으로 `/loginMemberPage` url 호출

<img src="https://user-images.githubusercontent.com/68289543/146669715-91547840-f6e5-4a88-bd42-968b6c89eec8.png" alt="image" style="zoom:67%;" />

<br>

## 4. 로그인한 사용자만 호출할 수 있는 URI - 인터셉터

필터로 했던 기능을 인터셉터로 바꾸어 보았다. 서블릿 필터는 서블릿이 제공하지만 스프링 인터셉터는 스프링 MVC가 제공하는 기능이다. 필터보다 더 많은 기능을 제공한다.

**인터셉터 흐름**

```
HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 인터셉터1 -> 인터셉터2 ... -> 컨트롤러
```

- 디스패처 서블릿과 컨트롤러 사이에서 컨트롤러 호출 직전에 호출된다.
- 로그인을 하지 않은 사용자가 접근한다면 인터셉터에서 걸려, 컨트롤러를 호출하지 않는다.
- **인터셉터의 3개의 흐름**
  - `preHandle` : 컨트롤러 호출 전에 호출된다.
  - `postHandle` : 컨트롤러 호출 후에 호출된다.
    - 컨트롤러에서 예외가 발생하면 호출되지 않는다.
  - `afterCompletion` : 뷰가 렌더링 된 이후에 호출된다. (항상 호출된다.)

<br>

### 코드

**LoginCheckInterceptor.java**

```java
package Kong.LoginPractice.interceptor;

import Kong.LoginPractice.SessionConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();

        HttpSession session = request.getSession();

        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {

            log.info("미로그인 사용자 접근 제한");

            log.info("requestURI int interceptor: {} ", requestURI);
            response.sendRedirect("/httpSessionLogin?redirectURL=" + requestURI);
            return false;
        }

        return true;
    }
}

```

- `implements HandlerInterceptor`
- 로그인한 사용자가 해당 URI를 호출하지 못하게 해야하기 때문에 `preHandler`를 구현한다.

**WebConfig.java - 추가**

```java
package Kong.LoginPractice.config;

import Kong.LoginPractice.filter.LoginCheckFilter;
import Kong.LoginPractice.interceptor.LoginCheckInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    // 생략

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor())
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/login", "/logout", "/join", "/sessionLoginHome", "/httpSessionLogin", "/httpSessionLogout", "/sessionLogin");
    }
}

```

- `WebMvcConfigurer`를 상속받아 `addInterceptors()`를 사용해서 인터셉터를 등록한다.
  - `addInterceptor()` : 인터셉터 등록
  - `order()` : 인터셉터의 호출 순서 지정
  - `addPathPatterns()` : 인터셉터를 적용할 URL 패턴을 지정
  - `excludePathPatterns()` : 인터셉터에서 제외할 패턴을 지정
<br>
<br>
<br>
<br>
<br>
---
참고 : 인프런 김영한 - 스프링 MVC 2
