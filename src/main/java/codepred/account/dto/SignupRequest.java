package codepred.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class SignupRequest {
    @NotNull
    @NotEmpty(message = "CODE_MUST_BY_NOT_EMPTY")
    @Schema(description = "code from backend", example = "1jbf31j4h3bt4jb12j4")
    private String code;

    @Schema(description = "client email", example = "client@example.pl")
    @Email
    private String email;

    @NotNull
    @NotEmpty(message = "PASSWORD_MUST_BY_NOT_EMPTY")
    @Schema(description = "new password for client", example = "21jbr43k12kj4b")
    private String password;
}
