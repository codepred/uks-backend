package codepred.service;

import codepred.common.util.ResponseObject;
import codepred.service.dto.ServiceReadDto;
import codepred.service.dto.UpdateServiceRequest;
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

@RestController
@RequestMapping(value = "/tutor/services/", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Api(value = "Tutor, part for services", tags = "Tutor-services")
@Tag(name = "Tutor-services", description = "Tutor-services API")
@CrossOrigin
public class ServiceControllerBean implements ServiceController {

    private final TutorService tutorService;
    private final ServiceService serviceService;

    @Override
    public List<ServiceReadDto> getServicesForClients(HttpServletRequest request) {
        var tutor = tutorService.getTutorByToken(request);
        return serviceService.gelAllServicesForTutor(tutor);

    }

    @Override
    public ResponseObject updateServiceForTutor(HttpServletRequest request, UpdateServiceRequest requestServiceForUpdateDTO) {
        var tutor = tutorService.getTutorByToken(request);
        var response = serviceService.updateService(tutor, requestServiceForUpdateDTO);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public ResponseObject removeService(HttpServletRequest request, Integer serviceId) {
        var tutor = tutorService.getTutorByToken(request);
        var response=serviceService.removeService(tutor, serviceId);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public ServiceReadDto getCertainService(HttpServletRequest request, Integer serviceId) {
        var tutor = tutorService.getTutorByToken(request);
        return serviceService.getCertainService(tutor, serviceId);
    }

    @Override
    public Integer countClientsConnectedWithService(HttpServletRequest request, Integer serviceId) {
        var tutor = tutorService.getTutorByToken(request);
        return serviceService.countClientsConnectedWithService(tutor, serviceId);
    }
}
