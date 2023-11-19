package codepred.documents;


import java.util.List;
import lombok.Data;

@Data
public class InvoiceData {

    private String username;
    private String date;
    private String name;
    private String street;
    private String aptNumber;
    private String zip;
    private String city;
    private List<Product> productList;
    private String paymentMethod;
    private String currency;
    private String signature;

}
