package com.ecomm.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Simple email sender abstraction.
 * Currently logs emails; later you can wire JavaMailSender / SES / SendGrid.
 */
@Slf4j
@Service
public class EmailSender {

    /**
     * Send a simple plain-text email.
     */
    public void sendPlainText(String to, String subject, String body) {
        // TODO: integrate with real SMTP or provider
        log.info("EMAIL-PLAINTEXT → to={} | subject={} | body={}", to, subject, body);
    }

    /**
     * Send a template-based email.
     *
     * @param to          recipient email
     * @param template    template name/key
     * @param payloadJson JSON payload for template variables
     */
    public void sendTemplateEmail(String to, String template, String payloadJson) {
        // TODO: integrate with your template engine (Thymeleaf, Freemarker, etc.)
        log.info("EMAIL-TEMPLATE → to={} | template={} | payload={}", to, template, payloadJson);
    }
}
