package adam.brooks.social.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async
    public void sendVerificationEmail(String toEmail, String username, String token) {
        try {
            String verificationLink = baseUrl + "/api/auth/verify?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Confirm your Adam account");
            message.setText(
                    "Hi " + username + ",\n\n" +
                    "Welcome to Adam! Please confirm your email address by clicking the link below:\n\n" +
                    verificationLink + "\n\n" +
                    "If you didn't create this account, you can safely ignore this email.\n\n" +
                    "— The Adam team"
            );

            mailSender.send(message);
        } catch (Exception e) {
            // Catches SMTP port blocks on Render without failing the user registration
            System.err.println("Failed to send verification email to " + toEmail + ": " + e.getMessage());
        }
    }
}
