package codepred.common.util;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseObject {

    @Schema(description = "HTTP response", example = "OK/ACCEPTED/BAD_REQUEST and so on")
    private HttpStatus code;

    @Schema(description = "Response from backend", example = "Something went wrong/Successful")
    private String message;

    @Schema(description = "authentication token", example = "gfrekjn123kmfvkq")
    private String token;


}
