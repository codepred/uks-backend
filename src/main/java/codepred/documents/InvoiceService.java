package codepred.documents;


import static codepred.common.DateUtil.convertStringToDate;

import codepred.common.FileService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final FileService fileService;

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;

    public InvoiceEntity saveInvoice(InvoiceData invoiceData) {
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setUsername(invoiceData.getUsername());
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setProducts(invoiceData.getProductList());
        invoice.setEmail(invoiceData.getEmail());
        invoice.setPaymentMethod(invoiceData.getPaymentMethod());
        invoice.setCurrency(invoiceData.getCurrency());
        invoice.setName(invoiceData.getName());
        invoice.setDate(convertStringToDate(invoiceData.getDate()));
        return invoiceRepository.save(invoice);
    }

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
                    updatedProductList.add(duplicateProduct);
                }
            } else {
                updatedProductList.add(product);
            }
        }
        return updatedProductList;
    }

    public List<Product> getAllInvoices(PeriodData periodData){
        final var invoices = invoiceRepository.getAllIdsByMonthAndYear(periodData.getMonth(), periodData.getYear());
        final var products = productRepository.getAllProductsByInvoiceIds(invoices);
        return products;
    }

    public String deleteProduct(Integer id){
        Product product = productRepository.getById(id);
        fileService.deleteFile(product.getUksPath());
        productRepository.delete(product);
        return "Product was deleted";
    }


}
