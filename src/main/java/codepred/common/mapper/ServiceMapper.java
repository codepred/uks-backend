package codepred.common.mapper;

import codepred.service.Service;
import codepred.service.dto.AddServiceRequest;
import codepred.service.dto.ServiceReadDto;
import codepred.service.dto.UpdateServiceRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ServiceMapper {
    Service AddServiceRequestToService(AddServiceRequest AddServiceRequest);

    List<ServiceReadDto> toRequestDto(List<Service> service);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Service fromUpdateDto(UpdateServiceRequest UpdateServiceRequest);

    ServiceReadDto toServiceReadDto(Service service);
}
