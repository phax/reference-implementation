package com.ingroupe.efti.eftilogger.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class LoggerService {

    public void log(final String...args) {
        if (args != null) {
            log.info(MarkerFactory.getMarker("STATS"), replaceWord(String.join("|", args)));
        }
    }

    private String replaceWord(final String log) {
        return log.replace("null", "");
    }
}
