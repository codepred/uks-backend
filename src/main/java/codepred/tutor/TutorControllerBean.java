package codepred.tutor;

import codepred.account.User;
import codepred.account.dto.ChangePasswordRequest;
import codepred.account.dto.ResetPasswordRequest;
import codepred.bookmark.BookMarkService;
import codepred.common.mapper.TutorMapper;
import codepred.common.util.ResponseObject;
import codepred.config.files.FileDto;
import codepred.config.files.FileListDto;
import codepred.meeting.EventService;
import codepred.service.dto.AddServiceRequest;
import codepred.tutor.dto.NewTutorPersonalDataRequest;
import codepred.tutor.dto.NewTutorRequest;
import codepred.tutor.dto.UpdateTutorRequest;
import codepred.whitelist.WhitelistService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/tutor", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Api(value = "Tutor", tags = "Tutor")
@Tag(name = "Tutor", description = "Tutor API")
@CrossOrigin
public class TutorControllerBean implements TutorController {

    private final TutorMapper tutorMapper;
    private final TutorService tutorService;
    private final BookMarkService bookMarkService;
    private final WhitelistService whitelistService;
    private final EventService eventService;

    @Override
    public ResponseObject tutorRegisterAccount(@Valid NewTutorRequest NewTutorRequest) {
        if(!whitelistService.canAddTutor(NewTutorRequest.getEmail())){
            return new ResponseObject(HttpStatus.BAD_REQUEST,"EMAIL_IS_NOT_ON_WHITELIST",null,null);
        }

        final var newTutor = tutorMapper.NewTutorRequestToTutor(NewTutorRequest);
        final var dbTutor = tutorService.createTutor(newTutor);
        return dbTutor;
    }

    @Override
    public ResponseObject tutorConfirmRegistration(String code) {
        return tutorService.confirmRegistration(code);
    }

    @Override
    public ResponseObject login(NewTutorRequest userLoginDto) {
        return tutorService.signin(tutorMapper.NewTutorRequestToTutor(userLoginDto));
    }

    public ResponseObject resetPassword(String email) {
        return tutorService.sendEmailToPasswordReset(email);
    }

    @Override
    public ResponseObject checkCodeForPasswordResetting(String code) {
        return tutorService.checkCodeForPasswordResetting(code);
    }

    public ResponseObject confirmPasswordResetting(ResetPasswordRequest passwordResetDTO) {
        return tutorService.passwordResetting(passwordResetDTO.getCode(), passwordResetDTO.getPassword());
    }

    @Override
    public ResponseObject firstLoginCheck(HttpServletRequest request) {
        var tutor = tutorService.getTutorByToken(request);
        var loginChecker = tutorService.checkIsItFirstLogin(tutor);
        loginChecker.setToken(request.getHeader("Authorization").substring(7));
        return loginChecker;
    }

    @Override
    public ResponseObject updatePersonalData(HttpServletRequest request, NewTutorPersonalDataRequest NewTutorPersonalDataRequest) {
        var tutorFromDB = tutorService.getTutorByToken(request);
        var updatedTutor = tutorService.updatePersonalTutorData(tutorFromDB, tutorMapper.requestPersonalTutorDataToTutor(NewTutorPersonalDataRequest));
        return new ResponseObject(HttpStatus.OK, "DATA_SUCCESSFULLY_UPDATED", request.getHeader("Authorization").substring(7));
    }

    @Override
    public ResponseObject addServiceToTutor(HttpServletRequest request, AddServiceRequest AddServiceRequest) {
        var tutorFromDb = tutorService.getTutorByToken(request);
        var tutorWithAddedService = tutorService.saveServiceAndAddToTutor(tutorFromDb, AddServiceRequest);
        tutorWithAddedService.setToken(request.getHeader("Authorization").substring(7));
        return new ResponseObject(HttpStatus.CREATED, "SERVICE_SUCCESSFULLY_ADD", request.getHeader("Authorization").substring(7));
    }


    @Override
    public User getCurrentTutorData(HttpServletRequest request) {
        var tutor = tutorService.getTutorByToken(request);
        return tutorService.getTutorDataByToken(tutor);
    }

    @Override
    public ResponseObject resetPassword(HttpServletRequest request, ChangePasswordRequest password) {
        var tutor = tutorService.getTutorByToken(request);
        return tutorService.resetPassword(tutor, password.getPassword(), password.getNewPassword());
    }

    @Override
    public ResponseObject removeAccount(HttpServletRequest request) {
        var tutor = tutorService.getTutorByToken(request);
        return tutorService.removeTutor(tutor);
    }

    @Override
    public ResponseObject updatePersonalTutorData(HttpServletRequest request, UpdateTutorRequest tutorData) {
        var tutor = tutorService.getTutorByToken(request);
        return tutorService.updatePersonalTutorData(tutor, tutorData);
    }

    @Override
    public ResponseObject setBookmarks(HttpServletRequest request,
                                       MultipartFile file1,
                                       String displayedName1,
                                       Boolean update1,
                                       String bookmarkId1,
                                       MultipartFile file2,
                                       String displayedName2,
                                       Boolean update2,
                                       String bookmarkId2,
                                       MultipartFile file3,
                                       String displayedName3,
                                       Boolean update3,
                                       String bookmarkId3) throws IOException {
        Integer bookId1 = null;
        Integer bookId2 = null;
        Integer bookId3 = null;
        try{
            bookId1 = Integer.valueOf(bookmarkId1);
        } catch (Exception e){
            System.out.println("ID 1 NULL");
        }
        try{
            bookId2 = Integer.valueOf(bookmarkId2);
        } catch (Exception e){
            System.out.println("ID 2 NULL");
        }
        try{
            bookId3 = Integer.valueOf(bookmarkId3);
        } catch (Exception e){
            System.out.println("ID 3 NULL");
        }

        FileListDto fileListDto = new FileListDto(file1,displayedName1, bookId1, file2,displayedName2,
                                                  bookId2, file3,displayedName3, bookId3);
        var tutor = tutorService.getTutorByToken(request);
        return tutorService.setBookmarks(tutor, fileListDto);
    }

    @Override
    public ResponseEntity<List<FileDto>> getBookmarkList(HttpServletRequest request) {
        var tutor = tutorService.getTutorByToken(request);
        return ResponseEntity.status(200).body(bookMarkService.getList(tutor));
    }
}
