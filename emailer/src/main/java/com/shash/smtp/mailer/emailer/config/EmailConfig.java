package com.shash.smtp.mailer.emailer.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * @author by shashank.g
 */
@AllArgsConstructor
@Builder
@Getter
@ToString
public class EmailConfig {
    private String from;
    private String subject;
    private String body;
    private List<String> tos;
    private List<String> ccs;
    private List<String> bccs;
    private List<String> attachments;
    private String contentType;
}
