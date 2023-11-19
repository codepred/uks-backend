package codepred.service;

import codepred.common.util.ResponseObject;
import codepred.service.dto.ServiceReadDto;
import codepred.service.dto.UpdateServiceRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ServiceController {

    @GetMapping("/get-all-services")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get all services for tutor", description = "Get services for tutors.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accepted."),
    })
    List<ServiceReadDto> getServicesForClients(HttpServletRequest request);

    @PutMapping("/update-service")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Update service for tutor", description = "Update services for tutors.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Successfully updated."),
    })
    ResponseObject updateServiceForTutor(HttpServletRequest request, @RequestBody UpdateServiceRequest UpdateServiceRequest);

    @PostMapping("/remove-service/{id}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Remove service for tutor", description = "Remove services for tutors.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully removed."),
            @ApiResponse(responseCode = "422", description = "Service cannot be removed because it is connected to clients."),
    })
    ResponseObject removeService(HttpServletRequest request, @PathVariable("id") Integer serviceId);

    @GetMapping("/get-certain-service/{id}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get certain service for tutor", description = "Get certain services for tutor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service successfully get."),
    })
    ServiceReadDto getCertainService(HttpServletRequest request, @PathVariable("id") Integer serviceId);

    @GetMapping("/count-clients-connected-with-service/{serviceId}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Counts clients", description = "Count clients connected with service.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Number successfully get."),
    })
    Integer countClientsConnectedWithService(HttpServletRequest request, @PathVariable("serviceId") Integer serviceId);
}
