package codepred.meeting;

import codepred.account.AppUserRole;
import static codepred.common.util.DateUtil.parseTimeFromString;


import codepred.account.User;
import codepred.attendance.Attendance;
import codepred.attendance.AttendanceRepository;
import codepred.calendar.availability.AvailabilityDay;
import codepred.calendar.availability.AvailabilityForCalendarReadDto;
import codepred.calendar.get.ClientForEventDto;
import codepred.calendar.get.ClientsDataDto;
import codepred.calendar.get.EventDto;
import codepred.calendar.get.EventFullDataDto;
import codepred.calendar.save.NewEventRequest;
import codepred.client.ClientDetails;
import codepred.client.ClientRepository;
import codepred.client.ClientService;
import codepred.common.mapper.EventMapper;
import codepred.common.util.ResponseObject;
import codepred.config.EmailService;
import codepred.exception.CustomException;
import codepred.group.Group;
import codepred.group.GroupRepository;
import codepred.meeting.dto.MeetingNewDateDto;
import codepred.service.ServiceRepository;
import codepred.tutor.TutorDetails;
import codepred.tutor.TutorRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
@Service
public class EventService {

    private final ClientRepository clientRepository;
    private final GroupRepository groupRepository;
    private final EventRepository eventRepository;
    private final ClientService clientService;
    private final EventMapper eventMapper;
    private final ServiceRepository serviceRepository;
    private final TutorRepository tutorRepository;
    private final AttendanceRepository attendanceRepository;
    private final EmailService emailService;

    public List<ClientsDataDto> getClientDataToEvent(Integer tutorId) {
        var clients = clientRepository.findByUsersId(tutorId);
        clients = clients.stream().filter(c -> c.getAppUserRoles().name().equals(AppUserRole.ROLE_CLIENT.name())).collect(Collectors.toList());
        var groups = groupRepository.findAllGroup(tutorId);
        var tutor = getTutorDataById(tutorId);
        List<ClientsDataDto> clientsDataDto = new ArrayList<>();

        for (User client : clients) {
            List<codepred.service.Service> clientServices = new ArrayList<>(client.getServices());
            var clientDetailsForGivenTutor = clientService.findClientDetailsForGivenTutor(client, tutor);
            Integer firstServiceId = null;
            if (!clientServices.isEmpty() && clientServices.get(0) != null) {
                firstServiceId = clientServices.get(0).getId();
            }

            ClientsDataDto clientDTO = ClientsDataDto.builder()
                .id(client.getId())
                .type("STUDENT")
                .name(clientDetailsForGivenTutor.getName() + " " + clientDetailsForGivenTutor.getLastname())
                .serviceId(firstServiceId)
                .build();
            clientsDataDto.add(clientDTO);
        }

        for (Group group : groups) {
            Integer serviceId = null;
            if (group.getService() != null) {
                serviceId = group.getService().getId();
            }

            ClientsDataDto groupDTO = ClientsDataDto.builder()
                .id(group.getId())
                .type("GROUP")
                .name(group.getGroupName())
                .serviceId(serviceId)
                .build();
            if (group.getUser().size() > 1) {
                clientsDataDto.add(groupDTO);
            }
        }
        return clientsDataDto;
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

    public List<EventDto> getEventsData(Integer tutorId) {
        List<Event> events = eventRepository.findEventsByUserId(tutorId);
        List<EventDto> eventDataDTOs = new ArrayList<>();
        for (Event event : events) {
            EventDto eventDto = new EventDto();
            if (event.getType().equals(MeetingType.UNAVAILABLE)) {
                eventDto = new EventDto(event.getId(),
                                                event.getTitle(),
                                                "#999999",
                                                event.getStartTime(),
                                                event.getEndTime());
            } else {
                eventDto = new EventDto(event.getId(),
                                                event.getTitle(),
                                                event.getBackgroundColor(),
                                                event.getStartTime(),
                                                event.getEndTime());
            }
            if (event.getWasCanceled()) {
                eventDto.setTitle(event.getTitle() + " (Odwołane)");
            }
            eventDataDTOs.add(eventDto);
        }
        return eventDataDTOs;
    }

    public List<codepred.service.Service> getServicesData(Integer tutorId) {
        return serviceRepository.findServiceByUserId(tutorId);
    }

    @Transactional
    public ResponseObject saveEvent(NewEventRequest eventRequest, User user) {

        Event newEvent = Event.builder()
            .repeatEvent(eventRequest.getRepeatEvent())
            .endTime(eventRequest.getEndTime())
            .startTime(eventRequest.getStartTime())
            .meetingStatus(MeetingStatus.planned)
            .createdAt(LocalDateTime.now(ZoneId.of("Europe/Warsaw")))
            .wasCanceled(false)
            .type(eventRequest.getType())
            .isAvailability(false)
            .group(null)
            .oldStartTime(eventRequest.getStartTime())
            .notifyUsers(eventRequest.getNotifyUsers())
            .service(serviceRepository.findServiceById(eventRequest.getServiceId()).orElse(null))
            .build();

        boolean isGroup = eventRequest.getIsGroup();

        var tutorFromDB = tutorRepository.findById(user.getId()).orElseThrow();

        if (!eventRepository.existsByTutorIdAndTimeRange(user.getId(), newEvent.getStartTime(), newEvent.getEndTime())) {

            if (newEvent.getRepeatEvent() != codepred.meeting.RepeatMeeting.NONE) {
                var eventNew = eventRepository.saveAndFlush(newEvent);
                tutorFromDB.getEvents().add(eventNew);
                tutorRepository.save(tutorFromDB);
                createRepeatableEvents(newEvent, user.getId(), eventRequest.getClientId(), isGroup);
                return new ResponseObject(HttpStatus.CREATED, "EVENT_SUCCESSFULLY_CREATED_WITH_ID=" + eventNew.getId(), null);
            }

            if (eventRequest.getServiceId() != null) {
                switch (eventRequest.getType()) {
                    case AVAILABLE:
                        newEvent.setIsAvailability(false);
                        break;
                    case UNAVAILABLE:
                        newEvent.setIsAvailability(true);
                        break;
                    default:
                        throw new IllegalArgumentException("WRONG_AVAILABILITY_TYPE");
                }
            } else {
                newEvent.setTitle(eventRequest.getTitle());
                switch (eventRequest.getType()) {
                    case AVAILABLE:
                        newEvent.setIsAvailability(true);
                        break;
                    case UNAVAILABLE:
                        newEvent.setIsAvailability(false);
                        break;
                    default:
                        throw new IllegalArgumentException("WRONG_AVAILABILITY_TYPE");
                }
            }

            var eventNew = eventRepository.saveAndFlush(newEvent);
            tutorFromDB.getEvents().add(eventNew);
            tutorRepository.save(tutorFromDB);

            return new ResponseObject(HttpStatus.CREATED, "EVENT_SUCCESSFULLY_CREATED_WITH_ID=" + eventNew.getId(), null);
        } else {
            return new ResponseObject(HttpStatus.BAD_REQUEST, "THIS_TIME_IS_BUSY", null);
        }
    }

    private Event cloneEvent(Event originalEvent) {
        return Event.builder()
            .repeatEvent(originalEvent.getRepeatEvent())
            .additionalInformation(originalEvent.getAdditionalInformation())
            .createdAt(originalEvent.getCreatedAt())
            .service(originalEvent.getService())
            .backgroundColor(originalEvent.getBackgroundColor())
            .type(originalEvent.getType())
            .title(originalEvent.getTitle())
            .group(originalEvent.getGroup())
            .users(originalEvent.getUsers())
            .meetingStatus(originalEvent.getMeetingStatus())
            .isAvailability(originalEvent.getIsAvailability())
            .updatedAt(originalEvent.getUpdatedAt())
            .endTime(originalEvent.getEndTime())
            .startTime(originalEvent.getStartTime())
            .oldStartTime(originalEvent.getOldStartTime())
            .build();
    }

    private void createRepeatableEvents(Event event, Integer tutorId, Integer clientId, boolean isGroup) {
        User tutor = tutorRepository.findById(tutorId)
            .orElseThrow(() -> new CustomException("TUTOR_DOESNT_EXIST", HttpStatus.BAD_REQUEST));

        List<Event> repeatedEvents = new LinkedList<>();
        List<Attendance> attendancesList = new LinkedList<>();

        User clientStudent = !isGroup ? clientRepository.findById(clientId)
            .orElseThrow(() -> new CustomException("CLIENT_DOESNT_EXIST", HttpStatus.BAD_REQUEST)) : null;

        Group group = isGroup ? groupRepository.findGroupByIdAndUser(clientId, tutorId)
            .orElseThrow(() -> new CustomException("EVENT_OR_CLIENT_WITH_THIS_ID_NOT_FOUND", HttpStatus.BAD_REQUEST)) : null;

        switch (event.getRepeatEvent()) {

            case EVERY_WEEK:
                for (int i = 1; i < 26; i++) {
                    Event clonedEvent = cloneEvent(event);
                    clonedEvent.setEndTime(event.getEndTime().plusWeeks(i));
                    clonedEvent.setStartTime(event.getStartTime().plusWeeks(i));
                    connectTutorToEvent(tutor, repeatedEvents, clonedEvent);
                }
                break;

            case EVERY_FORTNIGHT:
                for (int i = 1; i < 13; i++) {
                    Event clonedEvent = cloneEvent(event);
                    clonedEvent.setEndTime(event.getEndTime().plusWeeks(2 * i));
                    clonedEvent.setStartTime(event.getStartTime().plusWeeks(2 * i));
                    connectTutorToEvent(tutor, repeatedEvents, clonedEvent);
                }
                break;

            case EVERY_MONTH:
                for (int i = 1; i < 6; i++) {
                    Event clonedEvent = cloneEvent(event);
                    clonedEvent.setEndTime(event.getEndTime().plusMonths(i));
                    clonedEvent.setStartTime(event.getStartTime().plusMonths(i));
                    connectTutorToEvent(tutor, repeatedEvents, clonedEvent);
                }
                break;

            case NONE:
                break;
        }

        eventRepository.saveAllAndFlush(repeatedEvents);

        for (Event repeatedEvent : repeatedEvents) {
            if (!isGroup) {
                repeatedEventConnectToClient(tutor, clientStudent, repeatedEvent, null, attendancesList, isGroup);
            } else {
                repeatedEventConnectToClient(tutor, null, repeatedEvent, group, attendancesList, isGroup);
            }
        }

        tutorRepository.save(tutor);
        if (!isGroup) {
            clientRepository.save(clientStudent);
        } else {
            groupRepository.save(group);
        }
        attendanceRepository.saveAll(attendancesList);
    }

    private void connectTutorToEvent(User tutor, List<Event> repeatedEvents, Event clonedEvent) {
        Set<User> users = new HashSet<>();
        users.add(tutor);
        clonedEvent.setUsers(users);
        repeatedEvents.add(clonedEvent);
    }

    private void repeatedEventConnectToClient(User tutor,
                                              User clientStudent,
                                              Event repeatedEvent,
                                              Group group,
                                              List<Attendance> attendancesList,
                                              boolean isGroup) {
        tutor.getEvents().add(repeatedEvent);
        if (!isGroup) {
            var clientDetailsForGivenTutor = clientService.findClientDetailsForGivenTutor(clientStudent, tutor);
            repeatedEvent.setWasCanceled(false);
            repeatedEvent.setNotifyUsers(false);
            repeatedEvent.setMeetingStatus(MeetingStatus.planned);
            repeatedEvent.setIsAvailability(false);

            repeatedEvent.setTitle(
                    clientDetailsForGivenTutor.getName() + " " + clientDetailsForGivenTutor.getLastname());
            repeatedEvent.setBackgroundColor(clientDetailsForGivenTutor.getColor());
            repeatedEvent.setTitle(
                    clientDetailsForGivenTutor.getName() + " " + clientDetailsForGivenTutor.getLastname());
            repeatedEvent.getUsers().add(clientStudent);
            clientStudent.getEvents().add(repeatedEvent);

            attendanceRepository.removeAttendanceById(repeatedEvent.getId());
            var attendance = new Attendance();
            attendance.setEvent(repeatedEvent);
            attendance.setClientId(clientStudent.getId());
            attendance.setClientType("STUDENT");
            attendance.setServiceId(repeatedEvent.getService().getId());
            attendancesList.add(attendance);
        } else {
            attendanceRepository.removeAttendanceById(repeatedEvent.getId());
            var clients = eventRepository.getConnectedClients(tutor.getId(), group.getId());
            for (Object[] data : clients) {
                var attendance = new Attendance();
                attendance.setClientType("CLIENT_IN_GROUP");
                attendance.setClientId((Integer) data[0]);
                attendance.setEvent(repeatedEvent);
                attendance.setServiceId(repeatedEvent.getService().getId());
                attendancesList.add(attendance);
            }

            repeatedEvent.setTitle(group.getGroupName());
            repeatedEvent.setBackgroundColor(group.getColor());
            repeatedEvent.setGroup(group);
            repeatedEvent.setWasCanceled(false);
        }
    }

    @Transactional
    public ResponseObject addServiceToEvent(User user, Integer eventId, Integer serviceId) {
        var event = eventRepository.findEventByIdAndUsers(eventId, user.getId());
        var service = serviceRepository.findServiceByIdAndUser(serviceId, user);
        if (event.isEmpty() || service.isEmpty()) {
            return new ResponseObject(HttpStatus.BAD_REQUEST, "EVENT_OR_SERVICE_WITH_THIS_ID_NOT_FOUND", null);
        }
        eventRepository.removeServiceInEvent(eventId);
        eventRepository.updateServiceInEvent(event.get().getId(), service.get().getId());
        return new ResponseObject(HttpStatus.OK, "SERVICE_SUCCESSFULLY_ADD_TO_EVENT", null);
    }

    @Transactional
    public ResponseObject addClientToEvent(User tutor, Integer eventId, Integer clientId) {
        var wasDateChanged = false;
        var event = eventRepository.findEventByIdAndUsers(eventId, tutor.getId())
            .orElseThrow(() -> new CustomException("EVENT_OR_CLIENT_WITH_THIS_ID_NOT_FOUND", HttpStatus.BAD_REQUEST));
        var client = clientRepository.findByTutorIdAndClientId(tutor.getId(), clientId)
            .orElseThrow(() -> new CustomException("EVENT_OR_CLIENT_WITH_THIS_ID_NOT_FOUND", HttpStatus.BAD_REQUEST));
        if (!event.getOldStartTime().equals(event.getStartTime())) {
            wasDateChanged = true;
        }
        eventRepository.removeClientInEvent(eventId, tutor.getId());
        eventRepository.removeGroupInEvent(eventId);
        var clientDetailsForGivenTutor = clientService.findClientDetailsForGivenTutor(client, tutor);
        event.setBackgroundColor(clientDetailsForGivenTutor.getColor());
        event.setTitle(clientDetailsForGivenTutor.getName() + " " + clientDetailsForGivenTutor.getLastname());
        var eventForAttendance = eventRepository.saveAndFlush(event);
        event.getUsers().add(client);
        client.getEvents().add(event);
        clientRepository.save(client);
        attendanceRepository.removeAttendanceById(eventId);
        var attendance = new Attendance();
        attendance.setEvent(eventForAttendance);
        attendance.setClientId(clientId);
        attendance.setClientType("STUDENT");
        attendance.setServiceId(eventForAttendance.getService().getId());
        if (event.getNotifyUsers() == Boolean.TRUE && !wasDateChanged) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

            LocalDateTime startTime = event.getStartTime();
            String formattedStartTime = startTime.format(formatter);

            emailService.sendEmailForEventInvitation(client.getEmail(),
                                                     "Tutorio",
                                                     tutor.getTutorDetails().getName() + " " + tutor.getTutorDetails()
                                                         .getSurname(),
                                                     formattedStartTime,
                    clientDetailsForGivenTutor.getName());
        } else if (event.getNotifyUsers() == Boolean.TRUE && wasDateChanged) {
            sendEmailForPostponedEvent(event, tutor);
        }
        attendanceRepository.save(attendance);
        return new ResponseObject(HttpStatus.OK, "CLIENT_SUCCESSFULLY_ADDED_TO_EVENT", null);
    }

    @Transactional
    public ResponseObject assignGroupToEvent(User tutor, Integer eventId, Integer groupId) {
        var event = eventRepository.findEventByIdAndUsers(eventId, tutor.getId())
            .orElseThrow(() -> new CustomException("EVENT_OR_CLIENT_WITH_THIS_ID_NOT_FOUND", HttpStatus.BAD_REQUEST));
        var group = groupRepository.findGroupByIdAndUser(groupId, tutor.getId())
            .orElseThrow(() -> new CustomException("EVENT_OR_CLIENT_WITH_THIS_ID_NOT_FOUND", HttpStatus.BAD_REQUEST));

        eventRepository.removeClientInEvent(eventId, tutor.getId());
        eventRepository.removeGroupInEvent(eventId);
        event.setTitle(group.getGroupName());
        event.setBackgroundColor(group.getColor());
        event.setGroup(group);
        var eventForAttendance = eventRepository.saveAndFlush(event);

        attendanceRepository.removeAttendanceById(eventId);
        var clients = eventRepository.getConnectedClients(tutor.getId(), group.getId());
        var attendances = new ArrayList<Attendance>();
        for (Object[] data : clients) {
            Optional<User> client = clientRepository.findUserById((Integer) data[0]);
            if(client.isEmpty()) continue;
            if(client.get().getClientDetails().size() < 1 && !client.get().getAppUserRoles().equals(AppUserRole.ROLE_TUTOR)) continue;
            var clientDetailsForGivenTutor = clientService.findClientDetailsForGivenTutor(client.get(), tutor);
            attendances.add(new Attendance("CLIENT_IN_GROUP",
                                           (Integer) data[0],
                                           eventForAttendance.getService().getId(),
                                           eventForAttendance));
            boolean wasDateChanged = !event.getOldStartTime().equals(event.getStartTime());
            if (event.getNotifyUsers() == Boolean.TRUE && event.getOldStartTime() != null && wasDateChanged) {

                emailService.sendEmailForEventChange(data[1].toString(),
                        "Tutorio",
                        tutor.getTutorDetails().getName() + " " + tutor.getTutorDetails()
                                .getSurname(),
                        event.getOldStartTime().toString().replace("T", " "),
                        event.getStartTime().toString().replace("T", " "),
                        clientDetailsForGivenTutor.getName() != null ? clientDetailsForGivenTutor.getName() : null);
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                LocalDateTime startTime = event.getStartTime();
                String formattedStartTime = startTime.format(formatter);
                emailService.sendEmailForEventInvitation(data[1].toString(),
                        "Tutorio",
                        tutor.getTutorDetails().getName() + " " + tutor.getTutorDetails()
                                .getSurname(),
                        formattedStartTime,
                        clientDetailsForGivenTutor.getName() != null ? clientDetailsForGivenTutor.getName() : null);
            }
        }
        attendanceRepository.saveAll(attendances);
        return new ResponseObject(HttpStatus.OK, "GROUP_SUCCESSFULLY_ADDED_TO_EVENT", null);
    }

    @Transactional
    public ResponseObject removeEvent(Integer eventId, User tutor, boolean happenedByTutor, User userWhoRemovedEvent) {

        // delete event by tutor - attendanceId is eventId
        var event = eventRepository.findById(eventId);
        if (event.isPresent()) {
            sendEmailsForDeletedEvent(event.get(), tutor, happenedByTutor, userWhoRemovedEvent);
            final var attendances = event.get().getAttendances();
            for (Attendance attendance : attendances) {
                attendance.setEvent(null);
                attendanceRepository.saveAndFlush(attendance);
            }
            event.get().setAttendances(new HashSet<>());
            event.get().setUsers(new HashSet<>());
            eventRepository.saveAndFlush(event.get());
            eventRepository.delete(event.get());
            attendanceRepository.deleteAll(attendances);

            return new ResponseObject(HttpStatus.ACCEPTED, "EVENT_SUCCESSFUL_DELETED", null);
        } else {
            Attendance attendance = attendanceRepository.getById(eventId);
            event = eventRepository.getEventByIdAndTutor(attendance.getEvent().getId(), tutor.getId());
            sendEmailsForDeletedEvent(event.get(), tutor, happenedByTutor, userWhoRemovedEvent);
            if (event.isPresent()) {
                attendance.setMeetingStatus(MeetingStatus.finished);
                attendanceRepository.save(attendance);
                event.get().setWasCanceled(true);
                eventRepository.saveAndFlush(event.get());
                return new ResponseObject(HttpStatus.ACCEPTED, "EVENT_SUCCESSFUL_DELETED", null);
            }
        }

        log.debug("EventService ==> removeEvent() - end: HttpStatus = {}, message = {}, token = {}",
                  HttpStatus.NOT_FOUND,
                  "EVENT_NOT_FOUND",
                  null);
        return new ResponseObject(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", null);
    }

    public void sendEmailsForDeletedEvent(Event event, User tutor, boolean happenedByTutor, User userWhoRemovedEvent) {
        log.info("Event is being deleted");
        List<User> users = tutorRepository.findAllUserByEventId(event.getId());
        users.remove(tutor);

        // send emails for group participants and tutor
        if(event.getGroup() != null) {
            var groupUsers = eventRepository.getConnectedClients(tutor.getId(), event.getGroup().getId());
            for (Object[] data : groupUsers) {
                Integer clientId = (Integer) data[0];
                Optional<User> client = clientRepository.findUserById(clientId);
                if(client.isEmpty()) throw new CustomException("Client does not exist", HttpStatus.BAD_REQUEST);
                if(client.get().getClientDetails().size() != 0) {
                    ClientDetails clientDetails = clientService.findClientDetailsForGivenTutor(client.get(), tutor);
                    emailService.sendEmailForEventRemoval((String) data[1],
                            "Tutorio",
                            happenedByTutor ? "Informujemy, że korepetytor usunął(ęła) spotkanie"
                                    : userWhoRemovedEvent.getAppUserRoles().equals(AppUserRole.ROLE_ADMIN)
                                    ? "Informujemy, że administrator" : "Potwierdzamy usunięcie spotkania",
                            happenedByTutor ? "" : tutor.getTutorDetails().getName() + " " + tutor.getTutorDetails().getSurname(),
                            "usunął(ęła) spotkanie",
                            event.getStartTime().toString().replace("T", " "),
                            clientDetails.getName());
                }
            }
            if(!happenedByTutor && userWhoRemovedEvent.getAppUserRoles().equals(AppUserRole.ROLE_ADMIN)) {
                var clientDetailsForGivenTutor = clientService.findClientDetailsForGivenTutor(userWhoRemovedEvent, tutor);
                emailService.sendEmailForEventRemoval(tutor.getEmail(),
                        "Tutorio",
                        "Informujemy, że administrator",
                        clientDetailsForGivenTutor.getName() + " " + clientDetailsForGivenTutor.getLastname(),
                        "usunął(ęła) spotkanie",
                        event.getStartTime().toString().replace("T", " "),
                        tutor.getTutorDetails().getName());
            }
        }

        // email for single clients and tutor
        else if(users.size() > 0) {
            User client = users.get(0);
            if(client.getClientDetails().size() != 0) {
                var clientDetailsForGivenTutor = clientService.findClientDetailsForGivenTutor(client, tutor);
                if (!happenedByTutor) {
                    emailService.sendEmailForEventRemoval(tutor.getEmail(),
                            "Tutorio",
                            "Informujemy, że Twój klient(ka)",
                            clientDetailsForGivenTutor.getName() + " " + clientDetailsForGivenTutor.getLastname(),
                            "usunął(ęła) spotkanie",
                            event.getStartTime().toString().replace("T", " "),
                            tutor.getTutorDetails().getName());
                }
                emailService.sendEmailForEventRemoval(client.getEmail(),
                        "Tutorio",
                        happenedByTutor ? "Informujemy, że korepetytor usunął(ęła) spotkanie"
                                : "Potwierdzamy usunięcie spotkania",
                        happenedByTutor ? "" : tutor.getTutorDetails().getName() + " " + tutor.getTutorDetails().getSurname(),
                        "",
                        event.getStartTime().toString().replace("T", " "),
                        clientDetailsForGivenTutor.getName());
            }
        }
        }

    public void sendEmailForPostponedEvent(Event event, User editor) {
        log.info("Event is being updated");
        List<User> users = tutorRepository.findAllUserByEventId(event.getId());
        users.forEach(userToSendEmail -> {
            if(userToSendEmail.getTutorDetails() != null && editor.getTutorDetails() != null) {
                //skip when userToSend=tutor and editor=tutor
            } else {
                if(userToSendEmail.getEmail().equals(editor.getEmail())) return;
                if(userToSendEmail.getClientDetails().size() < 1 && !userToSendEmail.getAppUserRoles().equals(AppUserRole.ROLE_TUTOR)) return;
                emailService.sendEmailForEventChange(userToSendEmail.getEmail(),
                        "Tutorio",
                        editor.getTutorDetails() != null
                                ? editor.getTutorDetails().getName() + " " + editor.getTutorDetails().getSurname()
                                : clientService.findClientDetailsForGivenTutor(editor, userToSendEmail).getName() + " " + clientService.findClientDetailsForGivenTutor(editor, userToSendEmail).getLastname(),
                        event.getOldStartTime().toString().replace("T", " "),
                        event.getStartTime().toString().replace("T", " "),
                        userToSendEmail.getClientDetails() != null && userToSendEmail.getClientDetails().size() > 0
                                ? clientService.findClientDetailsForGivenTutor(userToSendEmail, editor).getName()
                                : userToSendEmail.getTutorDetails().getName());
            }
        });
    }

    public ResponseObject updateEventData(User user, Event newEvent) {

        var eventFromDb = eventRepository.findEventByIdAndUsers(newEvent.getId(), user.getId())
            .orElseThrow(() -> new CustomException("EVENT_NOT_FOUND", HttpStatus.BAD_REQUEST));
        var isExist = eventRepository.findEventsAtTimeRangeForTutor(user.getId(),
                                                                    newEvent.getId(),
                                                                    newEvent.getStartTime(),
                                                                    newEvent.getEndTime());

        if (isExist.isPresent() && isExist.get().getId().equals(newEvent.getId())) {
            return new ResponseObject(HttpStatus.BAD_REQUEST, "THIS_TIME_IS_BUSY_OR_EVENT_DOES_NOT_EXIST", null);
        } else {
            newEvent.setId(eventFromDb.getId());
            newEvent.setOldStartTime(eventFromDb.getStartTime());
            eventRepository.save(newEvent);

            return new ResponseObject(HttpStatus.ACCEPTED, "EVENT_SUCCESSFULLY_UPDATED", null);
        }

    }

    public Event updateEvent(Optional<Event> event, MeetingNewDateDto newDate) {
        final var minutes = getMinutesBetween(event.get().getStartTime(), event.get().getEndTime());
        event.get().setStartTime(newDate.getNewDate());
        event.get().setEndTime(newDate.getNewDate().plus(minutes, ChronoUnit.MINUTES));
        return eventRepository.saveAndFlush(event.get());
    }

    public static long getMinutesBetween(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return ChronoUnit.MINUTES.between(dateTime1, dateTime2);
    }

    public EventFullDataDto getCertainEvent(User tutor, Integer eventId) {
        var eventFromDB = eventRepository.findEventByIdAndUsers(eventId, tutor.getId());
        if (eventFromDB.isPresent()) {
            var response = eventMapper.toEventFullDataDto(eventFromDB.get());
            var clientId = eventRepository.getClientIdForEvent(tutor.getId(), eventId);
            if (clientId != null) {
                ClientForEventDto client = new ClientForEventDto();
                response.setClient(client);
                response.getClient().setId(clientId);
                var clientData = eventRepository.getClientDataForEvent(clientId, tutor.getId());
                if (!clientData.isEmpty()) {
                    Object[] record = clientData.get(0);
                    response.getClient().setName((String) record[0]);
                    response.getClient().setLastname((String) record[1]);
                    response.getClient().setType("STUDENT");
                }
            } else if (eventFromDB.get().getGroup() != null) {
                var group = eventFromDB.get().getGroup();
                ClientForEventDto client = new ClientForEventDto();
                response.setClient(client);
                response.getClient().setId(group.getId());
                response.getClient().setName(group.getGroupName());
                response.getClient().setType("GROUP");
            }
            return response;
        } else {
            throw new CustomException("EVENT_NOT_FOUND", HttpStatus.BAD_REQUEST);
        }
    }

    private List<AvailabilityDay> createDefaultAvailabilitiesList() {
        final List<AvailabilityDay> availabilitiesList = getAvailabilityDays();

        for (int day = 1; day <= 5; day++) {
            AvailabilityDay availabilityDay = availabilitiesList.get(day - 1);
            List<EventEntry> eventsList = availabilityDay.getEvents();

            EventEntry eventEntry = new EventEntry();
            eventEntry.setStartTime("08:00");
            eventEntry.setEndTime("16:00");
            eventEntry.setType("AVAILABLE");

            eventsList.add(eventEntry);
        }

        // Set Saturday and Sunday as UNAVAILABLE
        for (int day = 6; day <= 7; day++) {
            AvailabilityDay availabilityDay = availabilitiesList.get(day - 1);
            List<EventEntry> eventsList = availabilityDay.getEvents();

            EventEntry eventEntry = new EventEntry();
            eventEntry.setStartTime("00:00");
            eventEntry.setEndTime("23:59");
            eventEntry.setType("AVAILABLE");
            availabilityDay.setIsCheckboxSelected(Boolean.FALSE);

            eventsList.add(eventEntry);
        }
        return availabilitiesList;
    }

    @NotNull
    private static List<AvailabilityDay> getAvailabilityDays() {
        final List<AvailabilityDay> availabilitiesList = getDays();
        return availabilitiesList;
    }


    public void addDefaultAvailability(User user){
        List<AvailabilityDay> availabilities = createDefaultAvailabilitiesList();
        setAvailabilityForTutor(user,availabilities);
    }

    @Transactional
    public ResponseObject setAvailabilityForTutor(User user, List<AvailabilityDay> availabilities) {
        var tutor = tutorRepository.findById(user.getId());
        var eventsToRemove = eventRepository.getAvailabilitiesForTutor(user.getId());
        for (Event event : eventsToRemove) {
            eventRepository.removeConnectionAvailabilities(user.getId(), event.getId());
            eventRepository.removeAvailabilities(event.getId());
        }

        List<Event> events = new ArrayList<>();

        for (AvailabilityDay availabilityDay : availabilities) {
            String translatedDayName = availabilityDay.getTranslatedName();
            DayOfWeek dayOfWeek = getDayOfWeekFromTranslatedName(translatedDayName);
            if (dayOfWeek == null) {
                // Invalid translated day name, skip this availabilityDay
                continue;
            }

            List<EventEntry> eventEntries = availabilityDay.getEvents();
            for (EventEntry eventEntry : eventEntries) {

                LocalDateTime startTime = parseTimeFromString(eventEntry.getStartTime(), dayOfWeek);
                LocalDateTime endTime = parseTimeFromString(eventEntry.getEndTime(), dayOfWeek);

                if (startTime.isAfter(endTime)) {
                    return new ResponseObject(HttpStatus.BAD_REQUEST, "UNSUCCESSFUL_START_TIME_MUST_BE_BEFORE_END_TIME", null);
                }

                Event event = createEvent(availabilityDay, startTime, endTime);
                event.getUsers().add(tutor.get());

                events.add(event);
            }
        }

        var savedEvents = new HashSet<>(eventRepository.saveAllAndFlush(events));
        var currentEvents = tutor.get().getEvents();
        currentEvents.addAll(savedEvents);
        tutor.get().setEvents(currentEvents);
        tutorRepository.save(tutor.get());
        return new ResponseObject(HttpStatus.OK, "AVAILABILITIES_SUCCESSFULLY_SET", null);
    }

    @NotNull
    private static Event createEvent(AvailabilityDay availabilityDay, LocalDateTime startTime, LocalDateTime endTime) {
        Event event = new Event();
        event.setStartTime(startTime);
        event.setEndTime(endTime);

        if (availabilityDay.getIsCheckboxSelected().equals(Boolean.FALSE)) {
            event.setEndTime(startTime);
        }

        if (availabilityDay.getIsCheckboxSelected().equals(Boolean.TRUE)) {
            event.setType(MeetingType.AVAILABLE);
        } else {
            event.setType(MeetingType.UNAVAILABLE);
        }
        event.setIsAvailability(Boolean.TRUE);
        event.setUsers(new HashSet<>());
        return event;
    }

    @Transactional
    public List<AvailabilityForCalendarReadDto> getAvailabilitiesForCalendar(User user) {
        var availabilities = eventRepository.getAllAvailabilitiesForTutor(user.getId());
        List<AvailabilityForCalendarReadDto> availabilitiesList = new ArrayList<>();
        if (availabilities.isEmpty()) {
            for (int day = 1; day <= 5; day++) {
                List<Integer> daysOfWeekList = Collections.singletonList(day);

                LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now().with(DayOfWeek.of(day)), LocalTime.of(8, 0));
                LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now().with(DayOfWeek.of(day)), LocalTime.of(16, 0));

                AvailabilityForCalendarReadDto availabilityForCalendarReadDto = AvailabilityForCalendarReadDto.builder()
                    .id(null)
                    .daysOfWeek(daysOfWeekList)
                    .startTime(startDateTime)
                    .endTime(endDateTime)
                    .build();

                availabilitiesList.add(availabilityForCalendarReadDto);
            }
        } else {
            for (Event event : availabilities) {
                Integer dayOfWeek = event.getStartTime().getDayOfWeek().getValue();
                if (dayOfWeek == 7) {
                    dayOfWeek = 0;
                }

                List<Integer> daysOfWeekList = Collections.singletonList(dayOfWeek);

                AvailabilityForCalendarReadDto availabilityForCalendarReadDto = AvailabilityForCalendarReadDto.builder()
                    .id(event.getId())
                    .daysOfWeek(daysOfWeekList)
                    .startTime(event.getStartTime())
                    .endTime(event.getEndTime())
                    .build();

                availabilitiesList.add(availabilityForCalendarReadDto);
            }

        }
        return availabilitiesList;
    }

    @Transactional
    public List<AvailabilityDay> getAvailabilitiesForSettings(User user) {
        var availabilities = eventRepository.getAllAvailabilitiesForTutor(user.getId());
        final List<AvailabilityDay> availabilitiesList = getDays();

        // If there are any existing availabilities, update the events for other days
        for (Event event : availabilities) {
            int dayOfWeek = event.getStartTime().getDayOfWeek().getValue();
            AvailabilityDay availabilityDay = availabilitiesList.get(dayOfWeek - 1);
            List<EventEntry> eventsList = availabilityDay.getEvents();

            EventEntry eventEntry = new EventEntry();
            eventEntry.setStartTime(event.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            eventEntry.setEndTime(event.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            String eventTypeString = event.getType().toString();
            eventEntry.setType(eventTypeString);

            if (event.getType() == MeetingType.UNAVAILABLE) {
                availabilityDay.setIsCheckboxSelected(false);
            }

            eventsList.add(eventEntry);
        }
        for (var iterator : availabilitiesList) {
            if (iterator.getEvents().isEmpty()) {
                iterator.setIsCheckboxSelected(Boolean.FALSE);
            }
        }
        return availabilitiesList;
    }

    @NotNull
    private static List<AvailabilityDay> getDays() {
        final List<AvailabilityDay> availabilitiesList = new ArrayList<>();

        // Days of the week in the order: Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
        String[] translatedDaysOfWeek = {"Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota", "Niedziela"};

        // Create an empty list for each day of the week
        for (int day = 1; day <= 7; day++) {
            AvailabilityDay availabilityDay = new AvailabilityDay();
            List<EventEntry> eventsList = new ArrayList<>();
            String translatedDayName = translatedDaysOfWeek[day - 1];

            availabilityDay.setTranslatedName(translatedDayName);
            availabilityDay.setEvents(eventsList);

            availabilitiesList.add(availabilityDay);
        }
        return availabilitiesList;
    }


    public List<Date> getTutorDaysAvailable(User user) {
        List<LocalDateTime> tutorDaysAvailability = eventRepository.getTutorDaysAvailible(user.getId());
        return tutorDaysAvailability.stream().map(d -> Date.from(d.toInstant(ZoneOffset.UTC))).collect(Collectors.toList());
    }


    private DayOfWeek getDayOfWeekFromTranslatedName(String translatedName) {
        String[] translatedDaysOfWeek = {"Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota", "Niedziela"};
        for (int i = 0; i < translatedDaysOfWeek.length; i++) {
            if (translatedDaysOfWeek[i].equalsIgnoreCase(translatedName)) {
                return DayOfWeek.of(i + 1);
            }
        }
        return null; // Invalid translated day name
    }


    public Optional<Event> getDateForAParticularMeeting(Integer eventId, Integer tutorId) {
        return eventRepository.getDateForAParticularMeeting(tutorId, eventId);
    }
}
