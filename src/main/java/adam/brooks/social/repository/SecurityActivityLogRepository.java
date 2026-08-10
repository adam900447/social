package adam.brooks.social.repository;

import adam.brooks.social.model.SecurityActivityLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SecurityActivityLogRepository extends MongoRepository<SecurityActivityLog, String> {
    List<SecurityActivityLog> findByUserIdOrderByTimestampDesc(String userId);
}
