package adam.brooks.social.config;

import adam.brooks.social.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * The frontend connects like:
 *   const socket = new SockJS('/ws?token=' + jwtToken);
 *
 * This runs once during the initial HTTP handshake (before it upgrades
 * to WebSocket) and stores the authenticated user id in the WebSocket
 * session attributes, so ChatSocketController can trust it later.
 */
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");

            if (token != null && jwtUtil.isTokenValid(token)) {
                attributes.put("userId", jwtUtil.extractUserId(token));
                attributes.put("username", jwtUtil.extractUsername(token));
                return true;
            }
        }
        // reject the handshake if there's no valid token
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
