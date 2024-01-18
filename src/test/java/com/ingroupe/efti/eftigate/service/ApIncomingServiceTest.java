package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.edeliveryapconnector.dto.ApConfigDto;
import com.ingroupe.efti.edeliveryapconnector.dto.MessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.RetrieveMessageDto;
import com.ingroupe.efti.edeliveryapconnector.service.NotificationService;
import com.ingroupe.efti.eftigate.config.GateProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.Optional;

import static com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto.MESSAGE_ID;
import static com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto.RECEIVE_SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApIncomingServiceTest extends AbstractServceTest {

    AutoCloseable openMocks;

    private ApIncomingService service;
    @Mock
    private NotificationService notificationService;
    @Mock
    private RequestService requestService;
    private final static String url = "url";
    private final static String password = "password";
    private final static String username = "username";

    @BeforeEach
    public void before() {
        final GateProperties gateProperties = GateProperties.builder()
                .owner("france")
                .ap(GateProperties.ApConfig.builder()
                        .url(url)
                        .password(password)
                        .username(username).build()).build();
        openMocks = MockitoAnnotations.openMocks(this);
        service = new ApIncomingService(notificationService, requestService, gateProperties);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void shouldManageIncomingNotification() {
        final String messageId = "messageId";
        final ReceivedNotificationDto receivedNotificationDto = ReceivedNotificationDto.builder()
                .body(Map.of(RECEIVE_SUCCESS, Map.of(MESSAGE_ID, messageId))).build();
        final ApConfigDto apConfigDto = ApConfigDto.builder()
                .username(username)
                .password(password)
                .url(url)
                .build();
        final NotificationDto<?> notificationDto = NotificationDto.builder()
                .content(RetrieveMessageDto.builder()
                        .messageId(messageId)
                        .messageBodyDto(MessageBodyDto.builder().build())
                        .build())
                .build();

        when(notificationService.consume(apConfigDto, receivedNotificationDto)).thenReturn(Optional.of(notificationDto));
        service.manageIncomingNotification(receivedNotificationDto);

        verify(notificationService).consume(apConfigDto, receivedNotificationDto);
        verify(requestService).updateWithResponse(notificationDto);
    }

    @Test
    void shouldNotUpdateResponseIfNoMessage() {
        final String messageId = "messageId";
        final ReceivedNotificationDto receivedNotificationDto = ReceivedNotificationDto.builder()
                .body(Map.of(RECEIVE_SUCCESS, Map.of(MESSAGE_ID, messageId))).build();
        final ApConfigDto apConfigDto = ApConfigDto.builder()
                .username(username)
                .password(password)
                .url(url)
                .build();

        when(notificationService.consume(apConfigDto, receivedNotificationDto)).thenReturn(Optional.empty());
        service.manageIncomingNotification(receivedNotificationDto);

        verify(notificationService).consume(apConfigDto, receivedNotificationDto);
        verify(requestService, never()).updateWithResponse(any());
    }
}
