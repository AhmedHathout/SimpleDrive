package com.ahmedhathout.SimpleDrive.services;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;

@Service
@AllArgsConstructor
public class EmailService {

    @Autowired
    private final JavaMailSender javaMailSender;

    @Async
    public void sendEmail(SimpleMailMessage simpleMailMessage) {
        javaMailSender.send(simpleMailMessage);
    }

    public void sendEmailWithAttachment(
            String to, String subject, String text, String attachmentName, String pathToAttachment) throws MessagingException {

        MimeMessage message = javaMailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);

        FileSystemResource file
                = new FileSystemResource(new File(pathToAttachment));
        helper.addAttachment(attachmentName, file);

        javaMailSender.send(message);
    }
}
