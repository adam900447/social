package adam.brooks.social.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5MB

    /**
     * Saves an uploaded avatar image to uploads/avatars/ on local disk and
     * returns the public URL path to store on the User document.
     */
    public String saveAvatar(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("No file uploaded");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("Image must be under 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPG, PNG, WEBP, or GIF images are allowed");
        }

        try {
            Path dir = Path.of("uploads", "avatars");
            Files.createDirectories(dir);

            String extension = switch (contentType) {
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                case "image/gif" -> ".gif";
                default -> ".jpg";
            };
            String filename = UUID.randomUUID() + extension;
            Path destination = dir.resolve(filename);

            file.transferTo(destination.toFile());

            return "/uploads/avatars/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save uploaded file", e);
        }
    }
}
