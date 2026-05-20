package pki_project.pki.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendCAUserActivationEmail(String email, String token) {
        String activationLink = frontendUrl + "/activate-ca-user?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Aktivacija CA korisničkog naloga");
        message.setText(
                "Vaš CA nalog je kreiran.\n\n" +
                        "Kliknite na link da postavite lozinku:\n" +
                        activationLink + "\n\n" +
                        "Link važi 24 sata."
        );

        mailSender.send(message);
    }
}
