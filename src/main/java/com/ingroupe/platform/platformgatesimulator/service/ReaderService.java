package com.ingroupe.platform.platformgatesimulator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
@Slf4j
public class ReaderService {
    public String readFromFile(String file) throws IOException {
        log.info("try to open file : {}", file);
        FileInputStream fileOpen = tryOpenXmlFile(file);
        if (fileOpen == null) {
            fileOpen = tryOpenJsonFile(file);
            if (fileOpen == null) {
                return null;
            }
        }
        return readFromInputStream(fileOpen);
    }

    private static FileInputStream tryOpenXmlFile(String file) {
        try {
            return new FileInputStream(file + ".xml");
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private static FileInputStream tryOpenJsonFile(String file) {
        try {
            return new FileInputStream(file + ".json");
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private String readFromInputStream(FileInputStream inputStream)
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
