package codepred.whitelist;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WhitelistRepo extends JpaRepository<Whitelist, Integer> {

    @Query(value = "select * from whitelist where email=:tutorEmail",
        nativeQuery = true)
    Whitelist getWhitelistByEmail(@Param("tutorEmail") String tutorEmail);

}
