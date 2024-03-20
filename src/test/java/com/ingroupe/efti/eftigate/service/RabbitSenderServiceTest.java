package com.ingroupe.efti.eftigate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

public class RabbitSenderServiceTest {

    AutoCloseable openMocks;

    private RabbitSenderService rabbitSenderService;

    @BeforeEach
    public void before() {
        openMocks = MockitoAnnotations.openMocks(this);

        rabbitSenderService = new RabbitSenderService();
        ReflectionTestUtils.setField(rabbitSenderService, "rabbitTemplate", Mockito.mock(RabbitTemplate.class));

    }

    @Test
    void sendMessageToRabbitTest() throws JsonProcessingException {
        RequestDto requestDto = new RequestDto();
        rabbitSenderService.sendMessageToRabbit("exchange", "key", requestDto);
    }
}
