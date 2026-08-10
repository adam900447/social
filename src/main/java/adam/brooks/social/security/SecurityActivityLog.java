package adam.brooks.social.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * One entry in a user's own security/login activity log — mirrors what
 * Facebook calls "Where You're Logged In". This is only ever shown to the
 * account owner viewing their own history, never used as a hidden
 * cross-user tracking table.
 */
@Data
@Document(collection = "security_activity_logs")
public class SecurityActivityLog {

    @Id
    private String id;

    private String userId;
    private String action;      // e.g. "LOGIN", "REGISTER", "PASSWORD_CHANGE"
    private String ipAddress;
    private String userAgentRaw;

    // parsed from the user-agent string — human-readable, not a raw fingerprint
    private String browser;
    private String os;
    private String device;

    private String methodName;
    private LocalDateTime timestamp = LocalDateTime.now();
    private boolean success;
}
