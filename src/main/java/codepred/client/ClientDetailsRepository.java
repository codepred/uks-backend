package codepred.client;

import codepred.account.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClientDetailsRepository extends JpaRepository<ClientDetails, Integer> {

    @Query(value = "SELECT * FROM client_details cd where cd.tutor_id = :tutorId and cd.user_id = :clientId", nativeQuery = true)
    ClientDetails findByTutorIdAndClientId(int clientId, int tutorId);

    @Query(value = "SELECT * FROM client_details cd where cd.user_id = :clientId", nativeQuery = true)
    List<ClientDetails> findAllByClientId(int clientId);

    void deleteByUser(User client);



}
