package codepred.company;

import codepred.common.util.ResponseObject;
import codepred.company.dto.CompanyDto;
import codepred.company.dto.CompanyFullDataDto;
import codepred.company.dto.CreateCompanyRequest;
import codepred.company.dto.UpdateCompanyRequest;
import codepred.group.dto.GroupReadDto;
import codepred.meeting.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface CompanyController {

    @PostMapping("/create-company")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Create company", description = "Create company with connecting to admin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Created, permission of tutor changed to admin."),
            @ApiResponse(responseCode = "201", description = "Created, email sent to new admin."),
    })
    ResponseObject createCompany(HttpServletRequest request, @ModelAttribute CreateCompanyRequest CreateCompanyRequest);

    @GetMapping("/get-all-companies/{number}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get all companies for tutor", description = "Get companies for tutor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Created, permission of tutor changed to admin."),
            @ApiResponse(responseCode = "201", description = "Created, email sent to new admin."),
    })
    List<CompanyDto> getAllCompaniesByPage(HttpServletRequest request,@PathVariable("number") int pageNumber);

    @GetMapping("/get-all-companies")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get all companies for tutor", description = "Get companies for tutor.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Created, permission of tutor changed to admin."),
        @ApiResponse(responseCode = "201", description = "Created, email sent to new admin."),
    })
    List<CompanyDto> getAllCompanies(HttpServletRequest request);

    @GetMapping("/get-all-companies-raw")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get all companies for tutor", description = "Get companies for tutor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Created, permission of tutor changed to admin."),
            @ApiResponse(responseCode = "201", description = "Created, email sent to new admin."),
    })
    List<CompanyDto> getAllUserCompanies(HttpServletRequest request);

    @GetMapping("/get-certain-company/{companyId}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get certain company", description = "Loading certain company data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok"),
    })
    CompanyFullDataDto getCertainCompany(HttpServletRequest request, @PathVariable Integer companyId);

    @PutMapping("/update-company")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Update company data", description = "Updated company data with administrator")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Data successfully updated."),
            @ApiResponse(responseCode = "400", description = "Company not found."),
    })
    ResponseObject updateCompany(HttpServletRequest request, @ModelAttribute UpdateCompanyRequest UpdateCompanyRequest);

    @GetMapping("/find-company-by-name/{companyName}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Find company data", description = "Find company by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data successfully loaded."),
    })
    List<CompanyDto> findCompaniesByName(HttpServletRequest request, @PathVariable("companyName") String companyName);

    @GetMapping("/number-of-company-pages")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Number of pages of companies", description = "Returnes how many pages for get all method")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Number of pages."),
    })
    Integer countCompanyPages(HttpServletRequest request);


    @GetMapping("/get-connected-groups/{companyId}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get connected groups", description = "Get connected groups")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    List<GroupReadDto> getConnectedGroup(HttpServletRequest request, @PathVariable("companyId") Integer companyId);


    @DeleteMapping("remove-client/{id}")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Remove company", description = "Remove company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "COMPANY_SUCCESSFULLY_REMOVED"),
            @ApiResponse(responseCode = "400", description = "COMPANY_NOT_FOUND"),
    })
    ResponseObject removeCompany(HttpServletRequest request, @PathVariable("id") Integer companyId);

    @PostMapping("/meetings")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get meetings for certain company", description = "Get meetings for certain clients")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data loaded"),
            @ApiResponse(responseCode = "400", description = "Event not found")
    })
    List<MeetingReadDto> getMeetings(HttpServletRequest request, @RequestBody CompanyMeetingWithPagesDto meetingsDto);

    @PostMapping("/meetings-count")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "Get number of pages for meetings", description = "Get number of pages for meetings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data loaded")
    })
    MeetingsDto getTotalSumAndPageSize(HttpServletRequest request, @RequestBody CompanyMeetingDto ClientMeetingDto);

    @PutMapping("/change-meeting-status/{eventId}")
    ResponseObject changeMeetingStatus(HttpServletRequest request, @PathVariable("eventId") Integer eventId);
}
