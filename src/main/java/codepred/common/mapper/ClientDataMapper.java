package codepred.common.mapper;

import codepred.account.User;
import codepred.client.ClientDetails;
import codepred.client.dto.ClientDefaultGroupDto;
import codepred.client.dto.ClientDefaultServiceDto;
import codepred.client.dto.ClientFullDataDto;
import codepred.group.Group;
import codepred.service.Service;

public class ClientDataMapper {

    public static ClientDefaultGroupDto mapGroupToDTO(Group groupWithTutor) {
        return ClientDefaultGroupDto.builder()
                .groupId(groupWithTutor.getId())
                .groupName(groupWithTutor.getGroupName())
                .build();
    }
    public static ClientDefaultServiceDto mapServiceToDto(Service serviceWithTutor) {
        return ClientDefaultServiceDto.builder()
                .serviceId(serviceWithTutor.getId())
                .serviceName(serviceWithTutor.getServiceName())
                .serviceDuration(serviceWithTutor.getServiceDuration())
                .timeUnit(serviceWithTutor.getTimeUnit())
                .price(serviceWithTutor.getPrice())
                .build();
    }
    public static ClientFullDataDto mapClientToDTO(User client, ClientDetails clientDetailsForGivenTutor) {
        return ClientFullDataDto.builder()
                .id(client.getId())
                .photo(clientDetailsForGivenTutor.getClientPhoto())
                .name(clientDetailsForGivenTutor.getName())
                .lastname(clientDetailsForGivenTutor.getLastname())
                .email(client.getEmail())
                .phoneNumber(clientDetailsForGivenTutor.getPhoneNumber())
                .meetingLink(clientDetailsForGivenTutor.getMeetingLink())
                .storageLink(clientDetailsForGivenTutor.getStorageLink())
                .additionalInformation(clientDetailsForGivenTutor.getAdditionalInformation())
                .isAvailableToChangeReservation(clientDetailsForGivenTutor.getIsAvailableToChangeReservation())
                .isAvailableToCancelReservation(clientDetailsForGivenTutor.getIsAvailableToCancelReservation())
                .minimumChangeTime(clientDetailsForGivenTutor.getMinimumChangeTime())
                .nip(clientDetailsForGivenTutor.getNip())
                .regon(clientDetailsForGivenTutor.getRegon())
                .color(clientDetailsForGivenTutor.getColor())
                .isBusinessInvoice(clientDetailsForGivenTutor.getIsBusinessInvoice())
                .province(clientDetailsForGivenTutor.getProvince())
                .postcode(clientDetailsForGivenTutor.getPostcode())
                .streetAndNumber(clientDetailsForGivenTutor.getStreetAndNumber())
                .build();
    }

}
