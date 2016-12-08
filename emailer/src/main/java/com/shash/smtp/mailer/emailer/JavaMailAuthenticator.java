package com.shash.smtp.mailer.emailer;

import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * @author shashank.g
 */
@AllArgsConstructor
@Builder
public class JavaMailAuthenticator extends Authenticator {

    private String user;
    private String password;

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }
}