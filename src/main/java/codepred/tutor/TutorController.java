package codepred.tutor;

import codepred.account.User;
import codepred.account.dto.ChangePasswordRequest;
import codepred.account.dto.ResetPasswordRequest;
import codepred.common.util.ResponseObject;
import codepred.config.files.FileDto;
import codepred.service.dto.AddServiceRequest;
import codepred.tutor.dto.NewTutorPersonalDataRequest;
import codepred.tutor.dto.NewTutorRequest;
import codepred.tutor.dto.UpdateTutorRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;


public interface TutorController {

    @PostMapping("/signup")
    @Operation(summary = "Create a new tutor account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Email sent for verification"),
            @ApiResponse(responseCode = "202", description = "Verification code was sent once again"),
            @ApiResponse(responseCode = "400", description = "User already added")
    })
    @ResponseStatus(HttpStatus.ACCEPTED)
    ResponseObject tutorRegisterAccount(@RequestBody @Valid NewTutorRequest NewTutorRequest);

    @GetMapping("/confirm/{code}")
    @Operation(summary = "Endpoint to confirm tutor registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tutor registration confirmed"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @ResponseStatus(HttpStatus.CREATED)
    ResponseObject tutorConfirmRegistration(@PathVariable("code") String code);

    // LOGIN
    @PostMapping("/signin")
    @Operation(summary = "Endpoint to login tutor", hidden = true)
    @ApiResponses(value = {//
            @ApiResponse(responseCode = "202", description = "Login successful"), //
            @ApiResponse(responseCode = "400", description = "Bad Request: Tutor was not registered"), //
            @ApiResponse(responseCode = "401", description = "Wrong data supplied"), //
            @ApiResponse(responseCode = "422", description = "Verification code was sent once again"),
    })
    ResponseObject login(@RequestBody NewTutorRequest userLoginDto);

    @PostMapping("/resetpassword/{email}")
    @Operation(summary = "Endpoint to reset tutor password")
    @ApiResponses(value = {//
            @ApiResponse(responseCode = "400", description = "Something went wrong"), //
            @ApiResponse(responseCode = "422", description = "Invalid username/password supplied")})
    ResponseObject resetPassword(@PathVariable("email") String email);

    @PatchMapping("/resetpassword/{code}")
    @Operation(summary = "Endpoint to check code from mail to reset password")
    @ApiResponses(value = {//
            @ApiResponse(responseCode = "401", description = "Invalid code provided"), //
            @ApiResponse(responseCode = "422", description = "Account is not activated"), //
            @ApiResponse(responseCode = "202", description = "Password reset code accepted, new code generated"), //
            @ApiResponse(responseCode = "400", description = "Provided code has expired")})
    ResponseObject checkCodeForPasswordResetting(@PathVariable("code") String code);

    @PostMapping("/confirmresetting")
    @Operation(summary = "Endpoint to confirm resetting tutor password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Something went wrong"),
            @ApiResponse(responseCode = "422", description = "Invalid code supplied")})
    ResponseObject confirmPasswordResetting(@RequestBody ResetPasswordRequest ResetPasswordRequest);

    @GetMapping("/first-login-check")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Endpoint to check if it is first login of a tutor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Something went wrong"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    ResponseObject firstLoginCheck(HttpServletRequest request);

    @PutMapping("/personal-data")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Endpoint to update personal tutor's data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Something went wrong"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    ResponseObject updatePersonalData(HttpServletRequest request, @RequestBody NewTutorPersonalDataRequest NewTutorPersonalDataRequest);

    @PutMapping("/service-add")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Endpoint to add service to tutor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Something went wrong"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
        ResponseObject addServiceToTutor(HttpServletRequest request, @RequestBody AddServiceRequest AddServiceRequest);

    @GetMapping("/get-tutor-data")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get tutor data", description = "Get current login tutor data.")
    @ApiResponses(value={
            @ApiResponse(responseCode = "200", description = "Data successfully loaded."),
            })
    User getCurrentTutorData(HttpServletRequest request);

    @PutMapping("/reset-password")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Reset password", description = "Reset user password.")
    @ApiResponses(value={
            @ApiResponse(responseCode = "202", description = "PASSWORD_SUCCESSFULLY_UPDATED"),
    })
    ResponseObject resetPassword(HttpServletRequest request, @RequestBody ChangePasswordRequest password);

    @DeleteMapping("/remove-account")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Remove account", description = "Remove tutor account.")
    @ApiResponse(responseCode = "202",description = "USER_SUCCESSFULLY_REMOVED")
    ResponseObject removeAccount(HttpServletRequest request);

    @PutMapping("/update-personal-data")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Update personal tutor data", description = "Update with updating avatar.")
    @ApiResponse(responseCode = "200", description = "DATA_UPDATED")
    ResponseObject updatePersonalTutorData(HttpServletRequest request, @ModelAttribute UpdateTutorRequest tutorData);

    @PostMapping("/profiles/set-bookmarks")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Set bookmarks data", description = "Set tutor bookmarks data.")
    @ApiResponse(responseCode = "200", description = "DATA_UPDATED")
    ResponseObject setBookmarks(HttpServletRequest request,
                                @RequestParam(required = false) MultipartFile file1,
                                @RequestParam(required = false) String displayedName1,
                                @RequestParam(required = false) Boolean update1,
                                @RequestParam(required = false) String bookmarkId1,
                                @RequestParam(required = false) MultipartFile file2,
                                @RequestParam(required = false) String displayedName2,
                                @RequestParam(required = false) Boolean update2,
                                @RequestParam(required = false) String bookmarkId2,
                                @RequestParam(required = false) MultipartFile file3,
                                @RequestParam(required = false) String displayedName3,
                                @RequestParam(required = false) Boolean update3,
                                @RequestParam(required = false) String bookmarkId3) throws IOException;

    @GetMapping("/profiles/get-list")
    @PreAuthorize("hasRole('ROLE_TUTOR') or hasRole('ROLE_CLIENT')")
    @Operation(summary = "Update personal tutor data", description = "Update with updating avatar.")
    @ApiResponse(responseCode = "200", description = "DATA_UPDATED")
    ResponseEntity<List<FileDto>> getBookmarkList(HttpServletRequest request);
}
