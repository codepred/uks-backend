package codepred.meeting;

import codepred.calendar.availability.AvailabilityForCalendarReadDto;
import codepred.calendar.availability.AvailabilityDay;
import codepred.calendar.get.EventDto;
import codepred.calendar.get.ClientsDataDto;
import codepred.calendar.get.EventFullDataDto;
import codepred.calendar.remove.RemoveEventRequest;
import codepred.calendar.save.NewEventRequest;
import codepred.calendar.save.GroupAndEventRequest;
import codepred.calendar.save.ServiceAndEventRequest;
import codepred.calendar.update.UpdateEventDto;
import codepred.common.mapper.EventMapper;
import codepred.common.mapper.ServiceMapper;
import codepred.common.util.ResponseObject;
import codepred.service.dto.ServiceReadDto;
import codepred.tutor.TutorService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping(value = "/tutor/events/", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Api(value = "Tutor, part for events", tags = "Tutor-events")
@Tag(name = "Tutor-events", description = "Tutor-events API")
@CrossOrigin
public class EventControllerBean implements EventController {
    private final TutorService tutorService;
    private final EventService eventService;
    private final ServiceMapper serviceMapper;
    private final EventMapper eventMapper;

    @Override
    public List<ClientsDataDto> getClientDataToEventAdd(HttpServletRequest request) {
        var tutor = tutorService.getTutorByToken(request);
        return eventService.getClientDataToEvent(tutor.getId());
    }

    @Override
    public List<EventDto> getEventsData(HttpServletRequest request) {
        var tutor = tutorService.getTutorByToken(request);
        return eventService.getEventsData(tutor.getId());
    }

    @Override
    public List<ServiceReadDto> getServicesData(HttpServletRequest request) {
        var tutor = tutorService.getTutorByToken(request);
        var service = eventService.getServicesData(tutor.getId());
        return serviceMapper.toRequestDto(service);
    }

    @Override
    public ResponseObject saveEventData(HttpServletRequest request, NewEventRequest newEvent) {
        var tutor = tutorService.getTutorByToken(request);
        var event = eventService.saveEvent(
                newEvent,
                tutor
        );
        event.setToken(request.getHeader("Authorization").substring(7));
        return event;
    }

    @Override
    public ResponseObject addServiceToEvent(HttpServletRequest request, ServiceAndEventRequest ServiceAndEventRequest) {
        var tutor = tutorService.getTutorByToken(request);
        var event = eventService.addServiceToEvent(tutor,
                ServiceAndEventRequest.getEventId(),
                ServiceAndEventRequest.getServiceId()
        );
        event.setToken(request.getHeader("Authorization").substring(7));
        return event;
    }

    @Override
    public ResponseObject addClientToEvent(HttpServletRequest request, Integer eventId, Integer clientId) {
        var tutor = tutorService.getTutorByToken(request);
        var event = eventService.addClientToEvent(tutor, eventId, clientId);
        event.setToken(request.getHeader("Authorization").substring(7));
        return event;
    }

    @Override
    public ResponseObject assignGroupToEvent(HttpServletRequest request, GroupAndEventRequest GroupAndEventRequest) {
        var tutor = tutorService.getTutorByToken(request);
        var event = eventService.assignGroupToEvent(tutor, GroupAndEventRequest.getEventId(), GroupAndEventRequest.getGroupId());
        event.setToken(request.getHeader("Authorization").substring(7));
        return event;
    }

    @Override
    @Transactional
    public ResponseObject removeEvent(HttpServletRequest request, RemoveEventRequest removeEventRequest) {
        var tutor = tutorService.getTutorByToken(request);
        var response = eventService.removeEvent(Integer.parseInt(removeEventRequest.getEventId()), tutor, true, tutor);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public ResponseObject updateEvent(HttpServletRequest request, UpdateEventDto updateEventDTO) {
        var tutor = tutorService.getTutorByToken(request);
        var eventToUpdate = eventMapper.fromUpdateEventDto(updateEventDTO);
        eventToUpdate.setTitle(updateEventDTO.getTitle());
        try {
            var response = eventService.updateEventData(tutor, eventToUpdate);
            response.setToken(request.getHeader("Authorization").substring(7));
            return response;
        } catch (Exception e){

        }
        return null;
    }


    @Override
    public EventFullDataDto getCertainEventData(HttpServletRequest request, Integer eventId) {
        var tutor = tutorService.getTutorByToken(request);
        return eventService.getCertainEvent(tutor, eventId);
    }

    @Override
    public ResponseObject setAvailabilitiesForTutor(HttpServletRequest request, List<AvailabilityDay> availabilities) {
        var tutor=tutorService.getTutorByToken(request);
        var response=eventService.setAvailabilityForTutor(tutor, availabilities);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public List<AvailabilityForCalendarReadDto> getAvailabilitiesForTutor(HttpServletRequest request) {
        var tutor=tutorService.getTutorByToken(request);
        return eventService.getAvailabilitiesForCalendar(tutor);
    }

    @Override
    public List<AvailabilityDay> getAvailabilitiesForSettings(HttpServletRequest request) {
        var tutor=tutorService.getTutorByToken(request);
        return eventService.getAvailabilitiesForSettings(tutor);
    }
}
