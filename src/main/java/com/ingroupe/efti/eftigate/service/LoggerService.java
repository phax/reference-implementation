package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.log.LogRequestDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@AllArgsConstructor
@Slf4j
public class LoggerService {

    private final GateProperties gateProperties;

    public void log(final String...args) {
        if (args != null) {
            log.info(MarkerFactory.getMarker("STATS"), replaceWord(String.join("|", args)));
        }
    }

    private String replaceWord(final String log) {
        return log.replace("null", "");
    }
}
