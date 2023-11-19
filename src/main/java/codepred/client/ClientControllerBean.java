package codepred.client;

import codepred.account.User;
import codepred.attendance.dto.AttendanceIdsDto;
import codepred.client.dto.*;
import codepred.common.util.ResponseObject;
import codepred.company.CompanyService;
import codepred.group.dto.GroupDto;
import codepred.group.dto.GroupIdAndNameDto;
import codepred.meeting.dto.*;
import codepred.note.dto.CreateNoteRequest;
import codepred.note.dto.NoteDto;
import codepred.payment.dto.PaymentForInvoiceDto;
import codepred.payment.dto.UpdatePaymentRequest;
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
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/tutor/clients/", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Api(value = "Tutor, part for clients", tags = "Tutor-clients")
@Tag(name = "Tutor-clients", description = "Tutor-client API")
@CrossOrigin
public class ClientControllerBean implements ClientController {

    public static final int PAGE_SIZE = 10;
    private final TutorService tutorService;
    private final ClientService clientService;
    private final CompanyService companyService;

    @Override
    public ClientFullDataDto getClientData(HttpServletRequest request, Integer id) {
        var tutor = tutorService.getTutorByToken(request);
        return clientService.getClientData(id, tutor);
    }

    @Override
    public ResponseObject createClient(HttpServletRequest request, @Valid NewClientRequest requestClientDTO) throws IOException {
        var tutor = tutorService.getTutorByToken(request);
        var response = clientService.createClient(requestClientDTO, tutor);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public List<ClientForTutorDto> getAllClientsForTutor(HttpServletRequest request, int pageNumber) {
        var tutor = tutorService.getTutorByToken(request);
        List<User> users = clientService.getClientsData(tutor, pageNumber, PAGE_SIZE);
        companyService.setCompanies(users);
        return clientService.getResponseAllClientForTutors(tutor, users);
    }

    @Override
    public Integer getPagesNumber(HttpServletRequest request) {
        var tutor = tutorService.getTutorByToken(request);
        return clientService.getNumberOfPagesForClients(tutor);
    }

    @Override
    public List<GroupIdAndNameDto> getGroupForClients(HttpServletRequest request) {
        var tutor = tutorService.getTutorByToken(request);
        return clientService.groupForClientToCreateClient(tutor);
    }

    @Override
    public List<ServiceDto> getServicesForClients(HttpServletRequest request) {
        var tutor = tutorService.getTutorByToken(request);
        return clientService.servicesForClient(tutor);
    }

    @Override
    public ResponseObject updateClientData(HttpServletRequest request, ClientUpdateRequest clientDTO) throws IOException {
        var tutor = tutorService.getTutorByToken(request);
        var response = clientService.updateClientData(tutor, clientDTO);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public ResponseObject updateClientGroups(HttpServletRequest request, ClientGroupDto clientGroupDTO) {
        var tutor = tutorService.getTutorByToken(request);
        var response = clientService.updateClientGroups(tutor, clientGroupDTO.getClientId(), clientGroupDTO.getGroupId());
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public ResponseObject updateClientService(HttpServletRequest request, ClientServiceDto clientServiceDTO) {
        var tutor = tutorService.getTutorByToken(request);
        var response = clientService.updateClientService(tutor, clientServiceDTO.getClientId(), clientServiceDTO.getServiceId());
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public ResponseObject createNote(HttpServletRequest request, CreateNoteRequest note) {
        var tutor = tutorService.getTutorByToken(request);
        var response = clientService.createNote(tutor, note.getClientId(), note.getContent());
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public List<NoteDto> getNotesConnectedToClient(HttpServletRequest request, Integer clientId) {
        var tutor = tutorService.getTutorByToken(request);
        return clientService.getNotesForClient(tutor, clientId);
    }

    @Override
    public ResponseObject removeNoteByClient(HttpServletRequest request, Integer clientId, Integer noteId) {
        var tutor = tutorService.getTutorByToken(request);
        var response = clientService.removeNote(tutor, clientId, noteId);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public List<ClientForTutorDto> findClientsByNameAndSurname(HttpServletRequest request, String content) {
        var tutor = tutorService.getTutorByToken(request);
        return clientService.findClientByNameAndSurname(tutor, content);
    }

    @Override
    public ResponseObject removeClient(HttpServletRequest request, Integer clientId) {
        var tutor = tutorService.getTutorByToken(request);
        var response = clientService.removeClients(tutor, clientId);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public List<ClientMeetingReadDto> getMeetings(HttpServletRequest request, ClientMeetingWithPagesDto clientMeetingDto) {
        var tutor = tutorService.getTutorByToken(request);
        return clientService.getMeetings(tutor, clientMeetingDto.getClientId(), clientMeetingDto.getStartTime(), clientMeetingDto.getEndTime(), clientMeetingDto.getPageNumber(), clientMeetingDto.getMeetingType());
    }

    @Override
    public MeetingsDto getTotalSumAndPageSize(HttpServletRequest request, ClientMeetingDto countPagesForMeetings) {
        var tutor = tutorService.getTutorByToken(request);
        return clientService.getTotalSumAndPageSize(tutor,
                countPagesForMeetings.getClientId(), countPagesForMeetings.getStartTime(), countPagesForMeetings.getEndTime(), countPagesForMeetings.getMeetingType()
        );
    }

    @Override
    public ResponseObject updatePaymentForEvent(HttpServletRequest request, UpdatePaymentRequest updatePaymentDto) {
        var tutor = tutorService.getTutorByToken(request);
        return clientService.updatePayment(updatePaymentDto.getAttendanceId(), updatePaymentDto.getValuePaid());
    }

    @Override
    public List<PaymentForInvoiceDto> getPaymentsForInvoice(HttpServletRequest request, AttendanceIdsDto attendancesId) {
        var tutor = tutorService.getTutorByToken(request);
        return clientService.getPaymentsForInvoice(attendancesId.getMeetingIds());
    }

    @Override
    public ResponseObject changeMeetingStatus(HttpServletRequest request, Integer attendanceId) {
        var tutor = tutorService.getTutorByToken(request);
        return clientService.changeMeetingStatus(tutor, attendanceId);
    }

}
