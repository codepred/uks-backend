package codepred.payment;

import codepred.account.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    @Query(value = "SELECT * FROM payment WHERE user_id=:userId order by id desc ", nativeQuery = true)
    List<Payment> getByUser(@Param("userId") Integer userId);

    @Query(value = "select users.id from users " +
            "join user_clients uc " +
            "on users.id = uc.client_id " +
            "where uc.user_id=:tutorId", nativeQuery = true)
    List<Integer> getClientIds(@Param("tutorId") Integer tutorId);

    @Query(value = "select groups.id, groups.group_name from groups join users_groups ug on groups.id = ug.groups_id where ug.user_id=:tutorId", nativeQuery = true)
    List<Object[]> getAllGroups(@Param("tutorId") Integer tutorId);

    @Query(value = "select companies.id, companies.name from companies join users_companies uc on companies.id = uc.companies_id where uc.users_id=:tutorId", nativeQuery = true)
    List<Object[]> getAllCompanies(@Param("tutorId") Integer tutorId);

    @Query("SELECT p FROM Payment p LEFT JOIN FETCH p.invoices WHERE p.id = :paymentId")
    Optional<Payment> findByIdWithInvoices(@Param("paymentId") Integer paymentId);

    @Query(value = "select p.id from payment p join users u on p.user_id = u.id where p.id=:paymentId and u.id=:tutorId", nativeQuery = true)
    Optional<Integer> findPaymentByIdAndTutor(@Param("paymentId") Integer paymentId, Integer tutorId);

    @Modifying
    @Query(value = "delete from invoice where payment_id=:paymentId", nativeQuery = true)
    void removeInvoiceByPayment(@Param("paymentId") Integer paymentId);

    @Modifying
    @Query(value = "delete from payment where id=:paymentId", nativeQuery = true)
    void removePaymentById(@Param("paymentId") Integer paymentId);

    @Query(value = "select * from payment join invoice i on payment.id = i.payment_id where payment.id=:paymentId", nativeQuery = true)
    Optional<Payment> findPaymentById(@Param("paymentId") Integer paymentId);

    Optional<Payment> findByIdAndUser(@Param("id") Integer id, User user);

    @Query(value = "select * from payment p where buyer_id=:clientId and p.buyer_type='STUDENT'", nativeQuery = true)
    List<Payment> findByClientId(@Param("clientId") Integer clientId);

    @Query(value = "select * from payment p where buyer_id=:clientId and p.buyer_type='COMPANY'", nativeQuery = true)
    List<Payment> findByCompanyId(@Param("clientId") Integer clientId);
}
