package codepred.customer;

import codepred.account.dto.SignupRequest;
import codepred.bookmark.BookMarkRepository;
import codepred.bookmark.dto.BookmarkDto;
import codepred.common.util.ResponseObject;
import codepred.config.files.FileService;
import codepred.meeting.Event;
import codepred.meeting.EventRepository;
import codepred.meeting.EventService;
import codepred.meeting.MeetingDate;
import codepred.meeting.dto.MeetingNewDateDto;
import codepred.payment.dto.ValueDto;
import codepred.tutor.TutorData;
import codepred.tutor.TutorRepository;
import codepred.tutor.TutorService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/customer/", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Api(value = "Customers, part for login, create account", tags = "Customers")
@Tag(name = "Customers", description = "Customers API")
@CrossOrigin
public class CustomerController {

    private final CustomerServiceBean customerServiceBean;
    private final TutorService tutorService;
    private final FileService fileService;
    private final BookMarkRepository bookMarkRepository;
    private final EventService eventService;
    private final TutorRepository tutorRepository;
    private final EventRepository eventRepository;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("[EEE MMM dd yyyy HH:mm:ss 'GMT'Z (z)]");

    @PostMapping("/signup")
    @Operation(summary = "Create customer", description = "Endpoint for create new password for customer. Also activate account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "EMAIL_SENT"),
        @ApiResponse(responseCode = "400", description = "EMAIL_DOES_NOT_EXIST"),
    })
    public ResponseObject signup(@RequestBody SignupRequest requestForSignup) {
        return customerServiceBean.signup(requestForSignup);
    }

    @GetMapping("/get-customer-data")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @Operation(summary = "Create customer", description = "Endpoint for create new password for customer. Also activate account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "EMAIL_SENT"),
        @ApiResponse(responseCode = "400", description = "EMAIL_DOES_NOT_EXIST"),
    })
    public ResponseEntity<CustomerData> getCustomerData(HttpServletRequest request) {
        final var client = tutorService.getTutorByToken(request);
        return ResponseEntity.ok(customerServiceBean.getClientData(client));
    }

    @GetMapping("/bookmark-list/{tutorId}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @Operation(summary = "Create customer", description = "Endpoint for create new password for customer. Also activate account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "EMAIL_SENT"),
        @ApiResponse(responseCode = "400", description = "EMAIL_DOES_NOT_EXIST"),
    })
    public ResponseEntity<List<BookmarkDto>> getBookmarkList(HttpServletRequest request, @PathVariable("tutorId") int tutorId) {
        final var client = tutorService.getTutorByToken(request);
        return ResponseEntity.ok(bookMarkRepository.findAllByUser(tutorId).stream().map(x -> {
            return new BookmarkDto(x.getId(), x.getDisplayedName(), x.getFileName());
        }).collect(Collectors.toList()));
    }

    @GetMapping("/bookmark-download/{bookMarkId}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @Operation(summary = "Create customer", description = "Endpoint for create new password for customer. Also activate account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "EMAIL_SENT"),
        @ApiResponse(responseCode = "400", description = "EMAIL_DOES_NOT_EXIST"),
    })
    public ResponseEntity<byte[]> downloadBookmark(HttpServletRequest request, @PathVariable("bookMarkId") int bookMarkId)
        throws IOException {
        final var user = tutorService.getTutorByToken(request);
        byte[] invoicePdf = fileService.download(bookMarkRepository.getById(bookMarkId).getFileName());
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=invoice.pdf")
            .header("Content-Type", "application/pdf")
            .body(invoicePdf);
    }

    @DeleteMapping("/remove-meeting/{tutorId}/meeting/{meetingId}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @Operation(summary = "Create customer", description = "Endpoint for create new password for customer. Also activate account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "EMAIL_SENT"),
        @ApiResponse(responseCode = "400", description = "EMAIL_DOES_NOT_EXIST"),
    })
    public ResponseObject deleteMeeting(HttpServletRequest request,
                                        @PathVariable("tutorId") Integer tutorId,
                                        @PathVariable("meetingId") Integer meetingId) {

        final var user = tutorService.getTutorByToken(request);
        var response = new ResponseObject();

        var tutor = tutorRepository.findById(tutorId);
        if(tutor.isEmpty()){
            response.setCode(HttpStatus.NOT_FOUND);
            response.setMessage("TUTOR_NOT_FOUND");
            return response;
        }

        response = eventService.removeEvent(meetingId, tutor.get(), false, user);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @GetMapping("/available-days/{tutorId}/meeting")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @Operation(summary = "Create customer", description = "Endpoint for create new password for customer. Also activate account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "EMAIL_SENT"),
        @ApiResponse(responseCode = "400", description = "EMAIL_DOES_NOT_EXIST"),
    })
    public Object getAvailableDays(HttpServletRequest request,
                                        @PathVariable("tutorId") Integer tutorId) {

        final var user = tutorService.getTutorByToken(request);

        var tutor = tutorRepository.findById(tutorId);
        if(tutor.isEmpty()){
            ResponseObject response = new ResponseObject();
            response.setCode(HttpStatus.NOT_FOUND);
            response.setMessage("TUTOR_NOT_FOUND");
            return response;
        }

        List<Date> list = eventService.getTutorDaysAvailable(tutor.get());
        list.add(new Date());
        List<String> responseDates = new ArrayList<>();
        for(Date date : list){
            TimeZone timeZone = TimeZone.getTimeZone("GMT+0200"); // Central European Summer Time
            dateFormat.setTimeZone(timeZone);

            // Format the Date object
            String formattedDate = dateFormat.format(date);
            responseDates.add(formattedDate);
        }

        return responseDates;
    }

    @GetMapping("/get-meeting-date/{tutorId}/meeting/{meetingId}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @Operation(summary = "Create customer", description = "Endpoint for create new password for customer. Also activate account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "EMAIL_SENT"),
        @ApiResponse(responseCode = "400", description = "EMAIL_DOES_NOT_EXIST"),
    })
    public String getDateForMeeting(HttpServletRequest request, @PathVariable("tutorId") Integer tutorId,
                                   @PathVariable("meetingId") Integer meetingId) {
        final var user = tutorService.getTutorByToken(request);
        Optional<Event> event = eventService.getDateForAParticularMeeting(meetingId, tutorId);
        TimeZone timeZone = TimeZone.getTimeZone("GMT+0200"); // Central European Summer Time
        dateFormat.setTimeZone(timeZone);

        // Format the Date object
        return formatLocalDateTimeToYYYYMMDD(event.get().getStartTime()) + " " + addLeadingZeroIfNecessary(event.get().getStartTime().getHour()) + ":" + addLeadingZeroIfNecessary(event.get().getStartTime().getMinute());
    }

    @PostMapping("/available-hours/{tutorId}/meeting/{meetingId}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @Operation(summary = "Create customer", description = "Endpoint for create new password for customer. Also activate account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "EMAIL_SENT"),
        @ApiResponse(responseCode = "400", description = "EMAIL_DOES_NOT_EXIST"),
    })
    public List<ValueDto> getHours(HttpServletRequest request, @PathVariable("tutorId") Integer tutorId,
                                   @PathVariable("meetingId") Integer meetingId, @RequestBody MeetingDate date) {
        final var user = tutorService.getTutorByToken(request);
        return customerServiceBean.getAvailableHours(tutorId , date.getDate(), meetingId);
    }

    @PutMapping("/postpone-meeting/{tutorId}/meeting/{meetingId}")
    @Operation(summary = "Create customer", description = "Endpoint for create new password for customer. Also activate account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "EMAIL_SENT"),
        @ApiResponse(responseCode = "400", description = "EMAIL_DOES_NOT_EXIST"),
    })
    public ResponseObject postponeMeeting(HttpServletRequest request, @PathVariable("tutorId") Integer tutorId,
                                          @PathVariable("meetingId") Integer meetingId, @RequestBody MeetingNewDateDto newDate) {
        final var client = tutorService.getTutorByToken(request);
        final var tutor = tutorRepository.getById(tutorId);
        Optional<Event> eventFromDb = eventRepository.getCertainEvent(tutorId, meetingId);
        if(eventFromDb.isPresent()){
            final var updatedEvent = eventService.updateEvent(eventFromDb, newDate);
            eventService.sendEmailForPostponedEvent(updatedEvent, client);
        }
        else {
            return new ResponseObject(HttpStatus.NOT_FOUND, "CLIENT_OR_SERVICE_DOES_NOT_EXIST", null);
        }
        return new ResponseObject(HttpStatus.ACCEPTED, "SERVICE_SUCCESSFULLY_UPDATED", null);
    }

    @GetMapping("/get-tutor-data/{tutorId}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @Operation(summary = "Create customer", description = "Endpoint for create new password for customer. Also activate account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "EMAIL_SENT"),
        @ApiResponse(responseCode = "400", description = "EMAIL_DOES_NOT_EXIST"),
    })
    public TutorData getTutorData(HttpServletRequest request, @PathVariable("tutorId") Integer tutorId){
        var client = tutorService.getTutorByToken(request);
        return customerServiceBean.getTutorData(client, tutorId);
    }

    public String formatLocalDateTimeToYYYYMMDD(LocalDateTime localDateTime) {
        // Define a DateTimeFormatter with the desired format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Format the LocalDateTime object using the formatter
        return localDateTime.format(formatter);
    }

    public String addLeadingZeroIfNecessary(int num) {
        String numStr = Integer.toString(num); // Convert the integer to a string
        if (numStr.length() == 1) {
            return "0" + numStr;
        } else {
            return numStr;
        }
    }

}
