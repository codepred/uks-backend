package codepred.group;

import codepred.attendance.dto.GroupAttendancesDto;
import codepred.attendance.dto.GroupAttendancesReadDto;
import codepred.common.util.ResponseObject;
import codepred.company.dto.CompanyGroupCreateRequest;
import codepred.group.dto.*;
import codepred.meeting.dto.MeetingsDto;
import codepred.note.dto.CreateNoteRequest;
import codepred.note.dto.NoteDto;
import codepred.service.dto.ServiceDto;
import codepred.tutor.TutorService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/tutor/groups/", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Api(value = "Tutor, part for groups", tags = "Tutor-groups")
@Tag(name = "Tutor-groups", description = "Tutor-groups API")
@CrossOrigin
public class GroupControllerBean implements GroupController {

    private final TutorService tutorService;
    private final GroupService groupService;

    @Override
    public ResponseObject createGroup(HttpServletRequest request, CreateGroupRequest CreateGroupRequest) {
        var tutor = tutorService.getTutorByToken(request);
        var response = groupService.createGroup(tutor, CreateGroupRequest);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public ResponseObject updateGroup(HttpServletRequest request, UpdateGroupRequest UpdateGroupRequest) {
        var tutor = tutorService.getTutorByToken(request);
        var response = groupService.updateGroup(tutor, UpdateGroupRequest);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public ResponseObject setCompanyToGroup(HttpServletRequest request, Integer groupId, Integer companyId)
        throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
        var tutor = tutorService.getTutorByToken(request);
        var response = groupService.setCompanyToGroup(tutor, groupId, companyId);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public ResponseObject setServiceToGroup(HttpServletRequest request, Integer groupId, Integer serviceId) {
        var tutor = tutorService.getTutorByToken(request);
        var response = groupService.setServiceToGroup(tutor, groupId, serviceId);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public List<ServiceDto> servicesForGroup(HttpServletRequest request, Integer groupId) {
        var tutor = tutorService.getTutorByToken(request);
        return groupService.servicesForGroup(tutor);
    }

    @Override
    public List<CompanyGroupCreateRequest> companiesForGroup(HttpServletRequest request, Integer groupId) {
        var tutor = tutorService.getTutorByToken(request);
        return groupService.companiesForGroup(tutor);
    }

    @Override
    public GroupDto getCertainGroup(HttpServletRequest request, Integer groupId) {
        var tutor = tutorService.getTutorByToken(request);
        return groupService.getCertainGroup(tutor, groupId);
    }

    @Override
    public ResponseObject createNote(HttpServletRequest request, CreateNoteRequest createNoteDTO) {
        var tutor = tutorService.getTutorByToken(request);
        var response = groupService.createNote(tutor, createNoteDTO.getClientId(), createNoteDTO.getContent());
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public List<NoteDto> getNotesForGroup(HttpServletRequest request, Integer groupId) {
        var tutor = tutorService.getTutorByToken(request);
        return groupService.getNotesForGroup(tutor, groupId);
    }

    @Override
    public ResponseObject removeNote(HttpServletRequest request, Integer groupId, Integer noteId) {
        var tutor = tutorService.getTutorByToken(request);
        var response = groupService.removeNote(tutor, groupId, noteId);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public List<GroupsDto> getAllGroups(HttpServletRequest request, int pageNumber) {
        var tutor = tutorService.getTutorByToken(request);
        var pageSize = 10;
        return groupService.getGroupData(tutor, pageNumber, pageSize);
    }

    @Override
    public Integer getNumberOfPagesForGroups(HttpServletRequest request) {
        var tutor = tutorService.getTutorByToken(request);
        return groupService.getNumberOfPagesForGroups(tutor);
    }

    @Override
    public List<GroupsDto> findGroupByName(HttpServletRequest request, String content) {
        var tutor = tutorService.getTutorByToken(request);
        return groupService.findGroupByName(tutor, content);
    }

    @Override
    public Integer countStudentInGroup(HttpServletRequest request, Integer groupId) {
        var tutor = tutorService.getTutorByToken(request);
        return groupService.getStudentsNumber(tutor, groupId);
    }

    @Override
    public List<GroupsDto> getAllGroups(HttpServletRequest request) {
        var tutor = tutorService.getTutorByToken(request);
        return groupService.getAllGroups(tutor);
    }

    @Override
    public List<ClientReadDto> getConnectedClients(HttpServletRequest request, Integer groupId) {
        var tutor = tutorService.getTutorByToken(request);
        return groupService.getConnectedClients(tutor, groupId);
    }

    @Override
    public List<GroupMeetingReadDto> getMeetings(HttpServletRequest request, GroupMeetingDto groupMeetingDto) {
        var tutor = tutorService.getTutorByToken(request);
        return groupService.getMeetings(tutor, groupMeetingDto.getGroupId(), groupMeetingDto.getPageNumber(), groupMeetingDto.getStartTime(), groupMeetingDto.getEndTime(), groupMeetingDto.getMeetingType());
    }

    @Override
    public MeetingsDto getTotalSumAndPageSize(HttpServletRequest request, GroupMeetingDto countPagesForMeetings) {
        var tutor = tutorService.getTutorByToken(request);
        return groupService.getTotalSumAndPageSize(tutor, countPagesForMeetings.getGroupId(), countPagesForMeetings.getStartTime(), countPagesForMeetings.getEndTime(), countPagesForMeetings.getMeetingType());
    }

    @Override
    public ResponseObject removeGroup(HttpServletRequest request, Integer groupId) {
        var tutor = tutorService.getTutorByToken(request);
        var response = groupService.removeGroup(tutor, groupId);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public List<GroupAttendancesReadDto> getClientForSetAttendance(HttpServletRequest request, Integer eventId) {
        var tutor = tutorService.getTutorByToken(request);
        return groupService.getClientForSetAttendance(tutor, eventId);
    }

    @Override
    public ResponseObject setAttendanceForEvent(HttpServletRequest request, GroupAttendancesDto clientsForAttendanceDto) {
        var tutor = tutorService.getTutorByToken(request);
        var response = groupService.setAttendanceForEvent(tutor, clientsForAttendanceDto);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }
}
