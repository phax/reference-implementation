package com.ingroupe.platform.platformgatesimulator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingroupe.efti.edeliveryapconnector.dto.MessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.RetrieveMessageDto;
import com.ingroupe.efti.edeliveryapconnector.service.NotificationService;
import com.ingroupe.efti.edeliveryapconnector.service.RequestSendingService;
import com.ingroupe.platform.platformgatesimulator.config.GateProperties;
import com.ingroupe.platform.platformgatesimulator.exception.UuidFileNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties(GateProperties.class)
@TestPropertySource("classpath:application-test.properties")
class ApIncomingServiceTest {

    AutoCloseable openMocks;


    @Mock
    private RequestSendingService requestSendingService;

    @Mock
    private NotificationService notificationService;
    @Mock
    private GateProperties gateProperties;

    @Mock
    private ReaderService readerService;

    @Mock
    private ObjectMapper objectMapper;

    private ApIncomingService apIncomingService;

    @BeforeEach
    public void before() {
        final GateProperties gateProperties = GateProperties.builder()
                .owner("france")
                .ap(GateProperties.ApConfig.builder()
                        .url("url")
                        .password("password")
                        .username("username").build()).build();
        openMocks = MockitoAnnotations.openMocks(this);
        apIncomingService = new ApIncomingService(requestSendingService, notificationService, gateProperties, readerService, objectMapper);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void manageIncomingNotificationTest() throws IOException, UuidFileNotFoundException {
        NotificationDto notificationDto = new NotificationDto<>();
        RetrieveMessageDto retrieveMessageDto = new RetrieveMessageDto();
        MessageBodyDto messageBodyDto = new MessageBodyDto("reques", "oki", "oki", "oki", "oki");
        retrieveMessageDto.setMessageBodyDto(messageBodyDto);
        notificationDto.setContent(retrieveMessageDto);
        Mockito.when(notificationService.consume(any(), any())).thenReturn(Optional.of(notificationDto));
        apIncomingService.manageIncomingNotification(new ReceivedNotificationDto());
    }
}
