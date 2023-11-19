package codepred.group.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class GroupDto {
    private Long groupId;
    private String groupName;
    private String color;
    private String linkToMeeting;
    private String folderLink;
    private byte[] groupPhoto;
    private String additionalInformation;

    private Long companyId;
    private String companyName;

    private Long serviceId;
    private Double price;
    private String serviceName;
    private Integer serviceDuration;
    private String timeUnit;


}
