package adam.brooks.social.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Serves files from a local "uploads" folder (created next to your project,
 * NOT inside src/) at the URL path /uploads/**.
 *
 * Example: a file saved to uploads/avatars/abc123.jpg becomes reachable at
 * http://localhost:8181/uploads/avatars/abc123.jpg
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadsPath = new File("uploads").getAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsPath + File.separator);
    }
}
