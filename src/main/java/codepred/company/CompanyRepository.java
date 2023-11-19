package codepred.company;

import codepred.account.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {

    List<Company> findCompaniesByUsers(User user);

    List<Company> findCompaniesByUsers(User user,Pageable pageable);

    @Query(value = "select * from companies c where c.name=:name", nativeQuery = true)
    Optional<Company> findCompanyByName(@Param("name") String name);
    
    @Query(value = "select * from companies c " +
            "join users_companies uc on c.id=uc.companies_id " +
            "where uc.users_id=:userId and c.id=:companyId", nativeQuery = true)
    Optional<Company> findCompanyByIdAndUsers(@Param("companyId") Integer companyId, @Param("userId") Integer user);

    @Query(value = "SELECT c.id as id, c.name as name, " +
            "COUNT(DISTINCT ug.user_id) FILTER (WHERE ug.user_id != :tutorId) as studentsnumber, " +
            "COUNT(DISTINCT g.id)  as groupnumber, " +
            "c.photo as photo " +
            "FROM companies c " +
            "LEFT JOIN groups g ON c.id = g.company_id " +
            "LEFT JOIN users_groups ug ON g.id = ug.groups_id " +
            "JOIN users_companies uc on c.id=uc.companies_id " +
            "where uc.users_id=:tutorId " +
            "GROUP BY c.id, c.name order by name",
            nativeQuery = true)
    List<Object[]> getAllCompanies(@Param("tutorId") Integer tutorId);

    @Query(value = "select c.id, c.name, c.address, c.postcode, c.place, c.nip, c.regon, " +
            // " td.name, td.surname, td.phone_number " +
            "c.is_available_to_cancel_reservation, c.is_available_to_change_reservation, " +
            "c.minimum_change_time, c.additional_info, c.photo " +
            "FROM companies c " +
            "JOIN users_companies uc ON c.id=uc.companies_id " +
            "JOIN users u ON uc.users_id=u.id " +
            "JOIN tutor_details td ON u.tutor_details_id=td.id " +
            "WHERE u.id=:tutorId " +
            "AND c.id=:companyId",
            nativeQuery = true)
    Object[] getCompanyByCompanyIdAndTutorId(@Param("companyId") Integer companyId, @Param("tutorId") Integer tutorId);


    @Query(value = "select u.id, td.name, td.surname, u.email, td.phone_number " +
            "from companies c " +
            "JOIN users_companies uc on c.id=uc.companies_id " +
            "JOIN users u on uc.users_id=u.id " +
            "JOIN tutor_details td on u.tutor_details_id=td.id " +
            "WHERE c.id=:companyId and u.app_user_roles=0",
            nativeQuery = true)
    Object getAdminInfo(@Param("companyId") Integer companyId);

    @Query(value = "SELECT c.id as id, c.name as name, " +
            "COUNT(DISTINCT ug.user_id) FILTER (WHERE ug.user_id != :tutorId) as studentsnumber, " +
            "COUNT(DISTINCT g.id)  as groupnumber, " +
            "c.photo as photo " +
            "FROM companies c " +
            "LEFT JOIN groups g ON c.id = g.company_id " +
            "LEFT JOIN users_groups ug ON g.id = ug.groups_id " +
            "JOIN users_companies uc on uc.companies_id=c.id " +
            "where lower(c.name) like lower(concat('%', :content, '%')) and uc.users_id=:tutorId " +
            "GROUP BY c.id, c.name order by name", nativeQuery = true)
    List<Object[]> findCompanyByNameAndTutorId(@Param("content") String content, Integer tutorId);


    @Query(value = "select * from companies c " +
            "join users_companies uc on c.id=uc.companies_id " +
            "where uc.companies_id=:companyId and uc.users_id=:tutorId", nativeQuery = true)
    Optional<Company> getCompanyByIdAndTutor(@Param("companyId") Integer companyId, @Param("tutorId") Integer tutorId);

    @Modifying
    @Query(value = "delete from users_companies uc where uc.users_id=:userId and uc.companies_id=:companyId", nativeQuery = true)
    void removeUserCompanyConnection(@Param("userId") Integer userId, @Param("companyId") Integer companyId);

    @Modifying
    @Query(value = "delete from companies_groups cg where cg.company_id=:companyId and cg.groups_id in " +
            "(select id from groups g join users_groups ug on g.id=ug.groups_id where ug.user_id=:userId)",
            nativeQuery = true)
    void removeGroupCompanyConnection(@Param("userId") Integer userId, @Param("companyId") Integer companyId);


    @Query(value = "select count(*) from events e \n" +
            "join groups g on g.id=e.group_id \n" +
            "join users_events ue on ue.events_id=e.id \n" +
            "join companies_groups cg on g.id=cg.groups_id\n" +
            "where ue.users_id=:tutorId and cg.company_id=:companyId"
            , nativeQuery = true)
    Integer countEventInTimeRangeForTutor(@Param("tutorId") Integer tutorId, @Param("companyId") Integer companyId);

    @Query(value = "SELECT COUNT(DISTINCT c.id) " +
            "FROM companies c " +
            "JOIN users_companies uc on c.id=uc.companies_id " +
            "WHERE uc.users_id=:tutorId",
            nativeQuery = true)
    Integer countCompaniesByTutorId(Integer tutorId);
}
