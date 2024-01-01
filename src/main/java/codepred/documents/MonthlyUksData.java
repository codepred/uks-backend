package codepred.documents;


import java.util.List;
import lombok.Data;

@Data
public class MonthlyUksData {

    private List<Integer> productList;
    private Integer month;
    private Integer year;

}
