package com.ingroupe.platform.platformgatesimulator.service;

import com.ingroupe.platform.platformgatesimulator.exception.UuidFileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Service
@Slf4j
public class ReaderService {

    public String readFromFile(String file) throws IOException, UuidFileNotFoundException {
        log.info("try to open file : {}", file);
        ClassLoader classLoader = getClass().getClassLoader();
        log.info("try .json");
        InputStream inputStream = classLoader.getResourceAsStream(file + ".json");
        if (inputStream == null) {
            log.info("try .xml");
            inputStream = classLoader.getResourceAsStream(file + ".xml");
            if (inputStream == null) {
                log.info("Don't find file");
                throw new UuidFileNotFoundException("Uuid file not found");
            }
        }
        return readFromInputStream(inputStream);
    }

    private String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        } catch (NullPointerException e) {
            log.error("File doesn't exist");
            return null;
        }
        return resultStringBuilder.toString();
    }
}
