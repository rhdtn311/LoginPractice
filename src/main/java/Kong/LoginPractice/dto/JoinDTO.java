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
