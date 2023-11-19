package codepred.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookMarkRepository extends JpaRepository<BookMark, Integer> {

    @Query(value = "SELECT * FROM bookmark WHERE user_id=:userId", nativeQuery = true)
    List<BookMark> findAllByUser(@Param("userId") Integer userId);

    @Query(value = "SELECT * FROM bookmark WHERE user_id=:userId and type=:type", nativeQuery = true)
    BookMark getByTutorAndType(@Param("userId") Integer userId, @Param("type") String type);


}

