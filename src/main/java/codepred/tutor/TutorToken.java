package codepred.tutor;

import codepred.account.AppUserRole;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TutorToken {

    private String token;

    private AppUserRole appUserRole;
}
