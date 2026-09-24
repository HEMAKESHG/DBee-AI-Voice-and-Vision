package com.dbee.service;

import com.dbee.config.AppProperties;
import com.google.cloud.dialogflow.v2.DetectIntentRequest;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.TextInput;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DialogflowService {
    private final SessionsClient sessionsClient;
    private final AppProperties properties;

    public DialogflowService(SessionsClient sessionsClient, AppProperties properties) {
        this.sessionsClient = sessionsClient;
        this.properties = properties;
    }

    public String detectReply(String text) {
        if (properties.dialogflowProjectId() == null || properties.dialogflowProjectId().isBlank()) {
            throw new IllegalStateException("DIALOGFLOW_PROJECT_ID is not configured.");
        }
        SessionName session = SessionName.of(properties.dialogflowProjectId(), UUID.randomUUID().toString());
        TextInput textInput = TextInput.newBuilder().setText(text).setLanguageCode(properties.dialogflowLanguageCode()).build();
        QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();
        DetectIntentRequest request = DetectIntentRequest.newBuilder().setSession(session.toString()).setQueryInput(queryInput).build();
        QueryResult result = sessionsClient.detectIntent(request).getQueryResult();
        return result.getFulfillmentText().isBlank() ? "Sorry, I could not find an answer for that." : result.getFulfillmentText();
    }
}
