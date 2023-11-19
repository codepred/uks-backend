package codepred.company;

import codepred.common.util.ResponseObject;
import codepred.company.dto.CompanyDto;
import codepred.company.dto.CompanyFullDataDto;
import codepred.company.dto.CreateCompanyRequest;
import codepred.company.dto.UpdateCompanyRequest;
import codepred.group.dto.GroupReadDto;
import codepred.meeting.dto.*;
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
@RequestMapping(value = "/tutor/companies/", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Api(value = "Tutor, part for companies", tags = "Tutor-companies")
@Tag(name = "Tutor-companies", description = "Tutor-companies API")
@CrossOrigin
public class CompanyControllerBean implements CompanyController {

    private final TutorService tutorService;
    private final CompanyService companyService;

    @Override
    public ResponseObject createCompany(HttpServletRequest request, CreateCompanyRequest requestCompanyToCreateDTO) {
        var tutor = tutorService.getTutorByToken(request);
        var response = companyService.createCompany(tutor, requestCompanyToCreateDTO);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public List<CompanyDto> getAllCompaniesByPage(HttpServletRequest request, int pageNumber) {
        var tutor = tutorService.getTutorByToken(request);
        return companyService.getAllCompaniesByPage(tutor, pageNumber);
    }

    @Override
    public List<CompanyDto> getAllCompanies(HttpServletRequest request) {
        var tutor = tutorService.getTutorByToken(request);
        return companyService.getAllCompanies();
    }

    @Override
    public List<CompanyDto> getAllUserCompanies(HttpServletRequest request) {
        var tutor = tutorService.getTutorByToken(request);
        return companyService.getAllUserCompanies(tutor);
    }

    @Override
    public CompanyFullDataDto getCertainCompany(HttpServletRequest request, Integer companyId) {
        var tutor = tutorService.getTutorByToken(request);
        return companyService.getCertainCompany(tutor, companyId);
    }

    @Override
    public ResponseObject updateCompany(HttpServletRequest request, UpdateCompanyRequest requestCompanyToUpdateDTO) {
        var tutor = tutorService.getTutorByToken(request);
        var response = companyService.updateCompany(tutor, requestCompanyToUpdateDTO);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public List<CompanyDto> findCompaniesByName(HttpServletRequest request, String companyName) {
        var tutor = tutorService.getTutorByToken(request);
        return companyService.findCompanyByName(tutor, companyName);
    }

    @Override
    public Integer countCompanyPages(HttpServletRequest request) {
        var tutor = tutorService.getTutorByToken(request);
        return companyService.getTotalPages(tutor);
    }

    @Override
    public List<GroupReadDto> getConnectedGroup(HttpServletRequest request, Integer companyId) {
        var tutor = tutorService.getTutorByToken(request);
        return companyService.getConnectedGroup(tutor, companyId);
    }

    @Override
    public ResponseObject removeCompany(HttpServletRequest request, Integer companyId) {
        var tutor = tutorService.getTutorByToken(request);
        var response = companyService.removeCompany(tutor, companyId);
        response.setToken(request.getHeader("Authorization").substring(7));
        return response;
    }

    @Override
    public List<MeetingReadDto> getMeetings(HttpServletRequest request, CompanyMeetingWithPagesDto meetingsDto) {
        var tutor = tutorService.getTutorByToken(request);
        return companyService.getMeetings(tutor, meetingsDto.getCompanyId(), meetingsDto.getStartTime(), meetingsDto.getEndTime(), meetingsDto.getPageNumber());
    }

    @Override
    public MeetingsDto getTotalSumAndPageSize(HttpServletRequest request, CompanyMeetingDto countPagesForMeetings) {
        var tutor = tutorService.getTutorByToken(request);
        return companyService.getTotalSumAndPageSize(tutor, countPagesForMeetings);
    }

    @Override
    public ResponseObject changeMeetingStatus(HttpServletRequest request, Integer eventId) {
        var tutor = tutorService.getTutorByToken(request);
        return companyService.changeMeetingStatus(tutor, eventId);
    }
}
