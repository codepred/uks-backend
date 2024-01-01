package codepred.common;



import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DateUtil {

    public static Date convertStringToDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date convertedDate = null;

        try {
            convertedDate = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return convertedDate;
    }

    public static int getCurrentMonth() {
        LocalDate currentDate = LocalDate.now();
        Month currentMonth = currentDate.getMonth();
        int monthValue = currentMonth.getValue();
        return monthValue;
    }
}