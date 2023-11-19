package codepred.group;

import codepred.attendance.dto.GroupAttendancesDto;
import codepred.attendance.dto.GroupAttendancesReadDto;
import codepred.common.util.ResponseObject;
import codepred.company.dto.CompanyGroupCreateRequest;
import codepred.group.dto.*;
import codepred.meeting.dto.ClientMeetingDto;
import codepred.meeting.dto.MeetingsDto;
import codepred.note.dto.CreateNoteRequest;
import codepred.note.dto.NoteDto;
import codepred.service.dto.ServiceDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface GroupController {

    @PostMapping("/create-group")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get all services for tutor", description = "Get services for tutors.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created."),
    })
    ResponseObject createGroup(HttpServletRequest request, @ModelAttribute CreateGroupRequest requestGroupForCreateDTO);

    @PostMapping("/update-group")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Update group", description = "Update a group.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accepted.")
    })
    ResponseObject updateGroup(HttpServletRequest request, @ModelAttribute UpdateGroupRequest requestGroupForUpdateDTO);

    @PostMapping("/set-company-to-group/{groupId}/{companyId}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Set company to group", description = "Set a company to a group.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accepted.")
    })
    ResponseObject setCompanyToGroup(HttpServletRequest request, @PathVariable("groupId") Integer groupId, @PathVariable("companyId") Integer companyId)
        throws InterruptedException;

    @PostMapping("/set-service-to-group/{groupId}/{serviceId}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Set service to group", description = "Set a service to a group.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accepted.")
    })
    ResponseObject setServiceToGroup(HttpServletRequest request, @PathVariable("groupId") Integer groupId, @PathVariable("serviceId") Integer serviceId);

    @GetMapping("/services-for-group/{groupId}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get services for group", description = "Get services for a group.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK.")
    })
    List<ServiceDto> servicesForGroup(HttpServletRequest request, @PathVariable("groupId") Integer groupId);

    @GetMapping("/companies-for-group/{groupId}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get companies for group", description = "Get companies for a group.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK.")
    })
    List<CompanyGroupCreateRequest> companiesForGroup(HttpServletRequest request, @PathVariable("groupId") Integer groupId);

    @GetMapping("/certain-group/{groupId}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get a certain group", description = "Get a specific group.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK.")
    })
    GroupDto getCertainGroup(HttpServletRequest request, @PathVariable("groupId") Integer groupId);

    @PostMapping("/create-note")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Create a note", description = "Create a note for a client in a group.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accepted.")
    })
    ResponseObject createNote(HttpServletRequest request, @RequestBody CreateNoteRequest createNoteDTO);

    @GetMapping("/notes-for-group/{groupId}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get notes for group", description = "Get notes for a group.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK.")
    })
    List<NoteDto> getNotesForGroup(HttpServletRequest request, @PathVariable("groupId") Integer groupId);

    @DeleteMapping("/remove-note/{groupId}/{noteId}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Remove a note", description = "Remove a note from a group.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accepted.")
    })
    ResponseObject removeNote(HttpServletRequest request, @PathVariable("groupId") Integer groupId, @PathVariable("noteId") Integer noteId);

    @GetMapping("/get-all-groups/{page-number}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get all groups", description = "Get groups with name of group, school and number of clients")
    @ApiResponse(responseCode = "200", description = "OK")
    List<GroupsDto> getAllGroups(HttpServletRequest request, @PathVariable("page-number") int pageNumber);

    @GetMapping("/get-number-of-pages")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get number of pages for groups", description = "Get number of pages for groups ")
    @ApiResponse(responseCode = "200", description = "OK")
    Integer getNumberOfPagesForGroups(HttpServletRequest request);

    @GetMapping("/find-group/{content}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Find group by name", description = "Find group by name")
    @ApiResponse(responseCode = "200", description = "OK")
    List<GroupsDto> findGroupByName(HttpServletRequest request, @PathVariable("content") String content);

    @GetMapping("/get-number-of-students/{id}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "count students", description = "Count students in the group")
    @ApiResponse(responseCode = "200", description = "OK")
    Integer countStudentInGroup(HttpServletRequest request, @PathVariable("id") Integer groupId);

    @GetMapping("/get-groups")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get all groups without pagination", description = "Get all groups")
    @ApiResponse(responseCode = "200", description = "OK")
    List<GroupsDto> getAllGroups(HttpServletRequest request);

    @GetMapping("/get-clients/{groupId}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get clients in group", description = "Get clients in group")
    @ApiResponse(responseCode = "200", description = "OK")
    List<ClientReadDto> getConnectedClients(HttpServletRequest request, @PathVariable("groupId") Integer groupId);

    @PostMapping("/meetings")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get meetings for group", description = "Get meetings for group")
    @ApiResponse(responseCode = "200", description = "Data loaded")
    List<GroupMeetingReadDto> getMeetings(HttpServletRequest request, @RequestBody GroupMeetingDto groupMeetingDto);

    @PostMapping("/meetings-count")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get meetings for group", description = "Get meetings for group")
    @ApiResponse(responseCode = "200", description = "Data loaded")
    MeetingsDto getTotalSumAndPageSize(HttpServletRequest request, @RequestBody GroupMeetingDto countPagesForMeetings);

    @DeleteMapping("/remove-client/{id}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Delete group", description = "Delete group")
    @ApiResponse(responseCode = "200", description = "GROUP_SUCCESSFULLY_REMOVED")
    @ApiResponse(responseCode = "400", description = "GROUP_NOT_FOUND")
    ResponseObject removeGroup(HttpServletRequest request, @PathVariable("id") Integer groupId);


    @GetMapping("/get-attendance/{id}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Delete group", description = "Delete group")
    @ApiResponse(responseCode = "200", description = "Data loaded")
    @ApiResponse(responseCode = "400", description = "EVENT_NOT_FOUND")
    List<GroupAttendancesReadDto> getClientForSetAttendance(HttpServletRequest request, @PathVariable("id") Integer eventId);

    @PutMapping("/update-attendance")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Update attendance", description = "Update attendance")
    @ApiResponse(responseCode = "200", description = "ATTENDANCES_UPDATED")
    ResponseObject setAttendanceForEvent(HttpServletRequest request, @RequestBody GroupAttendancesDto clientsForAttendanceDto);
}
