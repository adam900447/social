package adam.brooks.social.security;

import adam.brooks.social.model.SecurityActivityLog;
import adam.brooks.social.repository.SecurityActivityLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ua_parser.Client;
import ua_parser.Parser;

import java.lang.reflect.Method;

/**
 * Records an entry in the CALLING USER'S OWN activity log whenever an
 * @TrackActivity-annotated method runs. This is intentionally scoped to
 * per-user, self-visible history (like Facebook's "Where You're Logged In")
 * — not a cross-user surveillance table. See UserController's
 * /api/users/me/security-log for how it's exposed back to the owner.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class SecurityTrackingAspect {

    private final SecurityActivityLogRepository securityActivityLogRepository;
    private final Parser userAgentParser = new Parser();

    @Around("@annotation(adam.brooks.social.security.TrackActivity)")
    public Object logSecurityActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        TrackActivity annotation = method.getAnnotation(TrackActivity.class);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = (attributes != null) ? attributes.getRequest() : null;

        String clientIp = RequestSecurityUtil.getClientIpAddress(request);
        String rawUserAgent = RequestSecurityUtil.getUserAgent(request);

        String userId = getCurrentUserId();
        String action = annotation.action();
        String methodName = signature.getDeclaringType().getSimpleName() + "." + method.getName();

        boolean success = false;
        try {
            Object result = joinPoint.proceed();
            success = true;
            return result;
        } finally {
            saveLogEntry(userId, action, clientIp, rawUserAgent, methodName, success);
        }
    }

    private void saveLogEntry(String userId, String action, String clientIp, String rawUserAgent,
                               String methodName, boolean success) {
        try {
            SecurityActivityLog entry = new SecurityActivityLog();
            entry.setUserId(userId);
            entry.setAction(action);
            entry.setIpAddress(clientIp);
            entry.setUserAgentRaw(rawUserAgent);
            entry.setMethodName(methodName);
            entry.setSuccess(success);

            if (rawUserAgent != null && !"UNKNOWN".equals(rawUserAgent)) {
                Client client = userAgentParser.parse(rawUserAgent);
                entry.setBrowser(client.userAgent.family);
                entry.setOs(client.os.family);
                entry.setDevice(client.device.family);
            }

            securityActivityLogRepository.save(entry);
        } catch (Exception e) {
            // logging must never break the actual request it's wrapping
            System.err.println("Failed to save security activity log: " + e.getMessage());
        }
    }

    /**
     * Login/register happen before a JWT exists, so there's no authenticated
     * principal yet for those — falls back to "anonymous" in that case.
     * Once JwtAuthFilter has set the principal (any request after login),
     * this returns the real user id.
     */
    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;
        return (principal instanceof String id) ? id : "anonymous";
    }
}
