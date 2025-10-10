package com.securityProject.jwtAuthServer.service.email;


import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class EmailTemplateLoader {

    public String loadTemplate(String path, String verificationUrl) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/email/" + path);
            String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            return html.replace("{{link}}", verificationUrl);


        } catch (IOException e) {
            throw new RuntimeException("Failed to load email template: " + path, e);
        }
    }
}