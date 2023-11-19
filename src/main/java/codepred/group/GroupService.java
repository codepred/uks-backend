package codepred.group;

import codepred.account.User;
import codepred.attendance.Attendance;
import codepred.attendance.AttendanceRepository;
import codepred.attendance.AttendanceStatus;
import codepred.attendance.dto.AttendanceDto;
import codepred.attendance.dto.GroupAttendancesDto;
import codepred.attendance.dto.GroupAttendancesReadDto;
import codepred.client.ClientRepository;
import codepred.client.ClientService;
import codepred.common.mapper.CompanyMapper;
import codepred.common.mapper.GroupMapper;
import codepred.common.mapper.NoteMapper;
import codepred.common.util.ResponseObject;
import codepred.company.CompanyRepository;
import codepred.company.dto.CompanyGroupCreateRequest;
import codepred.exception.CustomException;
import codepred.group.dto.*;
import codepred.meeting.Event;
import codepred.meeting.EventRepository;
import codepred.meeting.MeetingStatus;
import codepred.meeting.dto.MeetingsDto;
import codepred.note.Note;
import codepred.note.NoteRepository;
import codepred.note.dto.NoteDto;
import codepred.service.Service;
import codepred.service.ServiceRepository;
import codepred.service.dto.ServiceDto;
import codepred.tutor.TutorRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Slf4j
@Component
public class GroupService {
    private final GroupMapper groupMapper;
    private final GroupRepository groupRepository;
    private final ServiceRepository serviceRepository;
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final ClientService clientService;
    private final TutorRepository tutorRepository;
    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;
    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public ResponseObject createGroup(User tutor, CreateGroupRequest group) {
        log.debug("GroupService ==> createGroup() - start: user = {}, group = {}", tutor, group);

        User user = tutorRepository.findById(tutor.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        var groupToSave = groupMapper.fromCreateGroupDTO(group);
        Group groupId = groupRepository.saveAndFlush(groupToSave);
        user.getGroups().add(groupId);
        tutorRepository.save(user);
        var response = new ResponseObject(HttpStatus.ACCEPTED, groupId.getId().toString(), null);
        log.debug("GroupService ==> createGroup() - end: response = {}", response);
        return response;
    }

    @Transactional
    public ResponseObject updateGroup(User user, UpdateGroupRequest group) {
        var groupToUpdate = groupMapper.fromUpdateGroupDTO(group);
        var groupFromDB = groupRepository.findByIdAndTutor(group.getId(), user.getId()).orElseThrow();
        groupToUpdate.setId(groupFromDB.getId());
        if (group.getChangePhoto().equals(Boolean.FALSE)) {
            groupToUpdate.setGroupPhoto(groupFromDB.getGroupPhoto());
        }
        groupRepository.save(groupToUpdate);
        return new ResponseObject(HttpStatus.ACCEPTED, "GROUP_SUCCESSFULLY_SAVED", null);
    }

    public List<ServiceDto> servicesForGroup(User user) {
        log.debug("GroupService ==> groupsForClient() - start: Tutor = {}", user);
        var services = serviceRepository.findServiceByUserId(user.getId());
        List<ServiceDto> finalServices = new ArrayList<>();
        for (Service iterations : services) {
            var groupData = iterations.getServiceName();
            finalServices.add(new ServiceDto(iterations.getId(), groupData, iterations.getServiceDuration(), iterations.getPrice()));
        }
        log.debug("GroupService ==> groupsForClient() - end: services = {}", finalServices);
        return finalServices;
    }

    public List<CompanyGroupCreateRequest> companiesForGroup(User user) {
        log.debug("GroupService ==> companiesForGroup() - start: Tutor = {}", user);
        var companies = companyRepository.findCompaniesByUsers(user);
        var response = companyMapper.toCreateCompanyGroupDTO(companies);
        log.debug("GroupService ==> groupsForClient() - end: services = {}", response);
        return response;
    }

    public ResponseObject setCompanyToGroup(User user, Integer groupId, Integer companyId) {
        log.debug("GroupService ==> setCompanyToGroup() - start: user = {}, groupId = {}, companyId = {}", user, groupId, companyId);
        var group = groupRepository.findGroupByIdAndUser(groupId, user.getId());
        var company = companyRepository.findCompanyByIdAndUsers(companyId, user.getId());
        ResponseObject response;
        if (group.isPresent() && companyId == 0) {
            response = new ResponseObject(HttpStatus.ACCEPTED, "COMPANY_SUCCESSFULLY_SET_FOR_GROUP", null);
            group.get().setCompany(null);
            groupRepository.save(group.get());
        } else if (group.isEmpty() || company.isEmpty()) {
            response = new ResponseObject(HttpStatus.UNAUTHORIZED, "ACCESS_DENIED", null);
            log.debug("GroupService ==> setCompanyToGroup() - end: response = {}", response);
        } else {
            response = new ResponseObject(HttpStatus.ACCEPTED, "COMPANY_SUCCESSFULLY_SET_FOR_GROUP", null);
            group.get().setCompany(company.get());
            groupRepository.save(group.get());
            log.debug("GroupService ==> setCompanyToGroup() - end: response = {}", response);
        }
        return response;
    }

    @Transactional
    public ResponseObject setServiceToGroup(User user, Integer groupId, Integer serviceId) {
        log.debug("GroupService ==> setServiceToGroup() - start: user = {}, groupId = {}, serviceId = {}", user, groupId, serviceId);
        var group = groupRepository.findGroupByIdAndUser(groupId, user.getId());
        var service = serviceRepository.findServiceByIdAndUser(serviceId, user);
        ResponseObject response;
        if (group.isEmpty() || service.isEmpty()) {
            response = new ResponseObject(HttpStatus.UNAUTHORIZED, "ACCESS_DENIED", null);
            log.debug("GroupService ==> setServiceToGroup() - end: response = {}", response);
        } else {
            response = new ResponseObject(HttpStatus.ACCEPTED, "SERVICE_SUCCESSFULLY_SET_FOR_GROUP", null);
            group.get().setService(null);
            var updatedGroup = groupRepository.saveAndFlush(group.get());
            updatedGroup.setService(service.get());
            groupRepository.save(updatedGroup);
            log.debug("GroupService ==> setServiceToGroup() - end: response = {}", response);
        }
        return response;
    }

    public GroupDto getCertainGroup(User user, Integer groupId) {
        List<Object[]> groups = groupRepository.findCertainGroup(groupId, user.getId());

        if (groups.isEmpty()) {
            throw new CustomException("CLIENT_NOT_FOUND", HttpStatus.UNAUTHORIZED);
        }

        return mapToObject(groups.get(0));
    }

    @Transactional
    public ResponseObject createNote(User user, Integer groupId, String content) {
        log.debug("GroupService ==> createNote() - end: user = {}, groupId = {}, content = {}", user, groupId, content);
        var tutor = tutorRepository.findById(user.getId());
        var group = groupRepository.findGroupByIdAndUser(groupId, user.getId());
        if (tutor.isEmpty() || group.isEmpty()) {
            var response = new ResponseObject(HttpStatus.BAD_REQUEST, "GROUP_DOES_NOT_EXIST", null);
            log.debug("GroupService ==> createNote() - end: response = {}", response);
            return response;
        } else {
            var note = new Note();
            note.setContent(content);
            note.setGroups(group.get());
            noteRepository.save(note);
            tutor.get().getNotes().add(note);
            group.get().getNotes().add(note);
            tutorRepository.save(tutor.get());
            groupRepository.save(group.get());
            var response = new ResponseObject(HttpStatus.OK, "NOTE_CREATED", null);
            log.debug("GroupService ==> createNote() - end: response = {}", response);
            return response;
        }
    }

    @Transactional
    public List<NoteDto> getNotesForGroup(User user, Integer groupId) {
        log.debug("GroupService ==> getNotesForGroup() - start: user = {}, groupId = {}", user, groupId);
        var notes = noteRepository.findByGroupsAndUsers(groupId, user.getId());
        List<NoteDto> notesDTOs = noteMapper.fromNoteDto(notes);

        List<NoteDto> response = new ArrayList<>();
        String previousMonth = null;

        Map<String, String> monthMap = new HashMap<>();
        monthMap.put("stycznia", "styczeń");
        monthMap.put("lutego", "luty");
        monthMap.put("marca", "marzec");
        monthMap.put("kwietnia", "kwiecień");
        monthMap.put("maja", "maj");
        monthMap.put("czerwca", "czerwiec");
        monthMap.put("lipca", "lipiec");
        monthMap.put("sierpnia", "sierpień");
        monthMap.put("września", "wrzesień");
        monthMap.put("października", "październik");
        monthMap.put("listopada", "listopad");
        monthMap.put("grudnia", "grudzień");

        for (NoteDto note : notesDTOs) {
            String currentMonth = note.getCreatedAt().format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("pl", "PL")));

            String[] parts = currentMonth.split(" ");
            parts[0] = monthMap.getOrDefault(parts[0], parts[0]);
            currentMonth = String.join(" ", parts);

            if (!currentMonth.equals(previousMonth)) {
                NoteDto monthNote = new NoteDto();
                monthNote.setType("DATE");
                monthNote.setContent(currentMonth);

                response.add(monthNote);
                previousMonth = currentMonth;
            }
            if (note.getType() == null) {
                note.setType("NOTE");
            }
            response.add(note);
        }

        log.debug("GroupService ==> getNotesForGroup() - end: response = {}", response);
        return response;
    }

    @Transactional
    public ResponseObject removeNote(User user, Integer groupId, Integer noteId) {
        log.debug("GroupService ==> removeNote() - start: user = {}, groupId = {}, noteId = {}", user, groupId, noteId);
        var tutor = tutorRepository.findById(user.getId());
        var group = groupRepository.findGroupByIdAndUser(groupId, user.getId());
        if (tutor.isEmpty() || group.isEmpty()) {
            var response = new ResponseObject(HttpStatus.BAD_REQUEST, "NOTE_DOES_NOT_EXIST", null);
            log.debug("GroupService ==> removeNote() - end: response = {}", response);
            return response;
        } else {
            groupRepository.removeNotesTutorConnection(noteId);
            groupRepository.removeNote(noteId);
            var response = new ResponseObject(HttpStatus.OK, "NOTE_SUCCESSFULLY_REMOVED", null);
            log.debug("GroupService ==> removeNote() - end: response = {}", response);
            return response;
        }
    }

    public Integer getNumberOfPagesForGroups(User user) {
        log.debug("GroupService ==> getNumberOfPagesForGroups() - start: Tutor = {}", user);
        long clientCount = groupRepository.countGroupByTutor(user.getId());
        int totalPages = (int) Math.ceil((double) clientCount / 10);
        log.debug("GroupService ==> getNumberOfPagesForGroups() - start: totalNumber = {}", totalPages);
        return totalPages;
    }

    public List<GroupsDto> getGroupData(User user, int pageNumber, int pageSize) {
        log.debug("GroupService ==> getClientsData() - start: Tutor = {}, pageNumber = {}, pageSize = {}", user, pageNumber, pageSize);
        var records = groupRepository.getAllGroupConnectedWithTutor(user.getId(), PageRequest.of(pageNumber - 1, pageSize));
        List<GroupsDto> result = new ArrayList<>();
        for (Object[] record : records) {
            GroupsDto group = new GroupsDto();
            group.setId((Integer) record[0]);
            group.setGroupname((String) record[1]);
            group.setStudentsnumber(((BigInteger) record[2]).intValue());
            group.setCompanyname((String) record[3]);
            group.setGroupPhoto((byte[]) record[4]);
            result.add(group);
        }
        log.debug("GroupService ==> getClientsData() - end: response = {}", result);
        return result;
    }

    public List<GroupsDto> getAllGroups(User user) {
        log.debug("GroupService ==> getAllGroups() - start: user = {}", user);
        final var groupConnectedWithTutor = groupRepository.getAllGroupConnectedWithTutor(user.getId());
        final var groupConnectedWithCompany = groupRepository.getAllGroupConnectedWithCompany(user.getId());
        List<GroupsDto> result = new ArrayList<>();
        for (Group group : Stream.concat(groupConnectedWithTutor.stream(), groupConnectedWithCompany.stream())
            .collect(Collectors.toList())) {
            GroupsDto responseGroup = new GroupsDto();
            responseGroup.setId(group.getId());
            responseGroup.setGroupname(group.getGroupName());
            responseGroup.setStudentsnumber(group.getUser().size() - 1);
            if(group.getCompany() != null) {
                responseGroup.setCompanyname(group.getCompany().getName());
            }
            result.add(responseGroup);
        }
        log.debug("GroupService ==> getAllGroups() - end: response = {}", result);
        return result;
    }

    public List<GroupsDto> findGroupByName(User user, String content) {
        log.debug("GroupService ==> findGroupByName() - start: Tutor = {}, content = {}", user, content);
        var records = groupRepository.findGroupByNameAndTutor(content, user.getId());
        List<GroupsDto> result = new ArrayList<>();
        for (Object[] record : records) {
            GroupsDto group = new GroupsDto();
            group.setId((Integer) record[0]);
            group.setGroupname((String) record[1]);
            group.setStudentsnumber(((BigInteger) record[2]).intValue());
            group.setCompanyname((String) record[3]);
            result.add(group);
        }
        log.debug("GroupService ==> findGroupByName() - end: response = {}", result);
        return result;
    }

    public Integer getStudentsNumber(User user, Integer groupId) {
        log.debug("GroupService ==> getStudentsNumber() - start: Tutor = {}, groupId = {}", user, groupId);
        var studentsNumber = groupRepository.countStudentInGroup(user.getId(), groupId);
        log.debug("GroupService ==> getStudentsNumber() - end: students number = {}", studentsNumber);
        return studentsNumber;
    }

    public List<ClientReadDto> getConnectedClients(User tutor, Integer groupId) {
        var studentsFromDb = groupRepository.getConnectedClients(tutor.getId(), groupId);
        var response = new ArrayList<ClientReadDto>();
        for (Object[] resultRow : studentsFromDb) {
            Integer clientId = (Integer) resultRow[0];
            Optional<User> client = clientRepository.findUserById(clientId);
            if(client.isEmpty()) continue;
            var clientDetailsForGivenTutor = clientService.findClientDetailsForGivenTutor(client.get(), tutor);
            response.add(new ClientReadDto(clientId, clientDetailsForGivenTutor.getName(), clientDetailsForGivenTutor.getLastname()));
        }
        return response;
    }

    @Transactional
    public List<GroupMeetingReadDto> getMeetings(User user, Integer groupId, Integer pageNumber, LocalDateTime startTime, LocalDateTime endTime, String meetingType) {
        int adjustedPageNumber = pageNumber - 1;
        Pageable pageable = PageRequest.of(adjustedPageNumber, 10);
        var events = eventRepository.findMeetingsByGroupIdAndTimeRange(groupId,startTime,endTime, pageable);
        var response = new ArrayList<GroupMeetingReadDto>();

        for (Event event : events) {
            MeetingStatus status = null;
            var presentStudents = 0;
            var studentCount = 0;

            // Create a list to collect modified attendances
            List<Attendance> modifiedAttendances = new ArrayList<>();

            for (Attendance attendance : event.getAttendances()) {
                String filter = meetingType.equals("individual") ? "STUDENT" : meetingType.equals("all") ? "" : "CLIENT_IN_GROUP";
                if(!filter.equals("")) {
                    if (!attendance.getClientType().equals(filter)) continue;
                }
                if (LocalDateTime.now().isAfter(event.getStartTime()) && attendance.getMeetingStatus() == MeetingStatus.planned) {
                    attendance.setMeetingStatus(MeetingStatus.finished);
                    modifiedAttendances.add(attendance);
                }
                if (attendance.getAttendanceStatus().equals(AttendanceStatus.PRESENT)) {
                    presentStudents++;
                }
                status = attendance.getMeetingStatus();
                studentCount++;
            }

            attendanceRepository.saveAll(modifiedAttendances);

            response.add(
                new GroupMeetingReadDto(
                    event.getId(), event.getStartTime(), status, event.getService().getPrice(), presentStudents, studentCount)
            );
        }
        return response;
    }

    @Transactional
    public MeetingsDto getTotalSumAndPageSize(User tutor, Integer groupId, LocalDateTime startTime, LocalDateTime endTime, String meetingType) {

        List<Attendance> totalAttendances = attendanceRepository.findPresentUserAttendanceByGroupIdAndTimeRange(groupId,startTime,endTime);
        String filter = meetingType.equals("individual") ? "STUDENT" : meetingType.equals("all") ? "" : "CLIENT_IN_GROUP";
        if(!filter.equals("")) {
            totalAttendances = totalAttendances.stream().filter(attendance -> attendance.getClientType().equals(filter)).collect(Collectors.toList());
        }
        Float totalSum = 0f;
        Float valuePaid = 0f;
        int eventCount = eventRepository.countGroupEvents(groupId,startTime,endTime);

        for (Attendance attendance: totalAttendances) {
            Event event = eventRepository.getGroupEvents(tutor.getId(), attendance.getEvent().getId(),groupId)
                    .orElseThrow(() -> new CustomException("EVENT_NOT_FOUND", HttpStatus.BAD_REQUEST));
            totalSum += event.getService().getPrice().floatValue();
            if(attendance.getValuePaid() != null){
                valuePaid += attendance.getValuePaid();
            }
        }

        int numberOfPages = (int) Math.ceil((double) eventCount/ 10);

        return MeetingsDto.builder()
                .numberOfPages(String.valueOf(numberOfPages))
                .summedValue(totalSum.toString())
                .paidValue(valuePaid.toString())
                .meetingIds(totalAttendances.stream().map(a -> a.getId()).collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public ResponseObject removeGroup(User user, Integer groupId) {
        var group = groupRepository.findGroupByIdAndTutor(groupId, user.getId()).orElseThrow(() -> new CustomException("GROUP_NOT_FOUND", HttpStatus.BAD_REQUEST));
        groupRepository.removeStudentsForGroup(group.getId());
        groupRepository.removeNotesForGroup(group.getId());
        groupRepository.removeAttendancesForGroup(group.getId());
        groupRepository.removeEventsForGroup(group.getId());
        return new ResponseObject(HttpStatus.OK, "GROUP_SUCCESSFULLY_REMOVED", null);
    }

    public List<GroupAttendancesReadDto> getClientForSetAttendance(User tutor, Integer eventId) {
        var event = eventRepository.getCertainEvent(tutor.getId(), eventId)
                .orElseThrow(() -> new CustomException("EVENT_NOT_FOUND", HttpStatus.BAD_REQUEST));
        var attendances = attendanceRepository.findAttendancesByEvent(event);
        var response = new ArrayList<GroupAttendancesReadDto>();
        for (Attendance attendance : attendances) {
            var client = clientRepository.findUserById(attendance.getClientId())
                    .orElseThrow(() -> new CustomException("CLIENT_NOT_FOUND", HttpStatus.BAD_REQUEST));
            var isPresent = Boolean.FALSE;
            if (attendance.getAttendanceStatus().equals(AttendanceStatus.PRESENT))
                isPresent = Boolean.TRUE;
            var clientDetailsForGivenTutor = clientService.findClientDetailsForGivenTutor(client, tutor);
            response.add(
                    new GroupAttendancesReadDto(
                            attendance.getId(),
                            clientDetailsForGivenTutor.getName() + " " + clientDetailsForGivenTutor.getLastname(),
                            isPresent));
        }
        return response;
    }

    @Transactional
    public ResponseObject setAttendanceForEvent(User user, GroupAttendancesDto clientsForAttendanceDto) {
        for (AttendanceDto i : clientsForAttendanceDto.getAttendanceDto()) {
            var attendance = attendanceRepository.findById(i.getAttendanceId())
                    .orElseThrow(() -> new CustomException("ATTENDANCE_NOT_FOUND", HttpStatus.BAD_REQUEST));
            if (i.getIsPresent().equals(Boolean.TRUE))
                attendance.setAttendanceStatus(AttendanceStatus.PRESENT);
            else
                attendance.setAttendanceStatus((AttendanceStatus.ABSENT));
            attendanceRepository.save(attendance);
        }

        return new ResponseObject(HttpStatus.OK, "ATTENDANCES_UPDATED", null);
    }

    private GroupDto mapToObject(Object[] result) {
        Long groupIdValue = (result[0] != null) ? ((Number) result[0]).longValue() : null;
        String color = (result[1] != null) ? (String) result[1] : null;
        String linkToMeeting = (result[2] != null) ? (String) result[2] : null;
        String folderLink = (result[3] != null) ? (String) result[3] : null;
        byte[] groupPhoto = (result[4] != null) ? (byte[]) result[4] : null;
        String additionalInformation = (result[5] != null) ? (String) result[5] : null;
        Long companyId = (result[6] != null) ? ((Number) result[6]).longValue() : null;
        String companyName = (result[7] != null) ? (String) result[7] : null;
        Long serviceId = (result[8] != null) ? ((Number) result[8]).longValue() : null;
        Double price = (result[9] != null) ? (Double) result[9] : null;
        String serviceName = (result[10] != null) ? (String) result[10] : null;
        Integer serviceDuration = (result[11] != null) ? ((Number) result[11]).intValue() : null;
        String timeUnit = (result[12] != null) ? (String) result[12] : null;
        String groupName = (result[13] != null) ? (String) result[13] : null;
        return new GroupDto(groupIdValue, groupName, color, linkToMeeting, folderLink, groupPhoto, additionalInformation,
                companyId, companyName, serviceId, price, serviceName, serviceDuration, timeUnit);
    }
}
