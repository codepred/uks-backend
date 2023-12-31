package codepred.documents;


import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class InvoiceService {

    public List<Product> addDuplicatedProducts(List<Product> productList) {
        List<Product> updatedProductList = new ArrayList<>();

        for (Product product : productList) {
            int quantity = Integer.parseInt(product.getAmount());
            if (quantity > 1) {
                for (int i = 0; i < quantity; i++) {
                    Product duplicateProduct = new Product();
                    duplicateProduct.setName(product.getName());
                    duplicateProduct.setAmount("1");
                    duplicateProduct.setPrice(product.getPrice());
                    duplicateProduct.setTotalPrice(product.getTotalPrice());
                    updatedProductList.add(duplicateProduct);
                }
            } else {
                updatedProductList.add(product);
            }
        }

        return updatedProductList;
    }

}
