package com.corecompass.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendOtpEmail(String toEmail, String userName, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "CoreCompass");
            helper.setTo(toEmail);
            helper.setSubject("Your CoreCompass Password Reset OTP");
            helper.setText(buildOtpEmailHtml(userName, otp), true);

            mailSender.send(message);
            log.info("OTP email sent to: {}", toEmail);

        } catch (Exception ex) {
            // Don't throw — async fire-and-forget; caller already returned 200
            log.error("Failed to send OTP email to {}: {}", toEmail, ex.getMessage());
        }
    }

    private String buildOtpEmailHtml(String name, String otp) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" bgcolor="#f5f5f5">
                <tr><td align="center" style="padding:40px 0;">
                  <table width="480" cellpadding="0" cellspacing="0"
                         style="background:#ffffff;border-radius:12px;overflow:hidden;
                                box-shadow:0 2px 12px rgba(0,0,0,0.08);">
                    <!-- Header -->
                    <tr>
                      <td align="center" bgcolor="#111827"
                          style="padding:32px;color:#ffffff;">
                        <h1 style="margin:0;font-size:24px;letter-spacing:-0.5px;">
                          CoreCompass
                        </h1>
                        <p style="margin:8px 0 0;font-size:14px;color:#9ca3af;">
                          Password Reset
                        </p>
                      </td>
                    </tr>
                    <!-- Body -->
                    <tr>
                      <td style="padding:40px 48px;">
                        <p style="color:#374151;font-size:16px;margin:0 0 8px;">
                          Hi <strong>%s</strong>,
                        </p>
                        <p style="color:#6b7280;font-size:14px;margin:0 0 32px;">
                          Use the OTP below to reset your password.
                          It expires in <strong>15 minutes</strong>.
                        </p>
                        <!-- OTP Box -->
                        <div style="text-align:center;margin:0 0 32px;">
                          <div style="display:inline-block;background:#f9fafb;
                                      border:2px dashed #e5e7eb;border-radius:12px;
                                      padding:24px 48px;">
                            <span style="font-size:40px;font-weight:700;letter-spacing:12px;
                                         color:#111827;font-family:monospace;">
                              %s
                            </span>
                          </div>
                        </div>
                        <p style="color:#9ca3af;font-size:12px;margin:0;text-align:center;">
                          If you didn't request this, you can safely ignore this email.
                          <br>Your password will not change.
                        </p>
                      </td>
                    </tr>
                    <!-- Footer -->
                    <tr>
                      <td bgcolor="#f9fafb" style="padding:20px 48px;border-top:1px solid #e5e7eb;">
                        <p style="color:#9ca3af;font-size:11px;margin:0;text-align:center;">
                          © 2026 CoreCompass · Do not reply to this email
                        </p>
                      </td>
                    </tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(name, otp);
    }
}