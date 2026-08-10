package adam.brooks.social.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * Turns the "userId" attribute (set by JwtHandshakeInterceptor) into a
 * java.security.Principal, which is what convertAndSendToUser() and
 * @MessageMapping's Principal parameter rely on to identify "this socket
 * belongs to this user".
 */
public class UserPrincipalHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                       Map<String, Object> attributes) {
        String userId = (String) attributes.get("userId");
        return () -> userId; // Principal is a functional interface (getName())
    }
}
