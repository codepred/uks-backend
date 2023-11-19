package codepred.service;

import codepred.account.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Integer> {
    Optional<Service> findServiceById(Integer serviceId);
    @Query("SELECT s FROM User u JOIN u.services s WHERE u.id = :userId and s.isDeleted=FALSE order by s.serviceName")
    List<Service> findServiceByUserId(@Param("userId") Integer tutorId);

    @Query("SELECT s FROM User u JOIN u.services s WHERE s.id = :serviceId AND u = :user and s.isDeleted=false")
    Optional<Service> findServiceByIdAndUser(@Param("serviceId") Integer serviceId, @Param("user") User user);

    @Query("SELECT count(u) FROM User u JOIN u.services s WHERE s.id = :serviceId")
    Long countClientsByServiceId(@Param("serviceId") Integer serviceId);

    void removeById(Integer serviceId);
}
