package codepred.tutor;


import codepred.account.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TutorRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    @Query(value = "SELECT * from users WHERE code =:code", nativeQuery = true)
    Optional<User> findByRegistrationCode(String code);

    @Query(value = "SELECT u.id AS user_id, u.email, td.id AS tutor_id, td.activity_type, td.name, td.surname, td.phone_number, td.nip, td.regon, td.company_name, td.street, td.post_code, td.place, td.bank_name, td.bank_account_number, td.is_service_active, td.photo FROM users u JOIN tutor_details td ON u.tutor_details_id = td.id WHERE u.id = :tutorId", nativeQuery = true)
    List<Object[]> findTutorById(Integer tutorId);


    @Query(value = "select * from users as u join user_clients as uc on u.id=uc.user_id where uc.client_id=:customer", nativeQuery = true)
    List<User> findAllClientTutors(User customer);

    @Query(value = " select * from users u inner join users_events ue on ue.users_id = u.id where ue.events_id=:eventId", nativeQuery = true)
    List<User> findAllUserByEventId(@Param("eventId") Integer eventId);



    @Query(value = "select id from services join users_services us on services.id = us.services_id where us.users_id=:tutorId", nativeQuery = true)
    List<Integer> findIdOfServices(@Param("tutorId") Integer tutorId);

    @Query(value = "select count(*) from users where email=:email", nativeQuery = true)
    Integer countEmails(@Param("email") String email);


}
