package codepred.company;

import codepred.account.AppUserRole;
import codepred.account.User;
import codepred.attendance.Attendance;
import codepred.attendance.AttendanceRepository;
import codepred.client.ClientDetails;
import codepred.client.ClientRepository;
import codepred.client.ClientService;
import codepred.common.mapper.CompanyMapper;
import codepred.common.util.ResponseObject;
import codepred.company.dto.CompanyDto;
import codepred.company.dto.CompanyFullDataDto;
import codepred.company.dto.CreateCompanyRequest;
import codepred.company.dto.UpdateCompanyRequest;
import codepred.config.EmailService;
import codepred.exception.CustomException;
import codepred.group.Group;
import codepred.group.GroupRepository;
import codepred.group.dto.GroupReadDto;
import codepred.meeting.Event;
import codepred.meeting.EventRepository;
import codepred.meeting.MeetingStatus;
import codepred.meeting.dto.CompanyMeetingDto;
import codepred.meeting.dto.MeetingReadDto;
import codepred.meeting.dto.MeetingsDto;
import codepred.tutor.TutorRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CompanyService {

    public static final int PAGE_SIZE = 10;
    private final TutorRepository tutorRepository;
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final ClientService clientService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final GroupRepository groupRepository;
    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;
    private final ClientRepository clientRepository;

    @Value("${frontend_path}")
    private String frontend_path;

    public CompanyService(TutorRepository tutorRepository,
                          CompanyRepository companyRepository,
                          CompanyMapper companyMapper,
                          ClientService clientService, PasswordEncoder passwordEncoder,
                          EmailService emailService,
                          GroupRepository groupRepository,
                          EventRepository eventRepository,
                          AttendanceRepository attendanceRepository,
                          ClientRepository clientRepository) {
        this.tutorRepository = tutorRepository;
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
        this.clientService = clientService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.groupRepository = groupRepository;
        this.eventRepository = eventRepository;
        this.attendanceRepository = attendanceRepository;
        this.clientRepository = clientRepository;
    }

    public ResponseObject createCompany(User tutorFromDb, CreateCompanyRequest createCompanyRequest) {
        log.debug("CompanyService ==> createCompany() - start: user = {}, company = {}", tutorFromDb, createCompanyRequest);

        Company company = companyMapper.fromRequestDTO(createCompanyRequest);
        User existingClient = clientRepository.findByEmail(createCompanyRequest.getEmail()).orElse(null);
        Company existingCompany = companyRepository.findCompanyByName(createCompanyRequest.getName()).orElse(null);

        if(existingClient != null && existingClient.getCompanies() != null) {
            return new ResponseObject(HttpStatus.BAD_REQUEST, "ADMINISTRATOR_HAS_ALREADY_REGISTERED_COMPANY", null);
        }

        //if(existingClient != null) return new ResponseObject(HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_TAKEN", null);

        if (existingCompany != null) {
            for (Company tutorCompany : tutorFromDb.getCompanies()) {
                if (tutorCompany.getName().equals(company.getName())) {
                    return new ResponseObject(HttpStatus.BAD_REQUEST, "CLIENT_ALREADY_EXIST", null);
                }
            }

        }
        if (existingClient != null) {

            companyRepository.saveAndFlush(company);
            existingClient.getCompanies().add(company);
            tutorFromDb.getCompanies().add(company);
            tutorFromDb.getClients().add(existingClient);
            tutorRepository.saveAndFlush(tutorFromDb);
            clientRepository.save(existingClient);

            var response = new ResponseObject(HttpStatus.OK, company.getId().toString(), null);
            log.debug("CompanyService ==> createCompany() - end: response = {}", response);
            return response;

        } else {

            String code = RandomStringUtils.randomAlphanumeric(30, 30);

            ClientDetails newClientDetails = setClientDetails(createCompanyRequest, tutorFromDb.getId());

            User newClient = User.builder()
                .isActivated(false)
                .code(code)
                .appUserRoles(AppUserRole.ROLE_ADMIN)
                .password(passwordEncoder.encode(code))
                .email(createCompanyRequest.getEmail())
                .companies(Set.of(company))
                .createdAt(LocalDateTime.now(ZoneId.of("Europe/Warsaw")))
                .clientDetails(Set.of(newClientDetails))
                .codeTimeGenerated(Date.from(Instant.now()))
                .build();

            newClientDetails.setUser(newClient);

            tutorFromDb.getCompanies().add(company);
            tutorFromDb.getClients().add(newClient);

            clientRepository.saveAndFlush(newClient);
            companyRepository.saveAndFlush(company);
            tutorRepository.saveAndFlush(tutorFromDb);

            var clientDetailsForGivenTutor = clientService.findClientDetailsForGivenTutor(newClient, tutorFromDb);
            emailService.sendEmailForNewClient(newClient.getEmail(),
                    "Tutorio",
                    clientDetailsForGivenTutor.getName(),
                    "Zaproszenie do systemu",
                    tutorFromDb.getTutorDetails().getName() + " " + tutorFromDb.getTutorDetails().getSurname(),
                    frontend_path + "/customer/register?code=" + code + "&email=" + newClient.getEmail());
            var response = new ResponseObject(HttpStatus.CREATED, company.getId().toString(), null);
            log.debug("CompanyService ==> createCompany() - end: response = {}", response);
            return response;
        }
    }

    @NotNull
    private static ClientDetails setClientDetails(CreateCompanyRequest CreateCompanyRequest, int tutorId) {
        var clientDetails = new ClientDetails();
        clientDetails.setName(CreateCompanyRequest.getAdminName());
        clientDetails.setLastname(CreateCompanyRequest.getAdminLastname());
        clientDetails.setPostcode(CreateCompanyRequest.getPostcode());
        clientDetails.setPhoneNumber(CreateCompanyRequest.getPhoneNumber());
        clientDetails.setProvince(CreateCompanyRequest.getPlace());
        clientDetails.setNip(CreateCompanyRequest.getNip());
        clientDetails.setRegon(CreateCompanyRequest.getRegon());
        clientDetails.setPhoneNumber(CreateCompanyRequest.getPhoneNumber());
        clientDetails.setIsAvailableToChangeReservation(CreateCompanyRequest.getIsAvailableToChangeReservation());
        clientDetails.setIsAvailableToCancelReservation(CreateCompanyRequest.getIsAvailableToCancelReservation());
        clientDetails.setMinimumChangeTime(CreateCompanyRequest.getMinimumChangeTime());
        clientDetails.setAdditionalInformation(CreateCompanyRequest.getAdditionalInfo());
        clientDetails.setClientPhoto(clientDetails.getClientPhoto());
        clientDetails.setTutorId(tutorId);
        return clientDetails;
    }

    public List<CompanyDto> getAllCompaniesByPage(User user, int pageNumber) {
        log.debug("CompanyService ==> getAllCompanies() - start: user = {}, pageNumber = {}", user, pageNumber);
        List<Company> companies = companyRepository.findCompaniesByUsers(user,
                                                                         PageRequest.of(adjustPageNumber(pageNumber), PAGE_SIZE));
        List<CompanyDto> response = new ArrayList<>();
        for (Company company : companies) {
            List<Group> groups = groupRepository.findByClientIdAndCompanyId(user.getId(), company.getId());
            response.add(new CompanyDto(company.getId(),
                                                     company.getName(),
                                                     groups.size(),
                                                     groups.stream().mapToInt(g -> g.getUser().size() - 1).sum(),
                                                     company.getPhoto()));
        }
        log.debug("CompanyService ==> getAllCompanies() - end: companies = {}", response);
        return response;
    }

    public List<CompanyDto> getAllUserCompanies(User user) {
        log.debug("CompanyService ==> getAllCompanies() - start: user = {}", user);
        List<Object[]> rawPage = companyRepository.getAllCompanies(user.getId());

        List<CompanyDto> result = rawPage.stream()
            .map(data -> CompanyDto.builder()
                .id((Integer) data[0])
                .name((String) data[1])
                .groupnumber(((BigInteger) data[3]).intValue())
                .studentsnumber(((BigInteger) data[2]).intValue())
                .photo((byte[]) data[4])
                .build())
            .collect(Collectors.toList());

        log.debug("CompanyService ==> getAllCompanies() - end: companies = {}", result);
        return result;
    }

    public List<CompanyDto> getAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        List<CompanyDto> response = new ArrayList<>();
        for (Company company : companies) {
            response.add(new CompanyDto(company.getId(), company.getName(), null, null,
                                                     company.getPhoto()));
        }
        log.debug("CompanyService ==> getAllCompanies() - end: companies = {}", response);
        return response;
    }


    public CompanyFullDataDto getCertainCompany(User user, Integer companyId) {
        var companyInfoArray = companyRepository.getCompanyByCompanyIdAndTutorId(companyId, user.getId());
        var companyInfo = new CompanyFullDataDto();
        if (companyInfoArray == null || companyInfoArray.length == 0) throw new CustomException("CLIENT_NOT_FOUND", HttpStatus.BAD_REQUEST);
        if (companyInfoArray[0] instanceof Object[]) companyInfoArray = (Object[]) companyInfoArray[0];
        setCompanyInfo(user, companyInfo, companyInfoArray, companyId);
        return companyInfo;
    }

    private void setCompanyInfo(User tutor,
                                CompanyFullDataDto companyInfo,
                                Object[] companyInfoArray,
                                Integer companyId) {
        Optional<Company> company = companyRepository.findById(companyId);
        if (company.isEmpty()) throw new CustomException("COMPANY_NOT_FOUND", HttpStatus.BAD_REQUEST);
        Set<User> companyOwners = company.get().getUsers();
        companyOwners.remove(tutor);
        User companyOwner = companyOwners.iterator().next();
        companyInfo.setId((Integer) companyInfoArray[0]);
        companyInfo.setName((String) companyInfoArray[1]);
        companyInfo.setAddress((String) companyInfoArray[2]);
        companyInfo.setPostcode((String) companyInfoArray[3]);
        companyInfo.setPlace((String) companyInfoArray[4]);
        companyInfo.setNip((String) companyInfoArray[5]);
        companyInfo.setRegon((String) companyInfoArray[6]);
        companyInfo.setIsAvailableToCancelReservation((Boolean) companyInfoArray[7]);
        companyInfo.setIsAvailableToChangeReservation((Boolean) companyInfoArray[8]);
        companyInfo.setMinimumChangeTime((Integer) companyInfoArray[9]);
        companyInfo.setAdditionalInfo((String) companyInfoArray[10]);
        companyInfo.setPhoto((byte[]) companyInfoArray[11]);
        companyInfo.setAdminId(companyOwner.getId());
        companyInfo.setEmail(companyOwner.getEmail());
        if (companyOwner.getClientDetails() != null) {
            var clientDetailsForGivenTutor = clientService.findClientDetailsForGivenTutor(companyOwner, tutor);
            companyInfo.setAdminName(clientDetailsForGivenTutor.getName());
            companyInfo.setAdminSurname(clientDetailsForGivenTutor.getLastname());
            companyInfo.setPhoneNumber(clientDetailsForGivenTutor.getPhoneNumber());
        } else {
            companyInfo.setAdminName(companyOwner.getTutorDetails().getName());
            companyInfo.setAdminSurname(companyOwner.getTutorDetails().getSurname());
            companyInfo.setPhoneNumber(companyOwner.getTutorDetails().getPhoneNumber());
        }

    }

    public ResponseObject updateCompany(User tutor, UpdateCompanyRequest updateCompanyRequest) {
        log.debug("CompanyService ==> updateCompany() - start: user = {}, companyId = {}", tutor, updateCompanyRequest);
        var companyFromDb = companyRepository.findCompanyByIdAndUsers(updateCompanyRequest.getId(), tutor.getId())
            .orElseThrow(() -> new CustomException("COMPANY_NOT_FOUND", HttpStatus.BAD_REQUEST));
        if (companyFromDb != null) {

            User adminUser = companyFromDb.getUsers()
                .stream()
                .filter(user -> user.getAppUserRoles().equals(AppUserRole.ROLE_ADMIN))
                .findFirst()
                .orElse(null);

            var clientDetailsForGivenTutor = clientService.findClientDetailsForGivenTutor(adminUser, tutor);
            clientDetailsForGivenTutor.setName(updateCompanyRequest.getAdminName());
            clientDetailsForGivenTutor.setLastname(updateCompanyRequest.getAdminLastname());
            clientDetailsForGivenTutor.setPhoneNumber(updateCompanyRequest.getPhoneNumber());

            adminUser.setClientDetails(Set.of(clientDetailsForGivenTutor));

            clientRepository.save(adminUser);

            var updatedCompany = companyMapper.fromRequestCompanyToUpdateDTO(updateCompanyRequest);
            if (updateCompanyRequest.getChangePhoto().equals(Boolean.FALSE)) {
                updatedCompany.setPhoto(companyFromDb.getPhoto());
            }
            updatedCompany.setId(companyFromDb.getId());
            companyRepository.save(updatedCompany);

            var response = new ResponseObject(HttpStatus.ACCEPTED, "COMPANY_SUCCESSFULLY_UPDATED", null);
            log.debug("CompanyService ==> updateCompany() - end: response = {}", response);
            return response;
        } else {
            var response = new ResponseObject(HttpStatus.BAD_REQUEST, "COMPANY_NOT_FOUND", null);
            log.debug("CompanyService ==> updateCompany() - end: response = {}", response);
            return response;
        }
    }

    public List<CompanyDto> findCompanyByName(User user, String companyName) {
        log.debug("CompanyService ==> findCompanyByName() - start: user = {}, companyName = {}", user, companyName);
        var companiesRaw = companyRepository.findCompanyByNameAndTutorId(companyName, user.getId());
        List<CompanyDto> result = new ArrayList<>();

        for (Object[] data : companiesRaw) {
            CompanyDto company = CompanyDto.builder()
                .id((Integer) data[0])
                .name((String) data[1])
                .groupnumber(((BigInteger) data[2]).intValue())
                .studentsnumber(((BigInteger) data[3]).intValue())
                .photo((byte[]) data[4])
                .build();
            result.add(company);
        }
        log.debug("CompanyService ==> findCompanyByName() - end: companies = {}", result);
        return result;
    }

    public Integer getTotalPages(User user) {
        log.debug("CompanyService ==> getTotalPages() - start: user = {}", user);
        int totalCount = companyRepository.countCompaniesByTutorId(user.getId());
        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);
        log.debug("CompanyService ==> getTotalPages() - end: totalPages = {}", totalPages);
        return totalPages;
    }

    public List<GroupReadDto> getConnectedGroup(User user, Integer companyId) {
        var groups = groupRepository.getConnectedGroups(companyId);
        var response = new ArrayList<GroupReadDto>();
        for (Group group : groups) {
            response.add(new GroupReadDto(group.getId(), group.getGroupName(), group.getGroupPhoto()));
        }
        return response;
    }

    @Transactional
    public ResponseObject removeCompany(User user, Integer companyId) {
        var company = companyRepository.getCompanyByIdAndTutor(companyId, user.getId())
            .orElseThrow(() -> new CustomException("COMPANY_NOT_FOUND", HttpStatus.BAD_REQUEST));
        companyRepository.removeUserCompanyConnection(user.getId(), company.getId());
        companyRepository.removeGroupCompanyConnection(user.getId(), company.getId());
        return new ResponseObject(HttpStatus.OK, "COMPANY_SUCCESSFULLY_REMOVED", null);
    }

    public List<MeetingReadDto> getMeetings(User user,
                                             Integer companyId,
                                             LocalDateTime startTime,
                                             LocalDateTime endTime,
                                             int pageNumber) {
        int adjustedPageNumber = pageNumber - 1;
        Pageable pageable = PageRequest.of(adjustedPageNumber, 10);
        var company = companyRepository.getCompanyByIdAndTutor(companyId, user.getId())
            .orElseThrow(() -> new CustomException("COMPANY_NOT_FOUND", HttpStatus.BAD_REQUEST));
        final var groupIdList = groupRepository.getCompanyGroupsIds(company.getId());
        var events = eventRepository.findAllEventsForCompanyInTimeRange(groupIdList, startTime, endTime, pageable);
        var response = new ArrayList<MeetingReadDto>();
        for (Event event : events) {
            if (event.getWasCanceled() == Boolean.FALSE) {
                var status = MeetingStatus.planned;
                if (LocalDateTime.now(ZoneId.of("Europe/Warsaw")).isAfter(event.getStartTime())) {
                    status = MeetingStatus.finished;
                    event.setMeetingStatus(status);
                }
                response.add(new MeetingReadDto(
                    event.getId(), event.getStartTime(), event.getGroup().getGroupName(), status, event.getService().getPrice()
                ));
            } else {
                List<Attendance> attendance = attendanceRepository.findAttendancesByEvent(event);
                response.add(new MeetingReadDto(
                    event.getId(),
                    event.getStartTime(),
                    event.getGroup().getGroupName(),
                    attendance.stream().findFirst().get().getMeetingStatus(),
                    event.getService().getPrice()
                ));
            }
        }
        return response;
    }

    public MeetingsDto getTotalSumAndPageSize(User tutor, CompanyMeetingDto companyMeetingDto) {
        var company = companyRepository.getCompanyByIdAndTutor(companyMeetingDto.getCompanyId(), tutor.getId())
            .orElseThrow(() -> new CustomException("COMPANY_NOT_FOUND", HttpStatus.BAD_REQUEST));
        final var groupIdList = groupRepository.getCompanyGroupsIds(company.getId());
        var events = eventRepository.findAllEventsForCompanyInTimeRange(groupIdList, companyMeetingDto.getStartTime(), companyMeetingDto.getEndTime());

        Double totalSum = 0d;
        Float valuePaid = 0f;
        int numberOfPages = (int) Math.ceil((double) events.size() / 10);

        List<Integer> meetingsIds = new ArrayList<>();

        for (Event event : events) {
            totalSum += event.getService().getPrice();
            List<Attendance> attendances = attendanceRepository.findAttendancesByEvent(event);
            for (Attendance attendance : attendances) {
                if (attendance.getValuePaid() != null) {
                    valuePaid += attendance.getValuePaid();
                }
                meetingsIds.add(attendance.getId());
            }
        }

        return MeetingsDto.builder()
            .numberOfPages(String.valueOf(numberOfPages))
            .summedValue(totalSum.toString())
            .paidValue(valuePaid.toString())
            .meetingIds(meetingsIds)
            .build();
    }

    public ResponseObject changeMeetingStatus(User user, Integer eventId) {
        var event = eventRepository.findById(eventId)
            .orElseThrow(() -> new CustomException("EVENT_NOT_FOUND", HttpStatus.BAD_REQUEST));
        final var attendancees = attendanceRepository.findAttendancesByEvent(event);
        for (Attendance attendance : attendancees) {
            if (attendance.getMeetingStatus().equals(MeetingStatus.finished)) {
                attendance.setMeetingStatus(MeetingStatus.missed);
            } else if (attendance.getMeetingStatus().equals(MeetingStatus.missed)) {
                attendance.setMeetingStatus(MeetingStatus.finished);
            }
            attendanceRepository.save(attendance);
        }
        return new ResponseObject(HttpStatus.OK, "STATUS_UPDATED", null);
    }

    public int adjustPageNumber(int pageNumber) {
        return pageNumber - 1;
    }

    public void setCompanies(List<User> users) {
        for (User user : users) {
            // get groups
            List<Group> group = groupRepository.findByClientId(user.getId());
            if (group.size() > 0 && group.get(0).getCompany() != null) {
                user.setCompanies(Set.of(group.get(0).getCompany()));
            }
        }
    }
}
