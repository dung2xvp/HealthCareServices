package org.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * EmailService - Gửi email cho user
 * Chức năng:
 * 1. Gửi email verification khi đăng ký
 * 2. Gửi email reset password
 * 3. Gửi email thông báo (appointment confirmed, cancelled...)
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Gửi email xác thực sau khi đăng ký
     */
    public void sendVerificationEmail(String toEmail, String hoTen, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Xác thực tài khoản - HealthCare Booking");
        message.setText(String.format(
            "Xin chào %s,\n\n" +
            "Cảm ơn bạn đã đăng ký tài khoản tại HealthCare Booking.\n\n" +
            "Mã xác thực của bạn là: %s\n\n" +
            "Mã này sẽ hết hạn sau 15 phút.\n\n" +
            "Nếu bạn không thực hiện đăng ký này, vui lòng bỏ qua email này.\n\n" +
            "Trân trọng,\n" +
            "HealthCare Booking Team",
            hoTen, verificationCode
        ));
        
        mailSender.send(message);
        System.out.println("Sent verification email to: " + toEmail);
    }

    /**
     * Gửi lại mã xác thực
     */
    public void resendVerificationEmail(String toEmail, String hoTen, String verificationCode) {
        sendVerificationEmail(toEmail, hoTen, verificationCode);
    }

    /**
     * Gửi email reset password
     */
    public void sendPasswordResetEmail(String toEmail, String hoTen, String resetCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Đặt lại mật khẩu - HealthCare Booking");
        message.setText(String.format(
            "Xin chào %s,\n\n" +
            "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.\n\n" +
            "Mã đặt lại mật khẩu của bạn là: %s\n\n" +
            "Mã này sẽ hết hạn sau 15 phút.\n\n" +
            "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.\n\n" +
            "Trân trọng,\n" +
            "HealthCare Booking Team",
            hoTen, resetCode
        ));
        
        mailSender.send(message);
        System.out.println("Sent password reset email to: " + toEmail);
    }

    /**
     * Gửi email thông báo lịch đã được xác nhận
     */
    public void sendAppointmentConfirmedEmail(String toEmail, String hoTen, String ngayKham, String gioKham, String bacSi) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Lịch khám đã được xác nhận - HealthCare Booking");
        message.setText(String.format(
            "Xin chào %s,\n\n" +
            "Lịch khám của bạn đã được xác nhận.\n\n" +
            "Thông tin lịch khám:\n" +
            "- Bác sĩ: %s\n" +
            "- Ngày khám: %s\n" +
            "- Giờ khám: %s\n\n" +
            "Vui lòng đến đúng giờ. Nếu cần hủy lịch, vui lòng thông báo trước ít nhất 2 giờ.\n\n" +
            "Trân trọng,\n" +
            "HealthCare Booking Team",
            hoTen, bacSi, ngayKham, gioKham
        ));
        
        mailSender.send(message);
        System.out.println("Sent appointment confirmed email to: " + toEmail);
    }
}

