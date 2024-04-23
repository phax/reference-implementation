package com.ingroupe.efti.eftigate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static com.ingroupe.efti.eftigate.EftiTestUtils.testFile;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitSenderServiceTest {
    private RabbitSenderService rabbitSenderService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    public void before() {
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

        verify(rabbitTemplate).convertAndSend("exchange", "key", StringUtils.deleteWhitespace(requestJson));

    }
}
