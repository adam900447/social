package adam.brooks.social.security;

import jakarta.servlet.http.HttpServletRequest;

public class RequestSecurityUtil {

    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "UNKNOWN";
        }

        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple comma-separated IPs. First one is client IP.
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "UNKNOWN";
        }
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "UNKNOWN";
    }
}
