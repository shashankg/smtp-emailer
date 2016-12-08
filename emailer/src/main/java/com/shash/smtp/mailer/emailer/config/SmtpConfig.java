package com.shash.smtp.mailer.emailer.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author by shashank.g
 */
@Data
@AllArgsConstructor
@Builder
@ToString
public class SmtpConfig {
    private String host;
    private Integer port;
    private Boolean authEnabled;
    private String user;
    private String password;
    private Integer timeoutMillis;
    private boolean attachmentEnabled;
}
