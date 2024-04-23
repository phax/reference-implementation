package com.ingroupe.efti.eftigate.service.request;


import com.ingroupe.efti.commons.enums.EDeliveryAction;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.ingroupe.efti.commons.enums.RequestTypeEnum.*;
import static com.ingroupe.efti.commons.enums.StatusEnum.COMPLETE;
import static com.ingroupe.efti.commons.enums.StatusEnum.PENDING;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RequestToEDeliveryActionFunctionTest {


    public static final String FR_GATE_URL = "https://efti.gate.fr.eu";
    public static final String DE_GATE_URL = "https://efti.gate.de.eu";

    @ParameterizedTest
    @MethodSource("provideMyObject")
    void myTestMethod(RequestDto actual, EDeliveryAction expected) {
        final GateProperties gateProperties = GateProperties.builder().owner(FR_GATE_URL).ap(GateProperties.ApConfig.builder().url(FR_GATE_URL).build()).build();
        RequestToEDeliveryActionFunction requestToEDeliveryActionFunction = new RequestToEDeliveryActionFunction(gateProperties);

        EDeliveryAction applied = requestToEDeliveryActionFunction.apply(actual);

        Assertions.assertEquals(expected, applied);
    }

    static Stream<Arguments> provideMyObject() {
        return Stream.of(
                arguments(buildRequestDto(LOCAL_METADATA_SEARCH.name(), FR_GATE_URL, PENDING.name()), EDeliveryAction.GET_IDENTIFIERS),
                arguments(buildRequestDto(EXTERNAL_METADATA_SEARCH.name(), FR_GATE_URL, PENDING.name()), EDeliveryAction.GET_IDENTIFIERS),
                arguments(buildRequestDto(EXTERNAL_ASK_METADATA_SEARCH.name(), FR_GATE_URL, PENDING.name()), EDeliveryAction.GET_IDENTIFIERS),
                arguments(buildRequestDto(LOCAL_UIL_SEARCH.name(), FR_GATE_URL, PENDING.name()), EDeliveryAction.GET_UIL),
                arguments(buildRequestDto(EXTERNAL_UIL_SEARCH.name(), FR_GATE_URL, PENDING.name()), EDeliveryAction.FORWARD_UIL),
                arguments(buildRequestDto(EXTERNAL_ASK_UIL_SEARCH.name(), FR_GATE_URL, PENDING.name()), EDeliveryAction.GET_UIL),
                arguments(buildRequestDto(EXTERNAL_ASK_UIL_SEARCH.name(), DE_GATE_URL, PENDING.name()), EDeliveryAction.FORWARD_UIL),
                arguments(buildRequestDto(EXTERNAL_ASK_UIL_SEARCH.name(), FR_GATE_URL, COMPLETE.name()), EDeliveryAction.FORWARD_UIL),
                arguments(buildRequestDto(NOTE_SEND.name(), FR_GATE_URL, PENDING.name()), null),
                arguments(buildRequestDto(null, FR_GATE_URL, PENDING.name()), null)
                );
    }

    private static RequestDto buildRequestDto(String requestType, String eftiGateUrl, String controlStatus) {
        return RequestDto.builder()
                .control(ControlDto.builder().requestType(requestType).eftiGateUrl(eftiGateUrl).status(controlStatus).build())
                .build();
    }
}
