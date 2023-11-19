package codepred.tutor;

import codepred.account.AppUserRole;
import codepred.account.User;
import codepred.bookmark.BookMark;
import codepred.bookmark.BookMarkRepository;
import codepred.common.mapper.ServiceMapper;
import codepred.common.mapper.TutorMapper;
import codepred.common.util.ResponseObject;
import codepred.config.EmailService;
import codepred.config.files.FileListDto;
import codepred.config.files.FileService;
import codepred.config.secutity.JwtTokenProvider;
import codepred.exception.AuthenticationException;
import codepred.exception.UserNotFoundException;
import codepred.meeting.EventService;
import codepred.service.ServiceRepository;
import codepred.service.dto.AddServiceRequest;
import codepred.tutor.dto.UpdateTutorRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class TutorService {

    private final TutorRepository tutorRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final ServiceMapper serviceMapper;
    private final ServiceRepository serviceRepository;
    private final TutorMapper tutorMapper;
    private final FileService fileService;
    private final BookMarkRepository bookMarkRepository;
    private final EventService eventService;

    @Value("${frontend_path}")
    private String frontend_path;

    public TutorService(TutorRepository tutorRepository, EmailService emailService, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager, ServiceMapper serviceMapper, ServiceRepository serviceRepository, TutorMapper tutorMapper, FileService fileService,
                        BookMarkRepository bookMarkRepository,
                        final EventService eventService) {
        this.tutorRepository = tutorRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.serviceMapper = serviceMapper;
        this.serviceRepository = serviceRepository;
        this.tutorMapper = tutorMapper;
        this.fileService = fileService;
        this.bookMarkRepository = bookMarkRepository;
        this.eventService = eventService;
    }

    public boolean checkIsTutorExist(String email) {
        log.debug("TutorService ==> checkIsTutorExist() - start: email = {}", email);
        var isTutorExist = tutorRepository.findByEmail(email).isPresent();
        log.debug("TutorService ==> checkIsTutorExist() - end: isTutorExist = {}", isTutorExist);
        return isTutorExist;
    }

    public static void main(String[] args) {

        System.out.println("I AM ALINA");
    }

    @Transactional
    public ResponseObject createTutor(User user) {
        log.debug("TutorService ==> createTutor() - start: tutor = {}", user);
        boolean existed = checkIsTutorExist(user.getEmail());
        if (existed) {
            user = getTutorByEmail(user.getEmail());
        }
        if (!existed || !user.getIsActivated()) {
            user.setIsActivated(Boolean.FALSE);
            String code = RandomStringUtils.randomAlphanumeric(30, 30);
            user.setCode(code);
            user.setAppUserRoles(AppUserRole.ROLE_TUTOR);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            var newTutorDetails = new TutorDetails();
            newTutorDetails.setIsServiceActive(false);
            user.setTutorDetails(newTutorDetails);
            user.setCodeTimeGenerated(Date.from(Instant.now()));
            emailService.sendEmail(
                    user.getEmail(),
                    "Tutorio",
                    "Weryfikacja rejestracji",
                    frontend_path + "/verification?code=" + code
            );
            tutorRepository.saveAndFlush(user);

            // add default tutor availability
            eventService.addDefaultAvailability(user);
            log.debug("TutorService ==> createTutor() - end: tutor = {}", user);
        } else {
            log.debug("TutorService ==> createTutor() - end: code = {}, message = {}, token = {}", HttpStatus.BAD_REQUEST, "USER_ALREADY_ADDED", null);
            return new ResponseObject(HttpStatus.BAD_REQUEST, "USER_ALREADY_ADDED", null);
        }
        if (user != null && !user.getIsActivated()) {
            log.debug("TutorService ==> createTutor() - end: code = {}, message = {}, token = {}", HttpStatus.ACCEPTED, "EMAIL_SENT", null);
            return new ResponseObject(HttpStatus.CREATED, "EMAIL_SENT", null);
        }
        log.debug("TutorService ==> createTutor() - end: code = {}, message = {}, token = {}", HttpStatus.ACCEPTED, "VERIFICATION_CODE_WAS_SENT_ONCE_AGAIN", null);
        return new ResponseObject(HttpStatus.ACCEPTED, "VERIFICATION_CODE_WAS_SENT_ONCE_AGAIN", null);
    }

    public ResponseObject confirmRegistration(String code) {
        log.debug("TutorService ==> confirmRegistration() - start: code = {}", code);
        var optionalTutor = tutorRepository.findByRegistrationCode(code);
        if (optionalTutor.isPresent()) {
            long hours = getHours(optionalTutor);
            User user = optionalTutor.get();
            if (hours < 24) {
                user.setIsActivated(Boolean.TRUE);
                user.setCode(null);
                user.setCodeTimeGenerated(null);
                tutorRepository.save(user);
                String token = jwtTokenProvider.createToken(
                        user.getEmail(),
                        new LinkedList<>(Collections.singletonList(user.getAppUserRoles())));
                var userToken = new TutorToken();
                userToken.setToken(token);
                userToken.setAppUserRole(user.getAppUserRoles());
                log.debug("TutorService ==> confirmRegistration() - end: code = {}, message = {}, token = {}", HttpStatus.OK, "Registration confirmed", token);
                return new ResponseObject(HttpStatus.OK, "REGISTRATION_CONFIRMED", token);
            } else {
                emailService.sendEmail(
                        user.getEmail(),
                        "Tutorio",
                        "Weryfikacja rejestracji",
                        frontend_path + "/verification?code=" + code
                );
                return new ResponseObject(HttpStatus.OK, "VERIFICATION_LINK_WAS_SEND_AGAIN", null);
            }
        } else {
            log.debug("TutorService ==> confirmRegistration() - end: code = {}, message = {}, token = {}", HttpStatus.BAD_REQUEST, "Invalid registration code", null);
            return new ResponseObject(HttpStatus.BAD_REQUEST, "INVALID_REGISTRATION_CODE", null);
        }
    }

    public ResponseObject signin(final User user) {
        log.debug("TutorService ==> signin() - start: tutor = {}", user);
        TutorToken userToken;
        Optional<User> tutorLoginData = tutorRepository.findByEmail(user.getEmail());
        if (tutorLoginData.isEmpty()) {
            return new ResponseObject(HttpStatus.BAD_REQUEST, "TUTOR_WAS_NOT_REGISTERED", null);
        } else if (!tutorLoginData.get().getIsActivated()) {
            try {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
                String code = RandomStringUtils.randomAlphanumeric(30, 30);
                tutorLoginData.get().setCode(code);
                tutorLoginData.get().setCodeTimeGenerated(Date.from(Instant.now()));
                tutorLoginData.get().setPassword(passwordEncoder.encode(user.getPassword()));
                CompletableFuture.runAsync(() -> {
                    emailService.sendEmail(
                            user.getEmail(),
                            "Tutorio",
                            "Weryfikacja rejestracji",
                            frontend_path
                                    + "/verification?code=" + code
                    );
                });
                tutorRepository.save(tutorLoginData.get());

                return new ResponseObject(HttpStatus.UNPROCESSABLE_ENTITY, "VERIFICATION_CODE_WAS_SENT_ONCE_AGAIN", null);
            } catch (Exception e) {
                return new ResponseObject(HttpStatus.UNAUTHORIZED, "WRONG_DATA", null);
            }
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
            String token = jwtTokenProvider.createToken(
                    tutorLoginData.get().getEmail(),
                    new LinkedList<>(Collections.singletonList(tutorLoginData.get().getAppUserRoles())));
            userToken = new TutorToken();
            userToken.setToken(token);
            AppUserRole appUserRole = tutorRepository.findByEmail(user.getEmail()).get().getAppUserRoles();
            if(appUserRole.equals(AppUserRole.ROLE_ADMIN)){
                appUserRole = AppUserRole.ROLE_CLIENT;
            }
            userToken.setAppUserRole(appUserRole);
            log.debug("TutorService ==> signin() - end: userToken = {}", userToken);
            return new ResponseObject(HttpStatus.ACCEPTED, "CORRECT_LOGIN_DATA", token, appUserRole);
        } catch (Exception e) {
            return new ResponseObject(HttpStatus.UNAUTHORIZED, "WRONG_DATA", null);
        }

    }

    public ResponseObject sendEmailToPasswordReset(String email) {
        log.debug("TutorService ==> sendEmailToPasswordReset() - start: email = {}", email);
        var tutor = tutorRepository.findByEmail(email);
        if (tutor.isEmpty()) {
            return new ResponseObject(HttpStatus.BAD_REQUEST, "WRONG_DATA", null);
        } else if (!tutor.get().getIsActivated()) {
            return new ResponseObject(HttpStatus.UNAUTHORIZED, "ACCOUNT_IS_NOT_ACTIVATED", null);
        } else {
            String code = RandomStringUtils.randomAlphanumeric(30, 30);
            tutor.get().setCode(code);
            tutor.get().setCodeTimeGenerated(Date.from(Instant.now()));
            tutorRepository.save(tutor.get());
            // CompletableFuture.runAsync(() -> {
            emailService.sendEmailToResetPassword(
                    email,
                    "Tutorio",
                    "Resetowanie hasła",
                    frontend_path +
                            "/forgotpassword?code=" + code
            );
            // });
            log.debug("TutorService ==> sendEmailToPasswordReset() - end:  HttpStatus = {}, message = {}, token = {}", HttpStatus.ACCEPTED, "EMAIL_SENT", null);
            return new ResponseObject(HttpStatus.ACCEPTED, "EMAIL_SENT", null);
        }
    }

    public ResponseObject checkCodeForPasswordResetting(String code) {
        log.debug("TutorService ==> passwordResetting() - start:  code = {}", code);
        var tutor = tutorRepository.findByRegistrationCode(code);
        if (tutor.isEmpty()) {
            return new ResponseObject(HttpStatus.UNAUTHORIZED, "INVALID_CODE", null);
        } else if (!tutor.get().getIsActivated()) {
            return new ResponseObject(HttpStatus.UNPROCESSABLE_ENTITY, "ACCOUNT_IS_NOT_ACTIVATED", null);
        }
        long hours = getHours(tutor);
        if (hours < 4) {
            String newCode = RandomStringUtils.randomAlphanumeric(30, 30);
            tutor.get().setCode(newCode);
            tutor.get().setCodeTimeGenerated(Date.from(Instant.now()));
            tutorRepository.save(tutor.get());
            return new ResponseObject(HttpStatus.ACCEPTED, newCode, null);
        } else {
            return new ResponseObject(HttpStatus.BAD_REQUEST, "CODE_EXPIRED", null);
        }

    }

    public ResponseObject passwordResetting(String code, String password) {
        log.debug("TutorService ==> passwordResetting() - start:  code = {}, password = {}", code, password);
        var tutorOptional = tutorRepository.findByRegistrationCode(code);
        if (tutorOptional.isEmpty()) {
            return new ResponseObject(HttpStatus.BAD_REQUEST, "CODE_NOT_EXIST", null);
        }
        long hours = getHours(tutorOptional);
        if (hours > 4) {
            log.debug("TutorService ==> sendEmailToPasswordReset() - end:  HttpStatus = {}, message = {}, token = {}", HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", null);
            return new ResponseObject(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", null);
        } else {
            var tutor = tutorOptional.get();
            tutor.setCode(null);
            tutor.setCodeTimeGenerated(null);
            tutor.setPassword(passwordEncoder.encode(password));
            tutorRepository.save(tutor);
            log.debug("TutorService ==> passwordResetting() - end:  HttpStatus = {}, message = {}, token = {}", HttpStatus.ACCEPTED, "PASSWORD_SUCCESSFULLY_CHANGED", null);
            return new ResponseObject(HttpStatus.ACCEPTED, "PASSWORD_SUCCESSFULLY_CHANGED", null, tutorOptional.get().getAppUserRoles());
        }
    }

    public ResponseObject checkIsItFirstLogin(User userData) {
        log.debug("TutorService ==> checkIsItFirstLogin() - start:  tutor = {}", userData);
        if (!userData.getIsActivated()) {
            log.debug("TutorService ==> checkIsItFirstLogin() - end:  UserNotFoundException = {}", userData.getId());
            throw new UserNotFoundException("ACCOUNT_IS_NOT_ACTIVATED");
        } else if (userData.getTutorDetails() != null && userData.getTutorDetails().getName() == null && !userData.getTutorDetails().getServiceActive()) {
            log.debug("TutorService ==> checkIsItFirstLogin() - end:  HttpStatus = {}, message = {}, token = {}", HttpStatus.ACCEPTED, "USER_DATA_AND_SERVICE_REQUIRED", null);
            return new ResponseObject(
                    HttpStatus.ACCEPTED,
                    "USER_DATA_AND_SERVICE_REQUIRED",
                    null
            );
        } else if (userData.getTutorDetails() != null && userData.getTutorDetails().getName() != null && !userData.getTutorDetails().getServiceActive()) {
            log.debug("TutorService ==> checkIsItFirstLogin() - end:  HttpStatus = {}, message = {}, token = {}", HttpStatus.ACCEPTED, "FIRST_SERVICE_REQUIRED", null);
            return new ResponseObject(
                    HttpStatus.ACCEPTED,
                    "FIRST_SERVICE_REQUIRED",
                    null
            );
        } else {
            log.debug("TutorService ==> checkIsItFirstLogin() - end:  HttpStatus = {}, message = {}, token = {}", HttpStatus.ACCEPTED, "USER_HAS_ALL_NECESSARY_DATA", null);
            return new ResponseObject(
                    HttpStatus.ACCEPTED,
                    "USER_HAS_ALL_NECESSARY_DATA",
                    null);
        }
    }

    public ResponseObject updatePersonalTutorData(User userFromDB, TutorDetails userPersonalData) {
        log.debug("TutorService ==> addTutorPersonalData() - start:  tutor = {}", userPersonalData);
        userFromDB.getTutorDetails().setName(userPersonalData.getName());
        userFromDB.getTutorDetails().setActivityType(userPersonalData.getActivityType());
        userFromDB.getTutorDetails().setSurname(userPersonalData.getSurname());
        userFromDB.getTutorDetails().setPhoneNumber(userPersonalData.getPhoneNumber());
        userFromDB.getTutorDetails().setNip(userPersonalData.getNip());
        userFromDB.getTutorDetails().setRegon(userPersonalData.getRegon());
        userFromDB.getTutorDetails().setCompanyName(userPersonalData.getCompanyName());
        userFromDB.getTutorDetails().setStreet(userPersonalData.getStreet());
        userFromDB.getTutorDetails().setPostCode(userPersonalData.getPostCode());
        userFromDB.getTutorDetails().setPlace(userPersonalData.getPlace());
        userFromDB.getTutorDetails().setBankName(userPersonalData.getBankName());
        userFromDB.getTutorDetails().setBankAccountNumber(userPersonalData.getBankAccountNumber());
        tutorRepository.save(userFromDB);
        log.debug("TutorService ==> addTutorPersonalData() - end:  HttpStatus = {}, message = {}, token = {}, tutor = {}",
                HttpStatus.OK, "DATA_SUCCESSFULLY_CREATED", null, userFromDB);
        return new ResponseObject(
                HttpStatus.OK,
                "DATA_SUCCESSFULLY_CREATED",
                null
        );
    }

    @Transactional
    public ResponseObject saveServiceAndAddToTutor(User user, AddServiceRequest requestServiceDTO) {
        log.debug("TutorService ==> addServiceToTutor() - start:  tutor = {}, service = {}", user, requestServiceDTO);
        if (requestServiceDTO.getIsServiceActive() != null && requestServiceDTO.getIsServiceActive()) {
            user.getTutorDetails().setIsServiceActive(true);
            tutorRepository.save(user);
        } else {
            var service = serviceMapper.AddServiceRequestToService(requestServiceDTO);
            service = serviceRepository.saveAndFlush(service);
            user.getServices().add(service);
            user.getTutorDetails().setIsServiceActive(true);
            tutorRepository.save(user);
            log.debug("TutorService ==> addServiceToTutor() - end:  tutor = {}", user);
        }
        return new ResponseObject(HttpStatus.CREATED, "SERVICE_SUCCESSFULLY_ADD", null);
    }

    public User getTutorByToken(HttpServletRequest request) {
        log.debug("TutorService ==> getTutorByToken() - start: HttpServletRequest = {}", request);
        var tutor = tutorRepository.findByEmail(jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(request)));
        if (tutor.isEmpty()) {
            log.debug("TutorService ==> getTutorByToken() - end: AuthenticationException = {}", "Access denied");
            throw new AuthenticationException("ACCESS_DENIED");
        } else {
            log.debug("TutorService ==> getTutorByToken() - end: tutor = {}", tutor.get());
            return tutor.get();
        }

    }

    public User getTutorByEmail(String email) {
        log.debug("TutorService ==> getTutorByEmail() - start: email = {}", email);
        Optional<User> tutor = tutorRepository.findByEmail(email);
        if (tutor.isEmpty()) {
            return null;
        }
        log.debug("TutorService ==> getTutorByEmail() - end: tutor = {}", tutor.get());
        return tutor.get();
    }

    public User getTutorDataByToken(User user) {
        log.debug("TutorService ==> getTutorDataByToken() - start: user = {}", user);

        List<Object[]> results = tutorRepository.findTutorById(user.getId());
        User mappedUser = null;

        if (!results.isEmpty()) {
            Object[] row = results.get(0);
            mappedUser = User.builder()
                    .id((Integer) row[0])
                    .email((String) row[1])
                    .tutorDetails(TutorDetails.builder()
                            .id((Integer) row[2])
                            .activityType((String) row[3])
                            .name((String) row[4])
                            .surname((String) row[5])
                            .phoneNumber((String) row[6])
                            .nip((String) row[7])
                            .regon((String) row[8])
                            .companyName((String) row[9])
                            .street((String) row[10])
                            .postCode((String) row[11])
                            .place((String) row[12])
                            .bankName((String) row[13])
                            .bankAccountNumber((String) row[14])
                            .isServiceActive((Boolean) row[15])
                            .photo((byte[]) row[16])
                            .build())
                    .build();
        }

        log.debug("TutorService ==> getTutorDataByToken() - end: user = {}", mappedUser);
        return mappedUser;
    }

    public User getTutorDataById(Integer tutorId) {
        List<Object[]> results = tutorRepository.findTutorById(tutorId);
        User mappedUser = null;

        if (!results.isEmpty()) {
            Object[] row = results.get(0);
            mappedUser = User.builder()
                    .id((Integer) row[0])
                    .email((String) row[1])
                    .tutorDetails(TutorDetails.builder()
                            .id((Integer) row[2])
                            .activityType((String) row[3])
                            .name((String) row[4])
                            .surname((String) row[5])
                            .phoneNumber((String) row[6])
                            .nip((String) row[7])
                            .regon((String) row[8])
                            .companyName((String) row[9])
                            .street((String) row[10])
                            .postCode((String) row[11])
                            .place((String) row[12])
                            .bankName((String) row[13])
                            .bankAccountNumber((String) row[14])
                            .isServiceActive((Boolean) row[15])
                            .photo((byte[]) row[16])
                            .build())
                    .build();
        }

        return mappedUser;
    }

    public User save(User user) {
        return tutorRepository.save(user);
    }

    public Optional<User> findByRegistrationCode(String code) {
        return tutorRepository.findByRegistrationCode(code);
    }

    public List<User> findAllClientTutors(User user) {
        return tutorRepository.findAllClientTutors(user);
    }

    @Transactional
    public ResponseObject resetPassword(User user, String password, String newPassword) {
        log.debug("TutorService ==> resetPassword() - start: email = {}", password);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), password));
            user.setPassword(passwordEncoder.encode(newPassword));
            tutorRepository.save(user);
            var response = new ResponseObject(HttpStatus.ACCEPTED, "PASSWORD_SUCCESSFULLY_UPDATED", refresh(user.getEmail()));
            log.debug("TutorService ==> resetPassword() - end: response = {}", response);
            return response;
        } catch (Exception ignored) {
            var response = new ResponseObject(HttpStatus.BAD_REQUEST, "WRONG_OLD_PASSWORD", refresh(user.getEmail()));
            log.debug("TutorService ==> resetPassword() - end: response = {}", response);
            return response;
        }

    }

    @Transactional
    public ResponseObject removeTutor(User user) {
        log.debug("TutorService ==> removeTutor() - start: email = {}", user);
        var services = tutorRepository.findIdOfServices(user.getId());
        for (Integer service : services) {
            removeService(user, service);
        }
        user.setClients(null);
        user.setServices(null);
        tutorRepository.save(user);
        tutorRepository.delete(user);
        var response = new ResponseObject(HttpStatus.ACCEPTED, "USER_SUCCESSFULLY_REMOVED", null);
        log.debug("TutorService ==> removeTutor() - end: response = {}", response);
        return response;
    }

    @Transactional
    public ResponseObject updatePersonalTutorData(User user, UpdateTutorRequest tutorData) {
        log.debug("TutorService ==> updatePersonalTutorData() - start: user = {}, tutorData = {}", user, tutorData);
        if (tutorRepository.countEmails(tutorData.getEmail()) <= 1) {
            var photo = user.getTutorDetails().getPhoto();
            tutorMapper.updateFromDto(tutorData, user);
            if (tutorData.getChangePhoto().equals(Boolean.FALSE)) {
                user.getTutorDetails().setPhoto(photo);
            }
            tutorRepository.save(user);
            var response = new ResponseObject(HttpStatus.OK, "DATA_UPDATED", refresh(user.getEmail()));
            log.debug("TutorService ==> updatePersonalTutorData() - end: response = {}", response);
            return response;
        } else {
            var response = new ResponseObject(HttpStatus.BAD_REQUEST, "THIS_EMAIL_CANNOT_BE_USED", refresh(user.getEmail()));
            log.debug("TutorService ==> updatePersonalTutorData() - end: response = {}", response);
            return response;
        }
    }


    public String refresh(String email) {
        log.debug("TutorService ==> refresh() - start: email = {}", email);
        var tutor = tutorRepository.findByEmail(email);
        if (tutor.isEmpty()) {
            log.debug("TutorService ==> refresh() - end: UserNotFoundException = {}", "USER_WITH_THIS_EMAIL_NOT_FOUND");
            throw new UserNotFoundException("USER_WITH_THIS_EMAIL_NOT_FOUND");
        } else {
            var token = jwtTokenProvider.createToken(email, Collections.singletonList(tutor.get().getAppUserRoles()));
            log.debug("TutorService ==> refresh() - start: token = {}", token);
            return token;
        }
    }


    @Transactional
    public void removeService(User user, Integer serviceId) {
        log.debug("ClientService ==> removeService() - start: Tutor = {}, service = {}", user, serviceId);
        var serviceFromDb = serviceRepository.findServiceByIdAndUser(serviceId, user).get();
        serviceFromDb.setUsers(null);
        serviceFromDb.setEvents(null);
        serviceFromDb.setGroups(null);
        var forRemove = serviceRepository.saveAndFlush(serviceFromDb);
        serviceRepository.delete(forRemove);
        log.debug("ClientService ==> removeService() - end: OK");

    }

    public ResponseObject setBookmarks(User user, FileListDto fileListDtoList) throws IOException {

        if (fileListDtoList.getFile1() != null) {
            if (fileListDtoList.getBookmarkId1() == null) {
                fileService.addNewBookmark(user, fileListDtoList.getFile1(), fileListDtoList.getDisplayedName1(), BookMark.Type.FIRST);
            } else {
                BookMark bookMark = bookMarkRepository.getById(fileListDtoList.getBookmarkId1());
                fileService.updateBookmark(fileListDtoList.getFile1(), fileListDtoList.getDisplayedName1(), bookMark);
            }
        } else {
            if (fileListDtoList.getBookmarkId1() == null) {
                BookMark bookMark = bookMarkRepository.getByTutorAndType(user.getId(), BookMark.Type.FIRST.name());
                if(bookMark!=null) {
                    fileService.delete(bookMark.getFileName());
                    bookMarkRepository.delete(bookMark);
                }
            } else {
                BookMark bookMark = bookMarkRepository.getById(fileListDtoList.getBookmarkId1());
                if (fileListDtoList.getDisplayedName1() != null) {
                    bookMark.setDisplayedName(fileListDtoList.getDisplayedName1());
                    bookMarkRepository.save(bookMark);
                }
            }
        }

        if (fileListDtoList.getFile2() != null) {
            if (fileListDtoList.getBookmarkId2() == null) {
                fileService.addNewBookmark(user, fileListDtoList.getFile2(), fileListDtoList.getDisplayedName2(), BookMark.Type.SECOND);
            } else {
                BookMark bookMark = bookMarkRepository.getById(fileListDtoList.getBookmarkId2());
                fileService.updateBookmark(fileListDtoList.getFile2(), fileListDtoList.getDisplayedName2(), bookMark);
            }
        } else {
            if (fileListDtoList.getBookmarkId2() == null) {
                BookMark bookMark = bookMarkRepository.getByTutorAndType(user.getId(), BookMark.Type.SECOND.name());
                if(bookMark!=null) {
                    fileService.delete(bookMark.getFileName());
                    bookMarkRepository.delete(bookMark);
                }
            } else {
                BookMark bookMark = bookMarkRepository.getById(fileListDtoList.getBookmarkId2());
                if (fileListDtoList.getDisplayedName2() != null) {
                    bookMark.setDisplayedName(fileListDtoList.getDisplayedName2());
                    bookMarkRepository.save(bookMark);
                }
            }
        }

        if (fileListDtoList.getFile3() != null) {
            if (fileListDtoList.getBookmarkId3() == null) {
                fileService.addNewBookmark(user, fileListDtoList.getFile3(), fileListDtoList.getDisplayedName3(), BookMark.Type.THIRD);
            } else {
                BookMark bookMark = bookMarkRepository.getById(fileListDtoList.getBookmarkId3());
                fileService.updateBookmark(fileListDtoList.getFile3(), fileListDtoList.getDisplayedName3(), bookMark);
            }
        } else {
            if (fileListDtoList.getBookmarkId3() == null) {
                BookMark bookMark = bookMarkRepository.getByTutorAndType(user.getId(), BookMark.Type.THIRD.name());
                if(bookMark!=null) {
                    fileService.delete(bookMark.getFileName());
                    bookMarkRepository.delete(bookMark);
                }
            } else {
                BookMark bookMark = bookMarkRepository.getById(fileListDtoList.getBookmarkId3());
                if (fileListDtoList.getDisplayedName3() != null) {
                    bookMark.setDisplayedName(fileListDtoList.getDisplayedName3());
                    bookMarkRepository.save(bookMark);
                }
            }
        }

        return new ResponseObject(HttpStatus.OK, "DATA_UPDATED", refresh(user.getEmail()));
    }

    private long getHours(Optional<User> optionalTutor) {
        Instant codeTimeGenerated = optionalTutor.get().getCodeTimeGenerated().toInstant();
        Instant now = Instant.now();

        Duration duration = Duration.between(codeTimeGenerated, now);
        long hours = duration.toHours();
        return hours;
    }
}
