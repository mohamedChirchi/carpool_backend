package com.sesame.carpool_backend.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
public class EmailSender {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public void sendOtpVerification(String email, String otp) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");

        try {
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setFrom("noreply@sesame-project.tn");
            mimeMessageHelper.setSubject("Verify OTP");

            Context context = new Context();
            context.setVariable("otpCode", otp);
            String htmlContent = templateEngine.process("otpcode", context);
            mimeMessageHelper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.out.println(e.getMessage());
        }

    }

    public void sendResetPassword(String email, String url) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");

        try {
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setFrom("noreply@sesame-project.tn");
            mimeMessageHelper.setSubject("Reset Password");

            Context context = new Context();
            context.setVariable("resetPasswordUrl", url);
            String htmlContent = templateEngine.process("forgotpassword", context);
            mimeMessageHelper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.out.println(e.getMessage());
        }

    }

}
