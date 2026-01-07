package com.team44.isa_youtubeich.service.impl;

import com.team44.isa_youtubeich.domain.model.User;
import com.team44.isa_youtubeich.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Override
    public void sendActivationEmail(User user, String activationToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Jutjubić - Aktivacija naloga");
        message.setText(buildActivationEmailText(user, activationToken));
        message.setFrom(mailUsername);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send activation email: " + e.getMessage());
        }
    }

    private String buildActivationEmailText(User user, String activationToken) {
        String activationUrl = baseUrl + "/auth/activate?token=" + activationToken;

        return String.format(
                "Poštovani,\n\n" +
                        "Hvala Vam što ste se registrovali na našoj aplikaciji.\n\n" +
                        "Molimo Vas da kliknete sledeći link kako biste aktivirali Vaš nalog:\n" +
                        "%s\n\n" +
                        "Ovaj link će isteći u 24 časa.\n\n" +
                        "Ukoliko niste registrovali nalog, ignorišite ovaj mejl.\n\n" +
                        "Sportski pozdrav,\n" +
                        "Tim 44",
                activationUrl
        );
    }
}

