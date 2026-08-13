package br.com.serviceflow.api.config;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.mail.javamail.*;

@Configuration
public class MailConfig {
    @Bean
    JavaMailSender javaMailSender(@Value("${spring.mail.host:smtp.gmail.com}") String host,
            @Value("${spring.mail.port:587}") int port, @Value("${spring.mail.username:}") String username,
            @Value("${spring.mail.password:}") String password) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password.replaceAll("\\s", ""));
        Properties p = sender.getJavaMailProperties();
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.starttls.required", "true");
        p.put("mail.smtp.connectiontimeout", "5000");
        p.put("mail.smtp.timeout", "5000");
        p.put("mail.smtp.writetimeout", "5000");
        return sender;
    }
}
