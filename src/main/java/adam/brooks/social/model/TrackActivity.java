package adam.brooks.social.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Put this on a controller method to have SecurityTrackingAspect record an
 * entry in that user's own activity log — e.g. @TrackActivity(action = "LOGIN").
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackActivity {
    String action();
}
