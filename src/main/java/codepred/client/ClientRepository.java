package codepred.client;

import codepred.account.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<User, Integer> {

    @Query(value = "WITH client_details_for_given_tutor AS (SELECT * FROM client_details WHERE tutor_id = :tutorId)\n" +
            "SELECT * FROM users u JOIN client_details_for_given_tutor cd ON cd.user_id = u.id", nativeQuery = true)
    List<User> findByUsersId(@Param("tutorId") Integer tutorId);

    @Query(value = "SELECT * FROM users u JOIN user_clients uc ON u.id = uc.client_id WHERE uc.user_id = :tutorId AND uc.client_id = :clientId", nativeQuery = true)
    Optional<User> findByTutorIdAndClientId(@Param("tutorId") Integer tutorId, @Param("clientId") Integer clientId);

    @Query(value = "WITH client_details_for_given_tutor AS (SELECT * FROM client_details WHERE tutor_id = :tutorId)\n" +
            "SELECT * FROM users u JOIN client_details_for_given_tutor cd ON cd.user_id = u.id WHERE app_user_roles=1",
            countQuery = "WITH client_details_for_given_tutor AS (SELECT * FROM client_details WHERE tutor_id = :tutorId)\n" +
                    "SELECT COUNT(*) FROM users u JOIN client_details_for_given_tutor cd ON cd.user_id = u.id",
            nativeQuery = true)
    Page<User> findClientsByTutor(@Param("tutorId") Integer tutorId, Pageable pageable);

    Optional<User> findClientByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.clientDetails c WHERE c.name = :name AND c.lastname = :lastName AND u.email = :email")
    Optional<User> findClientByNameAndLastname(@Param("name") String name, @Param("lastName") String lastName, @Param("email") String email);

    @Query("SELECT COUNT(c) FROM User u JOIN u.clients c WHERE u.id = :userId and c.appUserRoles = 1")
    long countByUsersId(@Param("userId") Integer tutorId);

    @Query(value = "select * from users " +
            "join client_details cd on cd.user_id = users.id " +
            "join user_clients uc on users.id = uc.client_id " +
            "left join users_companies u on users.id = u.users_id " +
            "left join companies c on u.companies_id = c.id " +
            "left join users_groups ug on users.id = ug.user_id " +
            "left join groups g on ug.groups_id=g.id " +
            "where " +
            "uc.user_id =:tutorId and " +
            "(" +
            "lower(cd.name) like lower(concat('%', :name, '%')) or " +
            "lower(cd.lastname) like lower(concat('%', :name, '%')) or " +
            "lower(g.group_name) like lower(concat('%', :name, '%'))" +
            ")", nativeQuery = true)
    List<User> findClientByContent(@Param("name") String name, @Param("tutorId") Integer tutorId);

    @Query(value = "select u.id as client_id, cd.client_photo, cd.name, cd.lastname, u.email, cd.phone_number, cd.meeting_link, cd.storage_link, cd.additional_information, " +
            "s.id, s.service_name, s.service_duration, s.time_unit, s.price, cd.is_available_to_change_reservation, cd.is_available_to_cancel_reservation, " +
            "cd.minimum_change_time,  groups.id as group_id, groups.group_name, cd.nip, cd.regon, cd.color, cd.is_business_invoice, " +
            "cd.province, cd.postcode, cd.street_and_number " +
            " from users u " +
            "join client_details cd on cd.user_id = u.id " +
            "left join users_services us on us.users_id=u.id " +
            "join user_clients uc on u.id = uc.client_id " +
            "left join users_groups on u.id = users_groups.user_id " +
            "left join groups on users_groups.groups_id = groups.id " +
            "left join services s on us.services_id = s.id " +
            "where u.id=:clientId and uc.user_id=:tutorId", nativeQuery = true)
    List<Object[]> findCertainClientByIdAndTutor(@Param("clientId") Integer clientId, @Param("tutorId") Integer tutorId);

    @Query(value = "select count(*) from user_clients where user_clients.client_id=:clientId",
            nativeQuery = true)
    Integer countTutorsToClient(@Param("clientId") Integer clientId);

    @Modifying
    @Query(value = "delete from users_groups where user_id=:clientId", nativeQuery = true)
    void removeClientGroupConnection(@Param("clientId") Integer clientId);

    @Modifying
    @Query(value = "delete from users_services where users_id=:clientId", nativeQuery = true)
    void removeServiceClientConnection(@Param("clientId") Integer clientId);

    @Modifying
    @Query(value = "INSERT INTO user_clients (user_id , client_id) VALUES (:tutorId, :clientId)", nativeQuery = true)
    @Transactional
    void connectClientToTutor(@Param("tutorId") Integer tutorId,  @Param("clientId") Integer clientId);

    Optional<User> findUserById(Integer id);

    @Query(value = "SELECT * FROM users WHERE id=:id and app_user_roles=1", nativeQuery = true)
    Optional<User> findStudentById(Integer id);

    Optional<User> findByEmail(String email);
}
