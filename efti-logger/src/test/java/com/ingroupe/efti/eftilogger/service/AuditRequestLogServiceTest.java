package com.ingroupe.efti.eftilogger.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ingroupe.efti.commons.dto.AuthorityDto;
import com.ingroupe.efti.commons.dto.ControlDto;
import com.ingroupe.efti.commons.dto.ErrorDto;
import com.ingroupe.efti.commons.enums.ErrorCodesEnum;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.eftilogger.dto.MessagePartiesDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static com.ingroupe.efti.eftilogger.model.ComponentType.GATE;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AuditRequestLogServiceTest extends AbstractTestService {

    private AuditRequestLogService auditRequestLogService;

    private ControlDto controlDto;
    private MessagePartiesDto messagePartiesDto;
    private final StatusEnum status = StatusEnum.COMPLETE;
    private ListAppender<ILoggingEvent> logWatcher;

    @BeforeEach
    public void init() {
        logWatcher = new ListAppender<>();
        logWatcher.start();
        ((Logger) LoggerFactory.getLogger(LogService.class)).addAppender(logWatcher);

        controlDto = ControlDto.builder()
                .id(1)
                .authority(AuthorityDto.builder()
                        .name("name")
                        .nationalUniqueIdentifier("nui").build())
                .requestType(RequestTypeEnum.EXTERNAL_UIL_SEARCH)
                .requestUuid("requestUuid")
                .subsetEuRequested("subsetEu")
                .subsetMsRequested("subsetMS")
                .eftiDataUuid("dataUuid")
                .error(ErrorDto.fromErrorCode(ErrorCodesEnum.DEFAULT_ERROR))
                .build();

        messagePartiesDto = MessagePartiesDto.builder()
                .requestingComponentId("sender")
                .requestingComponentType(GATE)
                .requestingComponentCountry("senderCountry")
                .respondingComponentId("receiver")
                .respondingComponentType(GATE)
                .respondingComponentCountry("receiverCountry").build();
        auditRequestLogService = new AuditRequestLogService(serializeUtils);
    }

    @Test
    void shouldLogAckTrue() {
        final String expected = "\"componentType\":\"GATE\",\"componentId\":\"gateId\",\"componentCountry\":\"gateCountry\",\"requestingComponentType\":\"GATE\",\"requestingComponentId\":\"sender\",\"requestingComponentCountry\":\"senderCountry\",\"respondingComponentType\":\"GATE\",\"respondingComponentId\":\"receiver\",\"respondingComponentCountry\":\"receiverCountry\",\"messageContent\":\"body\",\"statusMessage\":\"COMPLETE\",\"errorCodeMessage\":\"DEFAULT_ERROR\",\"errorDescriptionMessage\":\"Error\",\"timeoutComponentType\":\"timeoutComponentType\",\"requestId\":\"requestUuid\",\"eFTIDataId\":\"dataUuid\",\"responseId\":\"responseId\",\"authorityNationalUniqueIdentifier\":\"nui\",\"authorityName\":\"name\",\"officerId\":\"officerId\",\"subsetEURequested\":\"subsetEu\",\"subsetMSRequested\":\"subsetMS\",\"requestType\":\"UIL_ACK\",\"eftidataId\":\"dataUuid\"";
        auditRequestLogService.log(controlDto, messagePartiesDto, gateId, gateCountry, body, status, true);
        assertThat(logWatcher.list.get(0).getFormattedMessage()).contains(expected);
    }

    @Test
    void shouldLogAckFalse() {
        final String expected = "\"componentType\":\"GATE\",\"componentId\":\"gateId\",\"componentCountry\":\"gateCountry\",\"requestingComponentType\":\"GATE\",\"requestingComponentId\":\"sender\",\"requestingComponentCountry\":\"senderCountry\",\"respondingComponentType\":\"GATE\",\"respondingComponentId\":\"receiver\",\"respondingComponentCountry\":\"receiverCountry\",\"messageContent\":\"body\",\"statusMessage\":\"COMPLETE\",\"errorCodeMessage\":\"DEFAULT_ERROR\",\"errorDescriptionMessage\":\"Error\",\"timeoutComponentType\":\"timeoutComponentType\",\"requestId\":\"requestUuid\",\"eFTIDataId\":\"dataUuid\",\"responseId\":\"responseId\",\"authorityNationalUniqueIdentifier\":\"nui\",\"authorityName\":\"name\",\"officerId\":\"officerId\",\"subsetEURequested\":\"subsetEu\",\"subsetMSRequested\":\"subsetMS\",\"requestType\":\"UIL\",\"eftidataId\":\"dataUuid\"";
        auditRequestLogService.log(controlDto, messagePartiesDto, gateId, gateCountry, body, status, false);
        assertThat(logWatcher.list.get(0).getFormattedMessage()).contains(expected);
    }
}
