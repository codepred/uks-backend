package codepred.documents;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query(value = "SELECT MAX(p.uks_file_number) FROM product p " + "INNER JOIN invoices_products ip ON p.id = ip.products_id "
        + "INNER JOIN invoices i ON ip.invoice_entity_id = i.id " + "WHERE i.customer_name = :clientName", nativeQuery = true)
    Integer getLastUksFileNumberByClientName(@Param("clientName") String clientName);

    @Query(value = "SELECT * FROM product p INNER JOIN invoices_products ip ON p.id = ip.products_id WHERE ip.invoice_entity_id IN :invoiceIds", nativeQuery = true)
    List<Product> getAllProductsByInvoiceIds(@Param("invoiceIds") List<Integer> invoiceIds);
}
