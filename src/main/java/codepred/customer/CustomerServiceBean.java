package codepred.customer;

import codepred.account.AppUserRole;
import codepred.account.User;
import codepred.account.dto.SignupRequest;
import codepred.attendance.Attendance;
import codepred.attendance.AttendanceRepository;
import codepred.bookmark.BookMarkRepository;
import codepred.bookmark.dto.BookmarkDto;
import codepred.client.ClientDetails;
import codepred.client.ClientDetailsRepository;
import codepred.client.ClientRepository;
import codepred.client.ClientService;
import codepred.client.dto.DetailsPerTutorDto;
import codepred.common.util.ResponseObject;
import codepred.company.Company;
import codepred.company.CompanyRepository;
import codepred.config.EmailService;
import codepred.exception.CustomException;
import codepred.group.Group;
import codepred.group.GroupRepository;
import codepred.group.GroupService;
import codepred.group.dto.GroupNameDto;
import codepred.group.dto.GroupsDto;
import codepred.meeting.Event;
import codepred.meeting.EventRepository;
import codepred.meeting.MeetingStatus;
import codepred.meeting.dto.MeetingDto;
import codepred.payment.Payment;
import codepred.payment.PaymentRepository;
import codepred.payment.dto.PaymentDto;
import codepred.payment.dto.ValueDto;
import codepred.tutor.TutorData;
import codepred.tutor.TutorService;
import codepred.tutor.dto.TutorDto;
import codepred.tutor.dto.TutorIdAndNameDto;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static codepred.common.util.DateUtil.*;

@AllArgsConstructor
@Slf4j
@Service
public class CustomerServiceBean {
    private final ClientRepository clientRepository;

    private final TutorService tutorService;
    private final EmailService emailService;
    private final ClientService clientService;
    private final PasswordEncoder passwordEncoder;
    private final GroupService groupService;
    private final AttendanceRepository attendanceRepository;
    private final EventRepository eventRepository;
    private final BookMarkRepository bookMarkRepository;
    private final PaymentRepository paymentRepository;
    private final GroupRepository groupRepository;
    private final CompanyRepository companyRepository;
    private final ClientDetailsRepository clientDetailsRepository;


    public ResponseObject signup(final SignupRequest requestForSignup) {
        final var tutorFromDb = tutorService.findByRegistrationCode(requestForSignup.getCode());
        if (tutorFromDb.isPresent() && tutorFromDb.get().getEmail().equals(requestForSignup.getEmail())) {
            tutorFromDb.get().setPassword(passwordEncoder.encode(requestForSignup.getPassword()));
            tutorFromDb.get().setCode(null);
            tutorFromDb.get().setIsActivated(Boolean.TRUE);
            tutorFromDb.get().setCodeTimeGenerated(null);
            tutorService.save(tutorFromDb.get());
            var response = new ResponseObject(HttpStatus.OK, "EMAIL_SENT", null);
            emailService.sendEmailForRegistrationConfirmation(tutorFromDb.get().getEmail(),
                    "Tutorio",
                    "Potwierdzenie rejestracji");
            return response;
        } else {
            return new ResponseObject(HttpStatus.BAD_REQUEST, "EMAIL_DOES_NOT_EXIST", null);
        }
    }

    public CustomerData getClientData(User client) {
        CustomerData customerData = new CustomerData();

        List<ClientDetails> clientDetailsForAllTutors = clientDetailsRepository.findAllByClientId(client.getId());;
        customerData.setDetailsPerTutorDtos(
                clientDetailsForAllTutors.stream().map( x -> {
                    User user = clientRepository.getById(x.getUser().getId());
                    AppUserRole role = user.getAppUserRoles();
                    String typeToSet = "";
                    if (role.equals(AppUserRole.ROLE_CLIENT)) typeToSet = "student";
                    else if (role.equals(AppUserRole.ROLE_ADMIN)) typeToSet = "company";
                    return new DetailsPerTutorDto(x.getTutorId(), x.getName() + " " + x.getLastname(), typeToSet);
                }).collect(Collectors.toList())
        );

        List<GroupsDto> groupList = groupService.getAllGroups(client);
        customerData.setGroupList(groupList.stream().map(g -> new GroupNameDto(g.getGroupname())).collect(Collectors.toList()));

        List<User> tutorList = tutorService.findAllClientTutors(client);
        customerData.setTutorList(tutorList.stream().map(t -> {
            String name;
            if(t.getTutorDetails().getActivityType() != null && t.getTutorDetails().getActivityType().equals("company")){
                name = t.getTutorDetails().getCompanyName();
            }
            else{
                name = t.getTutorDetails().getName() + " " +
                    t.getTutorDetails().getSurname();
            }
            return new TutorIdAndNameDto(name, t.getId());
        }).collect(Collectors.toList()));
        return customerData;
    }

    @Transactional
    public TutorData getTutorData(User client, Integer tutorId) {
        var tutor = tutorService.getTutorDataById(tutorId);
        var clientDetailsForGivenTutor = clientService.findClientDetailsForGivenTutor(client, tutor);
        var tutorData = new TutorDto(tutor.getId(), clientDetailsForGivenTutor.getMeetingLink(), clientDetailsForGivenTutor.getStorageLink());
        final List<MeetingDto> meetingsData = new ArrayList<>();
        var paymentsData = new ArrayList<PaymentDto>();
        var bookmarkData = new ArrayList<BookmarkDto>();

        if(client.getAppUserRoles().equals(AppUserRole.ROLE_ADMIN)){
            setMeetingsDataForCompany(client, meetingsData);
        }
        else {
            setMeetingsDataForClient(client, tutor, meetingsData);
        }

        List<Payment> paymentsList = new ArrayList<>();
        if(client.getAppUserRoles().equals(AppUserRole.ROLE_ADMIN)){
            Company company = companyRepository.findCompaniesByUsers(client).get(0);
            paymentsList = paymentRepository.findByCompanyId(company.getId());
        }
        else{
            paymentsList = paymentRepository.findByClientId(client.getId());
        }

        for (var item : paymentsList) {
            paymentsData.add(new PaymentDto(item.getId(), item.getInvoiceNumber(), item.getBuyerName(), item.getStatus(),
                    item.getBrutto(), item.getAmountPaid(), item.getValueToBePaid(), item.getDateOfIssue()));
        }

        var bookmarkList = bookMarkRepository.findAllByUser(tutorId);
        for (var item : bookmarkList) {
            bookmarkData.add(new BookmarkDto(item.getId(), null, item.getFileName()));
        }

        final var sortedMeetings = meetingsData.stream().sorted(Comparator.comparing(MeetingDto::getDate))
                .collect(Collectors.toList());

        var response = new TutorData();
        response.setTutorData(tutorData);
        response.setMeetingsData(sortedMeetings);
        response.setPaymentData(paymentsData);
        response.setBookmarkList(bookmarkData);

        return response;
    }

    private void setMeetingsDataForCompany(User client, List<MeetingDto> meetingsData) {
        List<Group> groupsDtoList = groupRepository.getAllGroupForAdmin(client.getId());
        Company company = companyRepository.findCompaniesByUsers(client).get(0);
        for(Group group : groupsDtoList){
            List<Event> eventList = eventRepository.getGroupEvents(group.getId());
            eventList.forEach(event -> {
                var meeting = new MeetingDto(
                    event.getId(),
                    event.getStartTime(),
                    "group",
                    event.getMeetingStatus().toString(),
                    event.getService().getPrice(),
                    company.getIsAvailableToChangeReservation(),
                    company.getIsAvailableToCancelReservation()
                );
                if (LocalDateTime.now().isAfter(event.getStartTime())) {
                    meeting.setStatus(MeetingStatus.finished.name());
                    event.setMeetingStatus(MeetingStatus.finished);
                    event.getAttendances().forEach(a -> a.setMeetingStatus(MeetingStatus.finished));
                    eventRepository.save(event);
                }
                meeting.setPostponePossibility(false);
                if (company.getIsAvailableToChangeReservation()) {
                    var time = company.getMinimumChangeTime();
                    if (getMinutes(event.getStartTime()) > time) meeting.setPostponePossibility(true);
                }
                meeting.setCancelPossibility(false);
                if (company.getIsAvailableToCancelReservation()) {
                    var time = company.getMinimumChangeTime();
                    if (getMinutes(event.getStartTime()) > time) meeting.setCancelPossibility(true);
                }
                meeting.setGroupName(event.getGroup().getGroupName());
                meetingsData.add(meeting);
            });
        }
    }

    private void setMeetingsDataForClient(User client, User tutor, List<MeetingDto> meetingsData) {
        var clientDetailsForGivenTutor = clientService.findClientDetailsForGivenTutor(client, tutor);
        List<Attendance> attendances = attendanceRepository.findByClientId(client.getId());
        for (Attendance attendance : attendances) {
            var event = eventRepository.findById(attendance.getEvent().getId())
                .orElseThrow(() -> new CustomException("EVENT_NOT_FOUND", HttpStatus.BAD_REQUEST));
            if (LocalDateTime.now().isAfter(event.getStartTime()) && attendance.getMeetingStatus() == MeetingStatus.planned) {
                attendance.setMeetingStatus(MeetingStatus.finished);
                attendance = attendanceRepository.saveAndFlush(attendance);
            }
            boolean postponePossibility = Boolean.FALSE;
            if (clientDetailsForGivenTutor.getIsAvailableToChangeReservation() && attendance.getClientType().equals("STUDENT")) {
                var time = clientDetailsForGivenTutor.getMinimumChangeTime();
                if (getMinutes(event.getStartTime()) > time) {
                    postponePossibility = Boolean.TRUE;
                }
            }
            boolean cancelPossibility = Boolean.FALSE;
            if (clientDetailsForGivenTutor.getIsAvailableToCancelReservation() && attendance.getClientType().equals("STUDENT")) {
                var time = clientDetailsForGivenTutor.getMinimumChangeTime();
                if (getMinutes(event.getStartTime()) > time) {
                    cancelPossibility = Boolean.TRUE;
                }
            }
            if (attendance.getClientType().equals("STUDENT") && event.getUsers().contains(tutor)) {
                meetingsData.add(new MeetingDto(
                    event.getId(),
                    event.getStartTime(),
                    "individual",
                    attendance.getMeetingStatus().toString(),
                    event.getService().getPrice(),
                    postponePossibility,
                    cancelPossibility
                ));
            } else if (attendance.getClientType().equals("CLIENT_IN_GROUP") && event.getUsers().contains(tutor)) {
                var meetingDto = new MeetingDto(
                        event.getId(),
                        event.getStartTime(),
                        "group",
                        attendance.getMeetingStatus().toString(),
                        event.getService().getPrice(),
                        postponePossibility,
                        cancelPossibility
                );
                meetingDto.setGroupName(event.getGroup().getGroupName());
                meetingsData.add(meetingDto);
            }
        }
    }

    public List<ValueDto> getAvailableHours(Integer tutorId, String date, Integer eventId) {

        final var event = eventRepository.getById(eventId);
        final var eventDuration = Duration.between(event.getStartTime(), event.getEndTime()).toMinutes();
        final var extractedDate = extractDate(date);
        final var dateFrom = convertToStartOfTheDay(extractedDate);
        final var dateTo = convertToEndOfTheDay(extractedDate);
        final var shortTermEvents = eventRepository.getAllTutorEventByDay(tutorId, convertToDateTime(dateFrom), convertToDateTime(dateTo));
        int dayOfWeek = convertToDateTime(dateFrom).getDayOfWeek().getValue();
        if(dayOfWeek == 7) dayOfWeek = 0;
        final var longTermUnavailability = eventRepository.getAvailabilitiesForTutor(tutorId, dayOfWeek);

        List<DateContainer> busyHours = shortTermEvents.stream().map(x ->
                new DateContainer(x.getStartTime(), x.getEndTime())).collect(Collectors.toList());

        List<DateContainer> availableHours = longTermUnavailability.stream().map(x ->
                new DateContainer(x.getStartTime(), x.getEndTime().minusMinutes(eventDuration-1))).collect(Collectors.toList());

        List<LocalDateTime> allHours = generateTimesForDateTimeString(date);
        allHours.removeIf(timeWindow ->
                              busyHours.stream().anyMatch(busyHour ->
                                                              timeWindow.isAfter(busyHour.startDate.minusHours(1)) && timeWindow.isBefore(busyHour.endDate)
                              )
        );

        allHours.removeIf(timeWindow -> shouldTimeWindowBeDeleted(timeWindow, availableHours));

        if(availableHours.isEmpty()) {
            allHours.clear();
            return convertToLocalTimeList(allHours);
        }
        return convertToLocalTimeList(allHours);
    }

    private boolean shouldTimeWindowBeDeleted(LocalDateTime timeWindow, List<DateContainer> availableHours){
        for(DateContainer availableHour : availableHours) {
            var availableHourStartTime = LocalTime.of(availableHour.startDate.getHour(), availableHour.startDate.getMinute());
            var timeWindowTime = LocalTime.of(timeWindow.getHour(), timeWindow.getMinute());
            var availableHourEndTime = LocalTime.of(availableHour.endDate.getHour(), availableHour.endDate.getMinute());
            if(!timeWindowTime.isBefore(availableHourStartTime) && timeWindowTime.isBefore(availableHourEndTime)) return false;
        }
        return true;
    }
}
