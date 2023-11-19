package codepred.client;

import codepred.account.AppUserRole;
import codepred.account.User;
import codepred.attendance.Attendance;
import codepred.attendance.AttendanceRepository;
import codepred.client.dto.*;
import codepred.common.mapper.ClientMapper;
import codepred.common.mapper.NoteMapper;
import codepred.common.util.ResponseObject;
import codepred.config.EmailService;
import codepred.exception.CustomException;
import codepred.group.Group;
import codepred.group.GroupRepository;
import codepred.group.dto.GroupIdAndNameDto;
import codepred.meeting.Event;
import codepred.meeting.EventRepository;
import codepred.meeting.MeetingStatus;
import codepred.meeting.dto.ClientMeetingReadDto;
import codepred.meeting.dto.MeetingsDto;
import codepred.note.Note;
import codepred.note.NoteRepository;
import codepred.note.dto.NoteDto;
import codepred.payment.dto.PaymentForInvoiceDto;
import codepred.service.Service;
import codepred.service.ServiceRepository;
import codepred.service.dto.ServiceDto;
import codepred.tutor.TutorRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static codepred.common.mapper.ClientDataMapper.*;

@Slf4j
@Component
public class ClientService {
    public static final int PAGE_SIZE = 10;
    private final EmailService emailService;
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final TutorRepository tutorRepository;
    private final GroupRepository groupRepository;
    private final ServiceRepository serviceRepository;
    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;
    private final ClientDetailsRepository clientDetailsRepository;
    private final AttendanceRepository attendanceRepository;
    private final EventRepository eventRepository;

    @Value("${frontend_path}")
    private String frontend_path;

    public ClientService(EmailService emailService,
                         ClientRepository clientRepository,
                         ClientMapper clientMapper,
                         TutorRepository tutorRepository,
                         GroupRepository groupRepository,
                         ServiceRepository serviceRepository,
                         NoteRepository noteRepository,
                         NoteMapper noteMapper,
                         ClientDetailsRepository clientDetailsRepository,
                         AttendanceRepository attendanceRepository,
                         EventRepository eventRepository) {
        this.emailService = emailService;
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
        this.tutorRepository = tutorRepository;
        this.groupRepository = groupRepository;
        this.serviceRepository = serviceRepository;
        this.noteRepository = noteRepository;
        this.noteMapper = noteMapper;
        this.clientDetailsRepository = clientDetailsRepository;
        this.attendanceRepository = attendanceRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public ResponseObject createClient(NewClientRequest newClientRequest, User tutor) {
        if(newClientRequest.getMinimumChangeTime() == null) newClientRequest.setMinimumChangeTime(0);
        User existingClient = clientRepository.findClientByEmail(newClientRequest.getEmail()).orElse(null);

        if ((newClientRequest.getEmail()).equals(tutor.getEmail())) {
            return new ResponseObject(HttpStatus.BAD_REQUEST, "TUTOR_EMAIL_CANT_BE_CLIENT_EMAIL", null);
        }
        if(existingClient != null) {
            if (existingClient.getAppUserRoles().equals(AppUserRole.ROLE_ADMIN)) {
                return new ResponseObject(HttpStatus.BAD_REQUEST, "CANNOT_CREATE_EXISTING_ADMIN_AS_STUDENT", null);
            }
        }

        if (existingClient==null) {

            String code = RandomStringUtils.randomAlphanumeric(30, 30);
            //ClientDetails clientDetails = clientMapper.fromDtoWithPhoto(newClientRequest);
            ClientDetails clientDetails =  ClientDetails.builder()
                    .name(newClientRequest.getName())
                    .lastname(newClientRequest.getLastname())
                    .streetAndNumber(newClientRequest.getStreetAndNumber())
                    .postcode(newClientRequest.getPostcode())
                    .province(newClientRequest.getProvince())
                    .phoneNumber(newClientRequest.getPhoneNumber())
                    .createdAt(LocalDateTime.now())
                    .color(newClientRequest.getColor())
                    .meetingLink(newClientRequest.getMeetingLink())
                    .storageLink(newClientRequest.getStorageLink())
                    .minimumChangeTime(newClientRequest.getMinimumChangeTime().intValue())
                    .additionalInformation(newClientRequest.getAdditionalInformation())
                    .isBusinessInvoice(newClientRequest.getIsBusinessInvoice().booleanValue())
                    .isAvailableToChangeReservation(newClientRequest.getIsAvailableToChangeReservation().booleanValue())
                    .isAvailableToCancelReservation(newClientRequest.getIsAvailableToCancelReservation().booleanValue())
                    .nip(newClientRequest.getNip())
                    .regon(newClientRequest.getRegon())
                    .build();

            clientDetails.setTutorId(tutor.getId());

            User newClient = User.builder()
                    .clientDetails(Set.of(clientDetails))
                    .codeTimeGenerated(Date.from(Instant.now()))
                    .appUserRoles(AppUserRole.ROLE_CLIENT)
                    .email(newClientRequest.getEmail())
                    .createdAt(LocalDateTime.now(ZoneId.of("Europe/Warsaw")))
                    .code(code)
                    .build();

            clientDetails.setUser(newClient);

            setClientPhotoIfExist(newClientRequest.getPhoto(), newClient, tutor);

            tutor.getClients().add(newClient);
            User savedTutor = tutorRepository.save(tutor);

            //clientRepository.save(newClient);

            emailService.sendEmailForNewClient(newClientRequest.getEmail(),
                                               "Tutorio",
                                               newClientRequest.getName(),
                                               "Zaproszenie do systemu",
                                               tutor.getTutorDetails().getName() + " " + tutor.getTutorDetails().getSurname(),
                                               frontend_path + "/customer/register?code=" + code + "&email="
                                                   + newClientRequest.getEmail());
            log.debug("ClientService ==> createClient() - end: code = {}, message = {}, token = {}",
                      HttpStatus.ACCEPTED,
                      "CLIENT_SUCCESSFULLY_SAVE",
                      null);
            return new ResponseObject(HttpStatus.ACCEPTED, savedTutor.getClients().stream().filter(c -> c.getEmail().equals(newClient.getEmail())).findFirst().get().getId().toString(), null);

        } else if (clientIsAlreadyOnTutorList(newClientRequest, tutor)) {
            return new ResponseObject(HttpStatus.BAD_REQUEST, "CLIENT_ALREADY_EXIST", null);
        }

        else {
            tutor.getClients().add(existingClient);
            tutorRepository.save(tutor);
            ClientDetails newClientDetails =  ClientDetails.builder()
                    .name(newClientRequest.getName())
                    .lastname(newClientRequest.getLastname())
                    .streetAndNumber(newClientRequest.getStreetAndNumber())
                    .postcode(newClientRequest.getPostcode())
                    .province(newClientRequest.getProvince())
                    .phoneNumber(newClientRequest.getPhoneNumber())
                    .color(newClientRequest.getColor())
                    .meetingLink(newClientRequest.getMeetingLink())
                    .createdAt(LocalDateTime.now())
                    .storageLink(newClientRequest.getStorageLink())
                    .minimumChangeTime(newClientRequest.getMinimumChangeTime())
                    .additionalInformation(newClientRequest.getAdditionalInformation())
                    .isBusinessInvoice(newClientRequest.getIsBusinessInvoice())
                    .isAvailableToChangeReservation(newClientRequest.getIsAvailableToChangeReservation())
                    .isAvailableToCancelReservation(newClientRequest.getIsAvailableToCancelReservation())
                    .nip(newClientRequest.getNip())
                    .regon(newClientRequest.getRegon())
                    .build();
            newClientDetails.setTutorId(tutor.getId());
            newClientDetails.setUser(existingClient);
            clientDetailsRepository.save(newClientDetails);
            emailService.sendEmailForExitingClient(newClientRequest.getEmail(),
                                                   "Tutorio",
                                                   newClientRequest.getName(),
                                                   "Zaproszenie do systemu",
                                                   tutor.getTutorDetails().getName() + " " + tutor.getTutorDetails().getSurname());

            log.debug("ClientService ==> createClient() - end: code = {}, message = {}, token = {}",
                      HttpStatus.ACCEPTED,
                      "CLIENT_SUCCESSFULLY_SAVE",
                      null);
            return new ResponseObject(HttpStatus.ACCEPTED, existingClient.getId().toString(), null);
        }
    }

    private boolean clientIsAlreadyOnTutorList(NewClientRequest NewClientRequest, User tutor) {
        return tutor.getClients()
                .stream()
                .anyMatch(user -> user.getEmail().equals(NewClientRequest.getEmail()));
    }

    private void setClientPhotoIfExist(MultipartFile NewClientRequest, User client, User tutor) {
        ClientDetails detailsForGivenTutor = findClientDetailsForGivenTutor(client, tutor);
        if (NewClientRequest != null && !NewClientRequest.isEmpty()) {
            try {
                detailsForGivenTutor.setClientPhoto(cropToCircle(NewClientRequest));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            detailsForGivenTutor.setClientPhoto(null);
        }
    }

    public ClientDetails findClientDetailsForGivenTutor(User client, User tutor) {
        return client.getClientDetails()
                .stream()
                .filter(clientDetails -> clientDetails.getTutorId().intValue() == tutor.getId().intValue())
                .findFirst()
                .orElseThrow(() -> new CustomException("Not found client details for given tutor", HttpStatus.BAD_REQUEST));
    }

    public ClientFullDataDto getClientData(Integer clientId, User tutor) {
        log.debug("ClientService ==> getClientData() - start: clientId = {}, Tutor = {}", clientId, tutor);

        User client = clientRepository.findById(clientId).orElseThrow(() -> {
            log.debug("ClientService ==> getClientData() - end: code = {}, message = {}", HttpStatus.BAD_REQUEST, "CLIENT_NOT_FOUND");
            return new CustomException("CLIENT_NOT_FOUND", HttpStatus.BAD_REQUEST);
        });

        Group actualTutorGroup = client.getGroups()
                .stream()
                .filter(group -> group.getUser().contains(tutor))
                .findFirst()
                .orElse(null);

        Service actualTutorService = client.getServices()
                .stream()
                .filter(service -> service.getUsers().contains(tutor))
                .findFirst()
                .orElse(null);
        var clientDetailsForGivenTutor = findClientDetailsForGivenTutor(client, tutor);
        ClientFullDataDto clientDTO = mapClientToDTO(client, clientDetailsForGivenTutor);

        clientDTO.setDefaultService(actualTutorService != null ? mapServiceToDto(actualTutorService) : null);
        clientDTO.setDefaultGroupDTO(actualTutorGroup != null ? mapGroupToDTO(actualTutorGroup) : null);

        log.debug("ClientService ==> getClientData() - end: client = {}", client);
        return clientDTO;
    }

    public List<User> getClientsData(User user, int pageNumber, int pageSize) {
        log.debug("ClientService ==> getClientData() - start: Tutor = {}, PageNumber = {}, PageSize = {}", user, pageNumber, pageSize);
        var clients = clientRepository.findClientsByTutor(user.getId(), PageRequest.of(pageNumber - 1, pageSize));
        log.debug("ClientService ==> getClientData() - end: clients = {}", clients);
        return clients.getContent();
    }

    public Integer getNumberOfPagesForClients(User user) {
        log.debug("ClientService ==> getNumberOfPagesForClients() - start: Tutor = {}", user);
        long clientCount = clientRepository.countByUsersId(user.getId());
        int totalPages = (int) Math.ceil((double) clientCount / PAGE_SIZE);
        log.debug("ClientService ==> getNumberOfPagesForClients() - start: totalNumber = {}", totalPages);
        return totalPages;
    }

    public List<ServiceDto> servicesForClient(User user) {
        log.debug("ClientService ==> groupsForClient() - start: Tutor = {}", user);
        var services = serviceRepository.findServiceByUserId(user.getId());
        List<ServiceDto> finalServices = new ArrayList<>();
        for (Service iterations : services) {
            var groupData = iterations.getServiceName();
            finalServices.add(new ServiceDto(iterations.getId(), groupData, iterations.getServiceDuration(), iterations.getPrice()));
        }
        log.debug("ClientService ==> groupsForClient() - end: services = {}", finalServices);
        return finalServices;
    }

    public ResponseObject updateClientData(User tutor, ClientUpdateRequest clientDTO) {
        log.debug("ClientService ==> updateClientData() - start: Tutor = {}, client = {}", tutor, clientDTO);
        if(clientDTO.getMinimumChangeTime() == null) clientDTO.setMinimumChangeTime(0);
        var tutorFromDb = tutorRepository.findById(tutor.getId()).get();
        var clientFromDb = clientRepository.findByTutorIdAndClientId(tutorFromDb.getId(), clientDTO.getId());
        if (clientFromDb.isEmpty()) {
            var response = new ResponseObject(HttpStatus.BAD_REQUEST, "NO_ACCESS", null);
            log.debug("ClientService ==> updateClientData() - end: ResponseObject = {}", response);
            return response;
        } else {
            var updatedClient = clientFromDb.get();
            var clientDetailsForGivenTutor = clientDetailsRepository.findByTutorIdAndClientId(clientFromDb.get().getId(), tutorFromDb.getId());
            if (!clientDTO.getName().isEmpty()) clientDetailsForGivenTutor.setName(clientDTO.getName());
            if (!clientDTO.getLastname().isEmpty()) clientDetailsForGivenTutor.setLastname(clientDTO.getLastname());
            if (!clientDTO.getEmail().isEmpty()) {
                if (!tutorFromDb.getEmail().equals(clientDTO.getEmail())) updatedClient.setEmail(clientDTO.getEmail());
                else {
                    var response = new ResponseObject(HttpStatus.UNPROCESSABLE_ENTITY, "CANNOT_USE_THIS_EMAIL", null);
                    log.debug("ClientService ==> updateClientData() - end: ResponseObject = {}", response);
                }
            }
            clientDetailsForGivenTutor.setStreetAndNumber(clientDTO.getStreetAndNumber());
            clientDetailsForGivenTutor.setPostcode(clientDTO.getPostcode());
            clientDetailsForGivenTutor.setProvince(clientDTO.getProvince());
            clientDetailsForGivenTutor.setPhoneNumber(clientDTO.getPhoneNumber());
            clientDetailsForGivenTutor.setColor(clientDTO.getColor());
            clientDetailsForGivenTutor.setMeetingLink(clientDTO.getMeetingLink());
            clientDetailsForGivenTutor.setStorageLink(clientDTO.getStorageLink());
            clientDetailsForGivenTutor.setMinimumChangeTime(clientDTO.getMinimumChangeTime().intValue());
            clientDetailsForGivenTutor.setAdditionalInformation(clientDTO.getAdditionalInformation());
            clientDetailsForGivenTutor.setNip(clientDTO.getNip());
            clientDetailsForGivenTutor.setRegon(clientDTO.getRegon());
            clientDetailsForGivenTutor.setIsAvailableToCancelReservation(clientDTO.getIsAvailableToCancelReservation().booleanValue());
            clientDetailsForGivenTutor.setIsAvailableToChangeReservation(clientDTO.getIsAvailableToChangeReservation().booleanValue());

            if (clientDTO.getChangePhoto().equals(Boolean.TRUE)) {
                if (clientDTO.getPhoto() != null && !clientDTO.getPhoto().isEmpty()) {
                    try {
                        clientDetailsForGivenTutor.setClientPhoto(cropToCircle(clientDTO.getPhoto()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    clientDetailsForGivenTutor.setClientPhoto(null);
                }
            }
            clientDetailsForGivenTutor.setIsBusinessInvoice(clientDTO.getIsBusinessInvoice().booleanValue());
            clientDetailsRepository.save(clientDetailsForGivenTutor);
            clientRepository.save(updatedClient);
            var response = new ResponseObject(HttpStatus.OK, "CLIENT_SUCCESSFULLY_UPDATED", null);
            log.debug("ClientService ==> updateClientData() - end: response = {}", response);
            return response;
        }
    }

    public List<GroupIdAndNameDto> groupForClientToCreateClient(User user) {
        log.debug("ClientService ==> groupForClientToCreateClient() - start: Tutor = {}", user);
        var groups = groupRepository.findByUserId(user.getId());
        List<GroupIdAndNameDto> groupsToReturn = new ArrayList<>();
        for (Group iteration : groups) {
            groupsToReturn.add(new GroupIdAndNameDto(iteration.getId(), iteration.getGroupName()));
        }
        log.debug("ClientService ==> groupForClientToCreateClient() - end: groups = {}", groupsToReturn);
        return groupsToReturn;
    }

    @Transactional
    public ResponseObject updateClientGroups(User user, Integer clientId, Integer groupId) {
        log.debug("ClientService ==> updateClientGroups() - start: Tutor = {}, clientId = {}, groupId = {}", user, clientId, groupId);
        var client = clientRepository.findByTutorIdAndClientId(user.getId(), clientId).orElseThrow(() -> new CustomException("CLIENT_DOESNT_EXIST",HttpStatus.BAD_REQUEST));
        var group = groupRepository.findGroupByIdAndUser(groupId, user.getId()).orElse(null);
        if (client != null && groupId == 0) {
            clientRepository.removeClientGroupConnection(clientId);
            return new ResponseObject(HttpStatus.OK, "CLIENT_UPDATED", null);
        }
        if (client != null  && group != null) {
            if (client.getGroups().contains(group)) {
                var response = new ResponseObject(HttpStatus.OK, "CLIENT_UPDATED", null);
                log.debug("ClientService ==> updateClientGroups() - end: Response = {}", response);
                return response;
            } else if(client.getGroups().size() > 0 && !client.getGroups().contains(group)) {
                clientRepository.removeClientGroupConnection(clientId);
                client.getGroups().remove(group);
                client.getGroups().add(group);
                clientRepository.save(client);
                var response = new ResponseObject(HttpStatus.OK, "CLIENT_UPDATED", null);
                log.debug("ClientService ==> updateClientGroups() - end: Response = {}", response);
                return response;
            }
            else {
//                clientRepository.removeClientGroupConnection(clientId);
                client.getGroups().add(group);
                clientRepository.save(client);
                var response = new ResponseObject(HttpStatus.OK, "CLIENT_UPDATED", null);
                log.debug("ClientService ==> updateClientGroups() - end: Response = {}", response);
                return response;
            }
        } else {
            var response = new ResponseObject(HttpStatus.BAD_REQUEST, "CLIENT_OR_GROUP_DOES_NOT_EXIST", null);
            log.debug("ClientService ==> updateClientGroups() - end: Response = {}", response);
            return response;
        }
    }

    @Transactional
    public ResponseObject updateClientService(User user, Integer clientId, Integer serviceId) {
        log.debug("ClientService ==> updateClientService() - start: Tutor = {}, clientId = {}, serviceId = {}",
                user, clientId, serviceId);
        var client = clientRepository.findByTutorIdAndClientId(user.getId(), clientId).orElseThrow(() -> new CustomException("SERVICE_NOT_FOUND",HttpStatus.BAD_REQUEST));
        var service = serviceRepository.findServiceByIdAndUser(serviceId, user).orElseThrow(() -> new CustomException("SERVICE_NOT_FOUND",HttpStatus.BAD_REQUEST));
        if (client != null && service != null) {
//            clientRepository.removeServiceClientConnection(clientId);
            client.getServices().add(service);
            clientRepository.save(client);
            var response = new ResponseObject(HttpStatus.OK, "CLIENT_UPDATED", null);
            log.debug("ClientService ==> updateClientService() - end: Response = {}", response);
            return response;
        } else {
            var response = new ResponseObject(HttpStatus.OK, "CLIENT_OR_SERVICE_DOES_NOT_EXIST", null);
            log.debug("ClientService ==> updateClientService() - end: Response = {}", response);
            return response;
        }
    }

    public ResponseObject createNote(User user, Integer clientId, String content) {
        log.debug("ClientService ==> createNote() - end: user = {}, clientId = {}, content = {}", user, clientId, content);
        var tutor = tutorRepository.findById(user.getId());
        var client = clientRepository.findByTutorIdAndClientId(user.getId(), clientId);
        if (tutor.isEmpty() || client.isEmpty()) {
            var response = new ResponseObject(HttpStatus.BAD_REQUEST, "CLIENT_DOES_NOT_EXIST", null);
            log.debug("ClientService ==> createNote() - end: response = {}", response);
            return response;
        } else {
            var note = new Note();
            note.setContent(content);
            noteRepository.save(note);
            tutor.get().getNotes().add(note);
            client.get().getNotes().add(note);
            tutorRepository.save(tutor.get());
            tutorRepository.save(client.get());
            var response = new ResponseObject(HttpStatus.OK, "NOTE_CREATED", null);
            log.debug("ClientService ==> createNote() - end: response = {}", response);
            return response;
        }
    }

    public List<NoteDto> getNotesForClient(User user, Integer clientId) {
        log.debug("ClientService ==> getNotesForClient() - start: user = {}, clientId = {}", user, clientId);
        var notes = noteRepository.findByTutorIdAndClientId(user.getId(), clientId);
        List<NoteDto> notesDTOs = noteMapper.fromNoteDto(notes);

        List<NoteDto> response = new ArrayList<>();
        String previousMonth = null;

        Map<String, String> monthMap = new HashMap<>();
        monthMap.put("stycznia", "styczeń");
        monthMap.put("lutego", "luty");
        monthMap.put("marca", "marzec");
        monthMap.put("kwietnia", "kwiecień");
        monthMap.put("maja", "maj");
        monthMap.put("czerwca", "czerwiec");
        monthMap.put("lipca", "lipiec");
        monthMap.put("sierpnia", "sierpień");
        monthMap.put("września", "wrzesień");
        monthMap.put("października", "październik");
        monthMap.put("listopada", "listopad");
        monthMap.put("grudnia", "grudzień");

        for (NoteDto note : notesDTOs) {
            String currentMonth = note.getCreatedAt().format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("pl", "PL")));

            String[] parts = currentMonth.split(" ");
            parts[0] = monthMap.getOrDefault(parts[0], parts[0]);
            currentMonth = String.join(" ", parts);

            if (!currentMonth.equals(previousMonth)) {
                NoteDto monthNote = new NoteDto();
                monthNote.setType("DATE");
                monthNote.setContent(currentMonth);

                response.add(monthNote);
                previousMonth = currentMonth;
            }
            if (note.getType() == null) {
                note.setType("NOTE");
            }
            response.add(note);
        }

        log.debug("ClientService ==> getNotesForClient() - end: response = {}", response);
        return response;
    }

    @Transactional
    public ResponseObject removeNote(User user, Integer clientId, Integer noteId) {
        log.debug("ClientService ==> removeNote() - start: user = {}, clientId = {}, noteId = {}", user, clientId, noteId);
        var tutor = tutorRepository.findById(user.getId()).orElseThrow(() -> new CustomException("TUTOR_DOES_NOT_EXIST", HttpStatus.BAD_REQUEST));
        var client = clientRepository.findByTutorIdAndClientId(user.getId(), clientId).orElseThrow(() -> new CustomException("CLIENT_DOES_NOT_EXISTS", HttpStatus.BAD_REQUEST));
        var note = noteRepository.findById(noteId).orElseThrow(() -> new CustomException("TUTOR_DOES_NOT_EXIST", HttpStatus.BAD_REQUEST));
        noteRepository.removeNoteUserConnection(noteId);
        noteRepository.delete(note);
        var response = new ResponseObject(HttpStatus.OK, "NOTE_SUCCESSFULLY_REMOVED", null);
        log.debug("ClientService ==> removeNote() - end: response = {}", response);
        return response;

    }

    public List<ClientForTutorDto> findClientByNameAndSurname(User tutor, String name) {
        log.debug("ClientService ==> findClientByName() - start: user = {}, name and surname = {}", tutor, name);
        String[] nameParts = name.split(" ");
        List<User> result = new ArrayList<>();

        for (String part : nameParts) {
            result.addAll(clientRepository.findClientByContent(part, tutor.getId()));
        }
        var finalResult = result.stream()
            .distinct()
            .collect(Collectors.toList());
        log.debug("ClientService ==> findClientByName() - end: result = {}", tutor);
        return finalResult.stream().map(userToMap -> {
            var clientDetailsForGivenTutor = findClientDetailsForGivenTutor(userToMap, tutor);
            if(userToMap.getAppUserRoles().equals(AppUserRole.ROLE_TUTOR)) return null;
            if(userToMap.getAppUserRoles().equals(AppUserRole.ROLE_ADMIN)) return null;
            String groupName = null;
            String companyName = null;
            if(userToMap.getGroups().size() > 0) {
                groupName = userToMap.getGroups().stream().findFirst().get().getGroupName();
            }
            if(userToMap.getGroups().stream().findFirst().get().getCompany() != null) {
                companyName = userToMap.getGroups().stream().findFirst().get().getCompany().getName();
            }
            return new ClientForTutorDto(
                userToMap.getId(),
                    clientDetailsForGivenTutor.getClientPhoto(),
                    clientDetailsForGivenTutor.getName(),
                    clientDetailsForGivenTutor.getLastname(),
                groupName,
                companyName
            );
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Transactional
    public ResponseObject removeClients(User tutor, Integer clientId) {
        log.debug("ClientService ==> removeClients() - start: user = {}", clientId);
        var clientFromDb = clientRepository.findStudentById(clientId);
        ClientDetails clientDetailsForGivenTutor = clientDetailsRepository.findByTutorIdAndClientId(clientId, tutor.getId());
        int tutorsForClientAmount = clientRepository.countTutorsToClient(clientId);
        if (clientFromDb.isPresent() && clientFromDb.get().getAppUserRoles() == AppUserRole.ROLE_CLIENT && clientDetailsForGivenTutor != null) {
            var client = clientFromDb.get();
            if (tutorsForClientAmount > 0) {
                Set<User> currentClientList = tutor.getClients();
                currentClientList.remove(client);
                tutor.setClients(currentClientList);
                Set<Integer> clientGroupsIds = client.getGroups().stream().map(Group::getId).collect(Collectors.toSet());
                Set<Group> groupsForGivenTutorAndClient = tutor.getGroups()
                        .stream()
                        .filter(group -> clientGroupsIds.contains(group.getId()))
                        .collect(Collectors.toSet());
                Set<Integer> groupsForGivenTutorAndClientIds = groupsForGivenTutorAndClient.stream().map(Group::getId).collect(Collectors.toSet());
                Set<Group> clientGroupsAfterRemove = client.getGroups().stream()
                        .filter(clientGroup -> !groupsForGivenTutorAndClientIds.contains(clientGroup.getId()))
                        .collect(Collectors.toSet());
                client.setGroups(clientGroupsAfterRemove);
                clientRepository.save(client);
                tutorRepository.save(tutor);
            } else {
                client.setCompanies(null);
                client.setServices(null);
                client.setGroups(null);
                client.setEvents(null);
                client = clientRepository.saveAndFlush(client);
                clientRepository.delete(client);
            }
            clientDetailsRepository.delete(clientDetailsForGivenTutor);
            return new ResponseObject(HttpStatus.OK, "CLIENT_SUCCESSFULLY_REMOVED", null);
        } else {
            return new ResponseObject(HttpStatus.OK, "CLIENT_NOT_FOUND", null);
        }
    }

    @Transactional
    public List<ClientMeetingReadDto> getMeetings(User user, Integer clientId, LocalDateTime startTime, LocalDateTime endTime, int pageNumber, String clientType) {
        int adjustedPageNumber = pageNumber - 1;
        Pageable pageable = PageRequest.of(adjustedPageNumber, 10);
        Page<Attendance> attendances = attendanceRepository.findByClientIdAndTimeRange(clientId,startTime,endTime, pageable);
        var response = new ArrayList<ClientMeetingReadDto>();

        for (Attendance attendance : attendances) {
            String filter = clientType.equals("individual") ? "STUDENT" : clientType.equals("all") ? "" : "CLIENT_IN_GROUP";
            if(!filter.equals("")) {
                if (!attendance.getClientType().equals(filter)) continue;
            }
            var event = eventRepository.getCertainEvent(user.getId(), attendance.getEvent().getId());
            if(event.isEmpty()) continue;
            if (LocalDateTime.now().isAfter(event.get().getStartTime()) && attendance.getMeetingStatus() == MeetingStatus.planned) {
                attendance.setMeetingStatus(MeetingStatus.finished);
                attendance = attendanceRepository.saveAndFlush(attendance);
            }
            if (attendance.getClientType().equals("STUDENT")) {

                response.add(new ClientMeetingReadDto(
                        attendance.getId(),
                        "individual",
                        event.get().getStartTime(),
                        attendance.getMeetingStatus(),
                        event.get().getService().getPrice(),
                        attendance.getValuePaid(),
                        event.get().getId()
                ));
            } else
                response.add(new ClientMeetingReadDto(
                        attendance.getId(),
                        "group",
                        groupRepository.findGroupByEvent(
                                attendance.getEvent().getId())
                            .orElseThrow(() -> new CustomException("GROUP_NOT_FOUND", HttpStatus.BAD_REQUEST))
                            .getGroupName(),
                        event.get().getStartTime(),
                        attendance.getMeetingStatus(),
                        event.get().getService().getPrice(),
                        attendance.getValuePaid(),
                        event.get().getId()
                ));
        }

        return response.stream()
                .sorted(Comparator.comparing(ClientMeetingReadDto::getPrice).reversed())
                .collect(Collectors.toList());
    }

    public MeetingsDto getTotalSumAndPageSize(User tutor, Integer clientId, LocalDateTime startTime, LocalDateTime endTime, String clientType) {
        List<Attendance> totalAttendances = attendanceRepository.findByClientIdAndTimeRange(clientId,startTime,endTime);
        String filter = clientType.equals("individual") ? "STUDENT" : clientType.equals("all") ? "" : "CLIENT_IN_GROUP";
        if(!filter.equals("")) {
            totalAttendances = totalAttendances.stream().filter( attendance -> attendance.getClientType().equals(filter)).collect(Collectors.toList());
        }
        int numberOfPages = (int) Math.ceil((double) totalAttendances.size() / 10);
        Float totalSum = 0f;
        Float valuePaid = 0f;


        for (Attendance attendance: totalAttendances) {
            Optional<Event> event = eventRepository.getCertainEvent(tutor.getId(), attendance.getEvent().getId());
            if(event.isEmpty()) continue;
            totalSum += event.get().getService().getPrice().floatValue();
            if(attendance.getValuePaid() != null){
                valuePaid += attendance.getValuePaid();
            }
        }

        return MeetingsDto.builder()
                .numberOfPages(String.valueOf(numberOfPages))
                .summedValue(totalSum.toString())
                .paidValue(valuePaid.toString())
                .meetingIds(totalAttendances.stream().map(a -> a.getId()).collect(Collectors.toList()))
                .build();
    }

    public ResponseObject updatePayment(Integer attendanceId, Float valuePaid) {
        var attendance = attendanceRepository.findById(attendanceId).orElseThrow(() -> new CustomException("ATTENDANCE_NOT_FOUND", HttpStatus.BAD_REQUEST));
        attendance.setValuePaid(valuePaid);
        attendanceRepository.save(attendance);
        return new ResponseObject(HttpStatus.ACCEPTED, "SUCCESSFULLY_UPDATED", null);
    }

    public List<PaymentForInvoiceDto> getPaymentsForInvoice(List<Integer> attendancesId) {
        List<Attendance> attendances = attendancesId.stream()
            .map(attendanceId -> attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new CustomException("ATTENDANCE_NOT_FOUND", HttpStatus.BAD_REQUEST)))
            .collect(Collectors.toList());

        List<ClientPaymentForInvoiceDtoWithServiceId> paymentForInvoiceDtoList = attendances.stream().map(item -> {
            Service service = serviceRepository.findServiceById(item.getServiceId()).orElseThrow();
            return new ClientPaymentForInvoiceDtoWithServiceId(
                    item.getClientId(),
                    service.getId(),
                    service.getServiceName(),
                    service.getPkwiu(),
                    1,
                    service.getTimeUnit(),
                    service.getPrice(),
                    service.getVat(),
                    item.getValuePaid() != null ? item.getValuePaid() : 0.0f);
        }).collect(Collectors.toList());

        List<ClientPaymentForInvoiceDtoWithServiceId> groupedPayments = new ArrayList<>(paymentForInvoiceDtoList.stream()
                .collect(Collectors.toMap(
                        dto -> Arrays.asList(dto.getServiceId()),
                        Function.identity(),
                        (payment1, payment2) -> new ClientPaymentForInvoiceDtoWithServiceId(
                                payment1.getId(),
                                payment1.getServiceId(),
                                payment1.getName(),
                                payment1.getPkwiu(),
                                payment1.getNumber() + 1,
                                payment1.getUnit(),
                                payment1.getPrice(),
                                payment1.getVat(),
                                payment1.getValuePaid() + payment2.getValuePaid()
                        )
                ))
                .values());

        return groupedPayments.stream().map( payment -> PaymentForInvoiceDto.builder()
                .id(payment.getId())
                .name(payment.getName())
                .pkwiu(payment.getPkwiu())
                .number(payment.getNumber())
                .unit(payment.getUnit())
                .price(payment.getPrice())
                .vat(payment.getVat())
                .valuePaid(payment.getValuePaid()).build()
        ).collect(Collectors.toList());
    }

    @Transactional
    public ResponseObject changeMeetingStatus(User tutor, Integer attendanceId) {
        var attendance = attendanceRepository.findById(attendanceId).orElseThrow(() -> new CustomException("ATTENDANCE_NOT_FOUND", HttpStatus.BAD_REQUEST));
        if (attendance.getMeetingStatus().equals(MeetingStatus.finished)) {
            attendance.setMeetingStatus(MeetingStatus.missed);
        }
        else if(attendance.getMeetingStatus().equals(MeetingStatus.missed)) {
            attendance.setMeetingStatus(MeetingStatus.finished);
        }
        attendanceRepository.save(attendance);
        return new ResponseObject(HttpStatus.OK, "STATUS_UPDATED", null);
    }

    public List<ClientForTutorDto> getResponseAllClientForTutors(User tutor, List<User> clients) {

        List<ClientForTutorDto> tutorClientsDTOlist = new LinkedList<>();

        for (User client : clients) {
            ClientForTutorDto clientDTO = new ClientForTutorDto();
            var clientDetailsForGivenTutor = findClientDetailsForGivenTutor(client, tutor);
            clientDTO.setId(client.getId());
            clientDTO.setClientPhoto(clientDetailsForGivenTutor.getClientPhoto());
            clientDTO.setName(clientDetailsForGivenTutor.getName());
            clientDTO.setLastname(clientDetailsForGivenTutor.getLastname());

            Group actualTutorGroup = client.getGroups()
                    .stream()
                    .filter(group -> group.getUser().contains(tutor))
                    .findFirst()
                    .orElse(null);

            if (actualTutorGroup != null) {
                clientDTO.setGroupName(actualTutorGroup.getGroupName());
                if (actualTutorGroup.getCompany() != null) {
                    clientDTO.setCompanyName(actualTutorGroup.getCompany().getName());
                }
            }

            tutorClientsDTOlist.add(clientDTO);
        }
        return tutorClientsDTOlist;
    }

    public byte[] cropToCircle(MultipartFile originalFile) throws IOException {
        log.debug("ClientService ==> cropToCircle() - start: photo = {}", originalFile);
        BufferedImage originalImage;

        if (originalFile == null || originalFile.isEmpty()) {
            return null;
        } else {
            originalImage = ImageIO.read(originalFile.getInputStream());
        }


        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;


        int targetSize = 150;
        BufferedImage circleImage = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = circleImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setClip(new Ellipse2D.Float(0, 0, targetSize, targetSize));
        g2d.drawImage(originalImage, 0, 0, targetSize, targetSize, x, y, x + size, y + size, null);
        g2d.dispose();


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(circleImage, "png", baos);
        baos.flush();
        byte[] croppedImageBytes = baos.toByteArray();
        baos.close();
        log.debug("ClientService ==> cropToCircle() - end: croppedImageBytes = {}", croppedImageBytes);
        return croppedImageBytes;
    }


}
