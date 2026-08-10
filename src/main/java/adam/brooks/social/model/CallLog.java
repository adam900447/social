package adam.brooks.social.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "call_logs")
public class CallLog {

    @Id
    private String id;

    private String callerId;
    private String calleeId;

    private CallType type = CallType.VOICE;
    private CallStatus status = CallStatus.MISSED;

    private LocalDateTime startedAt = LocalDateTime.now();
    private LocalDateTime endedAt;

    public enum CallType { VOICE, VIDEO }
    public enum CallStatus { MISSED, ANSWERED, DECLINED }
}
