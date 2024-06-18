package com.ingroupe.efti.eftigate.service.gate;

import com.ingroupe.efti.eftigate.entity.GateEntity;
import com.ingroupe.efti.eftigate.repository.GateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GateServiceTest {

    private GateService gateService;

    @Mock
    private GateRepository gateRepository;

    @BeforeEach
    public void before() {
        gateService = new GateService(gateRepository);
    }

    @Test
    void checkGateUrlShouldFindUrlTest() {
        String gateUrl = "findGateUrl";

        when(gateRepository.findByUrl(gateUrl)).thenReturn(new GateEntity());

        boolean result = gateService.checkGateUrl(gateUrl);

        verify(gateRepository, times(1)).findByUrl(any());
        assertTrue(result);
    }

    @Test
    void checkGateUrlShouldNotFindUrlTest() {
        String gateUrl = "notFindGateUrl";

        when(gateRepository.findByUrl(gateUrl)).thenReturn(null);

        boolean result = gateService.checkGateUrl(gateUrl);

        verify(gateRepository, times(1)).findByUrl(any());
        assertFalse(result);
    }

    @Test
    void checkGateUrlBadStringSendTest() {
        String gateUrl = "";

        boolean result = gateService.checkGateUrl(gateUrl);

        verify(gateRepository, times(0)).findByUrl(any());
        assertFalse(result);
    }
}
