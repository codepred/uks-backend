package codepred.client;

import codepred.attendance.dto.AttendanceIdsDto;
import codepred.client.dto.*;
import codepred.common.util.ResponseObject;
import codepred.group.dto.GroupDto;
import codepred.group.dto.GroupIdAndNameDto;
import codepred.meeting.dto.*;
import codepred.note.dto.CreateNoteRequest;
import codepred.note.dto.NoteDto;
import codepred.payment.dto.PaymentForInvoiceDto;
import codepred.payment.dto.UpdatePaymentRequest;
import codepred.service.dto.ServiceDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

public interface ClientController {

    @GetMapping("/client-get/{id}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Update event", description = "Update event by event Id and group id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Event successfully updated."),
            @ApiResponse(responseCode = "404", description = "Group doesn't exist"),
    })
    ClientFullDataDto getClientData(HttpServletRequest request, @PathVariable("id") Integer id);

    @PostMapping("/create-client")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Create client", description = "Create client and connect with tutor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Client successfully created."),
            @ApiResponse(responseCode = "404", description = "Client already exist"),
    })
    ResponseObject createClient(HttpServletRequest request, @ModelAttribute @Valid NewClientRequest requestClientDTO) throws IOException;

    @GetMapping("/clients-get-all/{page-number}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get all clients", description = "Get certain page of clients.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Clients list."),
    })
    List<ClientForTutorDto> getAllClientsForTutor(HttpServletRequest request, @PathVariable("page-number") int pageNumber);

    @GetMapping("/get-pages-number")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get number of pages", description = "Get number of pages, max 10 elements.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Number of pages."),
    })
    Integer getPagesNumber(HttpServletRequest request);

    @GetMapping("/get-all-groups")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get all groups for clients", description = "Get groups for clients.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Number of pages."),
    })
    List<GroupIdAndNameDto> getGroupForClients(HttpServletRequest request);

    @GetMapping("/get-all-services")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get all groups for clients", description = "Get groups for clients.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Number of pages."),
    })
    List<ServiceDto> getServicesForClients(HttpServletRequest request);

    @PutMapping("/update-client")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Update client data", description = "Update clients data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data successfully updated")
    })
    ResponseObject updateClientData(HttpServletRequest request, @ModelAttribute ClientUpdateRequest clientDTO) throws IOException;

    @PutMapping("/client-groups")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Update client data", description = "Update clients group connection data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data successfully updated"),
            @ApiResponse(responseCode = "404", description = "Client or group does not exist")
    })
    ResponseObject updateClientGroups(HttpServletRequest request, @RequestBody ClientGroupDto clientGroupDTO);

    @PutMapping("/client-service")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Update client data", description = "Update clients service connection data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data successfully updated"),
            @ApiResponse(responseCode = "404", description = "Client or service does not exist")
    })
    ResponseObject updateClientService(HttpServletRequest request, @RequestBody ClientServiceDto clientServiceDTO);


    @PostMapping("/note-client")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Create note", description = "Create note to client")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data successfully created")
    })
    ResponseObject createNote(HttpServletRequest request, @RequestBody CreateNoteRequest note);

    @GetMapping("/note-client/{id}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get notes", description = "Get all notes connected to certain client")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data successfully created")
    })
    List<NoteDto> getNotesConnectedToClient(HttpServletRequest request, @PathVariable("id") Integer clientId);

    @DeleteMapping("/note-client/client/{clientId}/note/{noteId}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Remove note", description = "Remove note by client and note id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note successfully removed")
    })
    ResponseObject removeNoteByClient(HttpServletRequest request, @PathVariable("clientId") Integer clientId, @PathVariable("noteId") Integer noteId);

    @GetMapping("/find-client/{content}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Find client", description = "Find client by name and surname")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of clients")
    })
    List<ClientForTutorDto> findClientsByNameAndSurname(HttpServletRequest request, @PathVariable("content") String content);

    @DeleteMapping("/remove-client/{id}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Remove client", description = "Remove client by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client successfully removed"),
            @ApiResponse(responseCode = "400", description = "Client not found")
    })
    ResponseObject removeClient(HttpServletRequest request, @PathVariable("id") Integer clientId);

    @PostMapping("/meetings")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get meetings for certain clients", description = "Get meetings for certain clients")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data loaded"),
            @ApiResponse(responseCode = "400", description = "Event not found")
    })
    List<ClientMeetingReadDto> getMeetings(HttpServletRequest request, @RequestBody ClientMeetingWithPagesDto clientMeetingDto);

    @PostMapping("/meetings-count")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get total invoice sum and number of pages for meetings", description = "Get total invoice sum and number of pages for meetings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data loaded")
    })
    MeetingsDto getTotalSumAndPageSize(HttpServletRequest request, @RequestBody ClientMeetingDto countPagesForMeetings);

    @PutMapping("/meeting-payment")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Update payment for event", description = "Use attendance id plz")
    ResponseObject updatePaymentForEvent(HttpServletRequest request, @RequestBody UpdatePaymentRequest updatePaymentDto);

    @PostMapping("/get-payments-data")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get data for generate invoice")
    List<PaymentForInvoiceDto> getPaymentsForInvoice(HttpServletRequest request, @RequestBody AttendanceIdsDto attendancesListId);

    @PutMapping("/change-meeting-status/{attendanceId}")
    ResponseObject changeMeetingStatus(HttpServletRequest request, @PathVariable("attendanceId") Integer attendanceId);
}
