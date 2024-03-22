package com.ingroupe.efti.eftigate.service.gate;

import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.eftigate.config.GateProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GateToRequestTypeFunctionTest {
    AutoCloseable openMocks;

    private GateToRequestTypeFunction gateToRequestTypeFunction;

    @BeforeEach
    public void before() {
        final GateProperties gateProperties = GateProperties.builder()
                .owner("https://efti.gate.fr.eu")
                .ap(GateProperties.ApConfig.builder()
                        .url("https://efti.gate.fr.eu")
                        .build()).build();
        openMocks = MockitoAnnotations.openMocks(this);
        gateToRequestTypeFunction = new GateToRequestTypeFunction(gateProperties);
    }

    @Test
    void shouldGetRequestType_WhenMultipleGatesAreGiven(){
        //Act
        RequestTypeEnum requestType = gateToRequestTypeFunction.apply(List.of("https://efti.gate.fr.eu", "https://efti.gate.be.eu"));

        //Assert
        assertEquals(RequestTypeEnum.EXTERNAL_METADATA_SEARCH, requestType);
    }

    @Test
    void shouldGetRequestType_WhenSingleGateIsGivenAndIsEqualToLocalGate(){
        //Act
        RequestTypeEnum requestType = gateToRequestTypeFunction.apply(List.of("https://efti.gate.fr.eu"));

        //Assert
        assertEquals(RequestTypeEnum.LOCAL_METADATA_SEARCH, requestType);
    }

    @Test
    void shouldGetRequestType_WhenSingleGateIsGivenAndIsDifferentFromLocalGate(){
        //Act
        RequestTypeEnum requestType = gateToRequestTypeFunction.apply(List.of("https://efti.gate.be.eu"));

        //Assert
        assertEquals(RequestTypeEnum.EXTERNAL_ASK_METADATA_SEARCH, requestType);
    }


}
