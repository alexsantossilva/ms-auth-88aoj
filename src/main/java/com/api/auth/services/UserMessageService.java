package com.api.auth.services;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserMessageService {

    private final String topic;

    public UserMessageService(@Value("${topic.name.producer}") String topic) {
        this.topic = topic;
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String message) {
        this.kafkaTemplate.send(this.topic, message);
        System.out.println("[X] Send Message: " + message);
    }

    public void userNotification(String firstName, String lastName, String email, String message){
        JSONObject json = new JSONObject();
        json.put("firstName", firstName);
        json.put("lastName", lastName);
        json.put("email", email);
        json.put("message", message);

        sendMessage(json.toJSONString());
    }
}
