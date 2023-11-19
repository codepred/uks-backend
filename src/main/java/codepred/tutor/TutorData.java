package codepred.tutor;

import codepred.bookmark.dto.BookmarkDto;
import codepred.meeting.dto.MeetingDto;
import codepred.payment.dto.PaymentDto;
import codepred.tutor.dto.TutorDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class TutorData {

    private TutorDto tutorData;
    private List<MeetingDto> meetingsData;
    private List<PaymentDto> paymentData;
    private List<BookmarkDto> bookmarkList;

}
