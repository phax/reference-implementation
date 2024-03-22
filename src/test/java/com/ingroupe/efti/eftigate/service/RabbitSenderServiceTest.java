package com.ingroupe.efti.eftigate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static com.ingroupe.efti.eftigate.EftiTestUtils.testFile;
import static org.mockito.Mockito.verify;

class RabbitSenderServiceTest {

    AutoCloseable openMocks;

    private RabbitSenderService rabbitSenderService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    public void before() {
        openMocks = MockitoAnnotations.openMocks(this);

        rabbitSenderService = new RabbitSenderService();
        ReflectionTestUtils.setField(rabbitSenderService, "rabbitTemplate", rabbitTemplate);

    }

    @Test
    void sendMessageToRabbitTest() throws JsonProcessingException {
        RequestDto requestDto = new RequestDto();
        requestDto.setStatus(RequestStatusEnum.RECEIVED.toString());
        requestDto.setRetry(0);
        requestDto.setControl(ControlDto.builder().id(1).build());
        requestDto.setGateUrlDest("https://efti.gate.be.eu");

        rabbitSenderService.sendMessageToRabbit("exchange", "key", requestDto);

        //Assert
        String requestJson = testFile("/json/request.json");

        verify(rabbitTemplate).convertAndSend("exchange", "key", requestJson);

    }
}
