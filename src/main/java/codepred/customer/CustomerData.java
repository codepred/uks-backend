package codepred.customer;

import codepred.client.dto.DetailsPerTutorDto;
import codepred.group.dto.GroupNameDto;
import codepred.tutor.dto.TutorIdAndNameDto;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@ToString
@Getter
@Setter
@NoArgsConstructor(force = true)
public class CustomerData {

    private List<DetailsPerTutorDto> detailsPerTutorDtos;
    private List<TutorIdAndNameDto> tutorList;
    private List<GroupNameDto> groupList;

}
