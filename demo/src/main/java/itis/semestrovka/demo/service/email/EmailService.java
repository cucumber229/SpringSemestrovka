package itis.semestrovka.demo.service.email;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
