package codepred.payment;

import codepred.client.dto.ClientDto;
import codepred.common.util.ResponseObject;
import codepred.invoice.dto.InvoicesDto;
import codepred.payment.dto.PaymentChangeStatusDto;
import codepred.payment.dto.PaymentDto;
import codepred.payment.dto.PaymentReadDto;
import codepred.payment.dto.PaymentRepeatDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface PaymentController {

    @GetMapping("/get-list")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get all payments for tutor", description = "Get payments for tutors.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created."),
    })
    @CrossOrigin
    List<PaymentReadDto> getPaymentList(HttpServletRequest request);

    @PostMapping("/create-payment")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Create payment.", description = "Create payment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK.")
    })
    @CrossOrigin
    ResponseObject createPayment(HttpServletRequest request, @RequestBody InvoicesDto invoicesDto);


    @GetMapping("/get-all-clients")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get all clients", description = "all clients list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @CrossOrigin
    List<ClientDto> getAllClients(HttpServletRequest request);


    @GetMapping("/download/{invoice}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Create pdf invoice", description = "create and download pdf invoice")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @CrossOrigin
    ResponseEntity<byte[]> generateInvoice(HttpServletRequest request, @PathVariable("invoice") Integer invoice);

    @DeleteMapping("/remove-payment/{id}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Remove payment", description = "Remove payment by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PAYMENT_SUCCESSFULLY_REMOVED"),
            @ApiResponse(responseCode = "400", description = "PAYMENT_NOT_FOUND"),
    })
    @CrossOrigin
    ResponseObject removePayment(HttpServletRequest request,@PathVariable("id") Integer id);

    @GetMapping("/get-detailed-info/{paymentId}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Repeat payment", description = "Repeat payment by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "PAYMENT_NOT_FOUND"),
    })
    @CrossOrigin
    PaymentRepeatDto repeatEvent(HttpServletRequest request, @PathVariable("paymentId") Integer paymentId);


    @PostMapping("/change-status")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Change payment status", description = "Change payment status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "PAYMENT_NOT_FOUND"),
    })
    @CrossOrigin
    ResponseObject changeStatus(HttpServletRequest request, @RequestBody PaymentChangeStatusDto paymentChangeStatusDto);
}
