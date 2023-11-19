package codepred.service;

import codepred.account.User;
import codepred.common.mapper.ServiceMapper;
import codepred.common.util.ResponseObject;
import codepred.service.dto.ServiceReadDto;
import codepred.service.dto.UpdateServiceRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Slf4j
@Service
public class ServiceService {
    private final ServiceRepository serviceRepository;

    private final ServiceMapper serviceMapper;

    public List<ServiceReadDto> gelAllServicesForTutor(User user) {
        log.debug("ClientService ==> gelAllServicesForTutor() - start: Tutor = {}", user);
        var services = serviceRepository.findServiceByUserId(user.getId());
        var response = serviceMapper.toRequestDto(services);
        log.debug("ClientService ==> gelAllServicesForTutor() - end: response = {}", response);
        return response;
    }

    public ResponseObject updateService(User user, UpdateServiceRequest UpdateServiceRequest) {
        log.debug("ClientService ==> updateService() - start: Tutor = {}, service = {}", user, UpdateServiceRequest);
        var serviceFromDb = serviceRepository.findServiceByIdAndUser(UpdateServiceRequest.getId(), user).get();
        var serviceUpdated = serviceMapper.fromUpdateDto(UpdateServiceRequest);
        serviceUpdated.setId(serviceFromDb.getId());
        serviceRepository.save(serviceUpdated);
        var response = new ResponseObject(HttpStatus.ACCEPTED, "SERVICE_SUCCESSFULLY_UPDATED", null);
        log.debug("ClientService ==> updateService() - end: response {}", response);
        return response;
    }

    public ResponseObject removeService(User user, Integer serviceId) {
        log.debug("ClientService ==> removeService() - start: Tutor = {}, service = {}", user, serviceId);
        var clients = serviceRepository.countClientsByServiceId(serviceId);
        var serviceFromDb = serviceRepository.findServiceByIdAndUser(serviceId, user);
        ResponseObject response;
        if (serviceFromDb.isEmpty()) {
            response = new ResponseObject(HttpStatus.UNAUTHORIZED, "ACCESS_DENIED", null);
        } else if (clients > 1) {
            response = new ResponseObject(HttpStatus.UNPROCESSABLE_ENTITY, "CANNOT_BE_DELETED", null);
        } else {
            serviceFromDb.get().setIsDeleted(Boolean.TRUE);
            serviceRepository.save(serviceFromDb.get());
            response = new ResponseObject(HttpStatus.ACCEPTED, "SERVICE_SUCCESSFULLY_DELETED", null);
        }
        log.debug("ClientService ==> removeService() - end: response {}", response);
        return response;
    }

    public ServiceReadDto getCertainService(User user, Integer serviceId) {
        log.debug("ClientService ==> getCertainService() - start: Tutor = {}, service = {}", user, serviceId);
        var service = serviceRepository.findServiceByIdAndUser(serviceId, user);
        log.debug("ClientService ==> getCertainService() - end: service = {}",service);
        return serviceMapper.toServiceReadDto(service.get());
    }

    public Integer countClientsConnectedWithService(User user, Integer serviceId) {
        log.debug("ClientService ==> countClientsConnectedWithService() - start: Tutor = {}, service = {}", user, serviceId);
        var clients = serviceRepository.countClientsByServiceId(serviceId)-1;
        log.debug("ClientService ==> countClientsConnectedWithService() - end: clients = {}", clients);
        return Math.toIntExact(clients);
    }
}
