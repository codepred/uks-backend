package codepred.account.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class ChangePasswordRequest {
    @Schema(description = "old password", example = "de2kjn12j4n")
    private String password;

    private String newPassword;
    @JsonCreator
    public ChangePasswordRequest(@JsonProperty("password") String password, @JsonProperty("newPassword") String newPassword) {
        this.password = password;
        this.newPassword= newPassword;
    }
}
