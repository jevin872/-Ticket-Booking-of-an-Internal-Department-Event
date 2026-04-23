package com.ticketbooking.service;

import com.ticketbooking.model.entity.Booking;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name}")
    private String appName;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async
    public void sendVerificationEmail(String to, String name, String token) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name", name);
            ctx.setVariable("verificationUrl", baseUrl + "/api/auth/verify-email?token=" + token);
            ctx.setVariable("appName", appName);

            String html = templateEngine.process("email/verification", ctx);
            sendHtmlEmail(to, "Verify Your Email - " + appName, html);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String name, String token) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name", name);
            ctx.setVariable("resetUrl", baseUrl + "/api/auth/reset-password?token=" + token);
            ctx.setVariable("appName", appName);

            String html = templateEngine.process("email/password-reset", ctx);
            sendHtmlEmail(to, "Password Reset Request - " + appName, html);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendBookingConfirmation(String to, String name, Booking booking) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name", name);
            ctx.setVariable("booking", booking);
            ctx.setVariable("appName", appName);

            String html = templateEngine.process("email/booking-confirmation", ctx);
            sendHtmlEmail(to, "Booking Confirmed: " + booking.getBookingReference(), html);
        } catch (Exception e) {
            log.error("Failed to send booking confirmation to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendCancellationEmail(String to, String name, Booking booking) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name", name);
            ctx.setVariable("booking", booking);
            ctx.setVariable("appName", appName);

            String html = templateEngine.process("email/booking-cancellation", ctx);
            sendHtmlEmail(to, "Booking Cancelled: " + booking.getBookingReference(), html);
        } catch (Exception e) {
            log.error("Failed to send cancellation email to {}: {}", to, e.getMessage());
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
        log.info("Email sent to: {}", to);
    }
}
