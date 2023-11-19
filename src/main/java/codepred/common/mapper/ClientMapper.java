package codepred.common.mapper;

import codepred.account.User;
import codepred.client.ClientDetails;
import codepred.client.dto.ClientForTutorDto;
import codepred.client.dto.NewClientRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientDetails fromDtoWithPhoto(NewClientRequest newClientRequest);

//    @Mapping(source = "id", target = "id")
//    @Mapping(source = "email", target = "email")
//    @Mapping(source = "clientDetails.name", target = "name")
//    @Mapping(source = "clientDetails.lastname", target = "lastname")
//    @Mapping(source = "clientDetails.clientPhoto", target = "clientPhoto")
//    @Mapping(source = "companies.name", target = "company")
//    List<ClientForTutorDto> fromClientToResponseAllClientForTutor(List<User> clients);

//    @Mapping(source = "clientDetails.name", target = "name")
//    @Mapping(source = "clientDetails.lastname", target = "lastname")
//    @Mapping(source = "clientDetails.clientPhoto", target = "clientPhoto")
//    ClientForTutorDto fromClientToResponseAllClientForTutor(User clients);

}
