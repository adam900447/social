package adam.brooks.social.repository;

import adam.brooks.social.model.CallLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CallLogRepository extends MongoRepository<CallLog, String> {
}
