package codepred.meeting;

import codepred.calendar.availability.AvailabilityDay;
import codepred.calendar.availability.AvailabilityForCalendarReadDto;
import codepred.calendar.get.ClientsDataDto;
import codepred.calendar.get.EventDto;
import codepred.calendar.get.EventFullDataDto;
import codepred.calendar.remove.RemoveEventRequest;
import codepred.calendar.save.GroupAndEventRequest;
import codepred.calendar.save.NewEventRequest;
import codepred.calendar.save.ServiceAndEventRequest;
import codepred.calendar.update.UpdateEventDto;
import codepred.common.util.ResponseObject;
import codepred.service.dto.ServiceReadDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventController {

    @GetMapping("/event-get-clients")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Endpoint to get clients and groups data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Something went wrong"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    List<ClientsDataDto> getClientDataToEventAdd(HttpServletRequest request);

    @GetMapping("/event-get-data")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Endpoint to get events data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Something went wrong"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    List<EventDto> getEventsData(HttpServletRequest request);

    @GetMapping("/event-get-services")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Endpoint to get events data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Something went wrong"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    List<ServiceReadDto> getServicesData(HttpServletRequest request);

    @PostMapping("/event-create")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Endpoint to save event data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event successfully saved"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    ResponseObject saveEventData(HttpServletRequest request, @RequestBody NewEventRequest newEvent);

    @PostMapping("/event-set-service")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Add service to event", description = "Add a service to an event for a tutor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service successfully added to event", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request: Event or service with this ID not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseObject addServiceToEvent(HttpServletRequest request, @RequestBody ServiceAndEventRequest requestServiceAndEventDTO);


    @PutMapping("/event-set-client/{eventId}/{clientId}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Add client to event", description = "Adds a client to an existing event.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client successfully added to event."),
            @ApiResponse(responseCode = "400", description = "Event or client with this ID not found.")
    })
    ResponseObject addClientToEvent(HttpServletRequest request, @PathVariable("eventId") Integer eventId, @PathVariable("clientId") Integer clientId);

    @PostMapping("/event-set-group")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Add group to event", description = "Adds a group to an existing event.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group successfully added to event."),
            @ApiResponse(responseCode = "400", description = "Event or group with this ID not found.")
    })
    ResponseObject assignGroupToEvent(HttpServletRequest request, @RequestBody GroupAndEventRequest requestGroupAndEventDTO);

    @PatchMapping("/event-remove")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Remove event", description = "Removing event by Id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Event successfully removed."),
            @ApiResponse(responseCode = "401", description = "You have no rights to modify this event.")
    })
    ResponseObject removeEvent(HttpServletRequest request, @RequestBody RemoveEventRequest removeEventDTO);

    @PutMapping("/event-update")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Update event", description = "Update event by Id.")
    @ApiResponses(value={
            @ApiResponse(responseCode = "202", description = "Event successfully updated."),
            @ApiResponse(responseCode = "404", description = "This time is busy"),
    })
    ResponseObject updateEvent(HttpServletRequest request, @RequestBody UpdateEventDto updateEventDTO);

    @GetMapping("/get-certain-event/{id}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get certain event data", description = "Get certain event data.")
    @ApiResponses(value={
            @ApiResponse(responseCode = "200", description = "Data loaded."),
    })
    EventFullDataDto getCertainEventData(HttpServletRequest request, @PathVariable("id") Integer eventId);

    @PutMapping ("/set-availabilities")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Set availabilities", description = "Set availabilities for tutor.")
    @ApiResponses(value={
            @ApiResponse(responseCode = "200", description = "AVAILABILITIES_SUCCESSFULLY_SET"),
    })
    ResponseObject setAvailabilitiesForTutor(HttpServletRequest request, @RequestBody List<AvailabilityDay> availabilities);

    @GetMapping("/get-availabilities")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get availabilities", description = "Get availabilities for tutor.")
    @ApiResponses(value={
            @ApiResponse(responseCode = "200", description = "AVAILABILITIES_SUCCESSFULLY_Get"),
    })
    List<AvailabilityForCalendarReadDto> getAvailabilitiesForTutor(HttpServletRequest request);

    @GetMapping("/get-availabilities-for-settings")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get availabilities for settings", description = "Get availabilities for settings tutor.")
    @ApiResponses(value={
            @ApiResponse(responseCode = "200", description = "AVAILABILITIES_SUCCESSFULLY_GET"),
    })
    List<AvailabilityDay> getAvailabilitiesForSettings(HttpServletRequest request);
}
