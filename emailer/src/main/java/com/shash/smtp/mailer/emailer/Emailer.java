package com.shash.smtp.mailer.emailer;

import com.google.common.base.Strings;
import com.shash.smtp.mailer.emailer.config.EmailConfig;
import com.shash.smtp.mailer.emailer.config.SmtpConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author by shashank.g
 */
@AllArgsConstructor
@Builder
@Slf4j
public class Emailer {

    private static final String DEFAULT_CONTENT_TYPE = "text/plain";
    private static final String EMAIL_ATTACHMENT_ERROR_MSG = "Note: This email is missing configured email attachments " +
            "as sending attachments in email is disabled in the Server. ";

    private SmtpConfig smtpConfig;

    /**
     * Sends email
     *
     * @param emailConfig ::
     */
    public void email(final EmailConfig emailConfig) {
        final String from = emailConfig.getFrom();
        final List<String> tos = emailConfig.getTos();
        final List<String> ccs = emailConfig.getCcs();
        final List<String> bccs = emailConfig.getBccs();
        final String subject = emailConfig.getSubject();
        final String body = emailConfig.getBody();
        final List<String> attachments = emailConfig.getAttachments();
        final String contentType = Strings.isNullOrEmpty(emailConfig.getContentType()) ? DEFAULT_CONTENT_TYPE : emailConfig.getContentType();

        try {
            final Properties properties = createProps();
            final Session session = createSession(properties);
            final Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipients(Message.RecipientType.TO, tos.stream().map(this::getInternetAddress).collect(Collectors.toList()).toArray(new InternetAddress[0]));
            message.addRecipients(Message.RecipientType.CC, ccs.stream().map(this::getInternetAddress).collect(Collectors.toList()).toArray(new InternetAddress[0]));
            message.addRecipients(Message.RecipientType.BCC, bccs.stream().map(this::getInternetAddress).collect(Collectors.toList()).toArray(new InternetAddress[0]));
            message.setSubject(subject);

            if (attachments != null && attachments.size() > 0 && smtpConfig.isAttachmentEnabled()) {
                final Multipart multipart = new MimeMultipart();
                final MimeBodyPart bodyTextPart = new MimeBodyPart();
                bodyTextPart.setText(body);
                multipart.addBodyPart(bodyTextPart);
                attachments.forEach(attachment -> {
                    try {
                        final URI attachUri = new URI(attachment);
                        if (attachUri.getScheme() != null && attachUri.getScheme().equals("file")) {
                            throw new RuntimeException("Encountered an error when attaching a file. A local file cannot be attached:");
                        }
                        final MimeBodyPart messageBodyPart = new MimeBodyPart();
                        messageBodyPart.setFileName(new File(attachment).getName());
                        multipart.addBodyPart(messageBodyPart);
                    } catch (final MessagingException | URISyntaxException e) {
                        log.error("Unexpected error while adding the attachment to mail - {},", e.getMessage(), e);
                        log.info("Anyway, Trying to send email without attachment...");
                        throw new RuntimeException(e);
                    }
                });
                message.setContent(multipart);
                message.setContent(body, contentType);
            } else if (attachments != null && attachments.size() > 0 && !smtpConfig.isAttachmentEnabled()) {
                log.warn("Attachment addition is disabled from server side. Sending email without attachment");
                message.setContent(body + "\n\n" + EMAIL_ATTACHMENT_ERROR_MSG, contentType);
            }

            // sendEmail
            Transport.send(message);
        } catch (final MessagingException e) {
            log.error("Unable to send mail - {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        log.debug("Email sent for - {}", emailConfig);
    }

    private InternetAddress getInternetAddress(final String address) {
        try {
            return new InternetAddress(address);
        } catch (final AddressException e) {
            log.error("Unable to get internet address - {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private Session createSession(final Properties properties) {
        return !smtpConfig.getAuthEnabled()
                ? Session.getInstance(properties)
                : Session.getInstance(properties, new JavaMailAuthenticator(smtpConfig.getUser(), smtpConfig.getPassword()));
    }

    private Properties createProps() {
        final Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", smtpConfig.getHost());
        properties.setProperty("mail.smtp.port", String.valueOf(smtpConfig.getPort()));
        properties.setProperty("mail.smtp.auth", String.valueOf(smtpConfig.getAuthEnabled()));
        properties.setProperty("mail.smtp.connectiontimeout", String.valueOf(smtpConfig.getTimeoutMillis()));
        properties.setProperty("mail.smtp.timeout", String.valueOf(smtpConfig.getTimeoutMillis()));
        properties.setProperty("mail.smtp.writetimeout", String.valueOf(smtpConfig.getTimeoutMillis()));
        return properties;
    }
}
