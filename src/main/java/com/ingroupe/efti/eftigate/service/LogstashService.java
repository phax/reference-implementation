package com.ingroupe.efti.eftigate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class LogstashService {

    public void log(final String...args) {
        if (args != null) {
            log.info(replaceWord(String.join("|", args)));
        }
    }

    private String replaceWord(final String log) {
        return log.replace("null", "");
    }
}
