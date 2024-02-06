package com.ingroupe.platform.platformgatesimulator.service;

import com.ingroupe.platform.platformgatesimulator.config.GateProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
@AllArgsConstructor
@Slf4j
public class ReaderService {

    @Autowired
    private final GateProperties gateProperties;

    public void uploadFile(MultipartFile file) {
        try {
            if (file == null) {
                throw new NullPointerException("No file send");
            }
            log.info("Try to upload file in {} with name {}", gateProperties.getCdaPath(), file.getOriginalFilename());
            file.transferTo(new File(gateProperties.getCdaPath() + file.getOriginalFilename()));
            log.info("File uploaded in {}", gateProperties.getCdaPath() + file.getOriginalFilename());
        } catch (IOException e) {
            log.error("Error when try to upload file to server", e);
        }
    }

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
