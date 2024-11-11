package com.sparta.codechef.domain.alarm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SlackService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String slackWebhookUrl;

    public SlackService(@Value("${SLACK_WEBHOOK_URL}") String slackWebhookUrl) {
        this.slackWebhookUrl = slackWebhookUrl;
    }

    public void sendSlackMessage(String message) {
        SlackMessagePayload payload = new SlackMessagePayload(message);
        restTemplate.postForEntity(slackWebhookUrl, payload, String.class);
    }

    private static class SlackMessagePayload {
        private final String text;

        public SlackMessagePayload(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}
