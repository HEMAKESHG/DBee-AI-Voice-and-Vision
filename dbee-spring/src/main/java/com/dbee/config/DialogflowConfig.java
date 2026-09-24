package com.dbee.config;

import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class DialogflowConfig {
    @Bean
    SessionsClient sessionsClient() throws IOException {
        return SessionsClient.create(SessionsSettings.newBuilder().build());
    }
}
