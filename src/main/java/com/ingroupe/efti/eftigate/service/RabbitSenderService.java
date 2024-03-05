package com.ingroupe.efti.eftigate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitSenderService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessageToRabbit(String exchange, String key, Object message) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        ObjectWriter ow = objectMapper.writer();
        String json = ow.writeValueAsString(message);
        rabbitTemplate.convertAndSend(exchange, key, json);
    }
}
