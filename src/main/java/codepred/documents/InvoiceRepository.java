package codepred.documents;

import io.swagger.models.auth.In;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Integer> {

    @Query("SELECT i.id FROM InvoiceEntity i " +
        "WHERE YEAR(i.date) = :year AND MONTH(i.date) = :month")
    List<Integer> getAllIdsByMonthAndYear(@Param("month") int month, @Param("year") int year);

}