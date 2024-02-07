package com.ingroupe.platform.platformgatesimulator.service;

import com.ingroupe.platform.platformgatesimulator.config.GateProperties;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReaderService {


    public static final String XML_FILE_TYPE = "xml";
    public static final String JSON_FILE_TYPE = "json";
    private final GateProperties gateProperties;

    private final ResourceLoader resourceLoader;

    public void uploadFile(MultipartFile file) {
        try {
            if (file == null) {
                throw new NullPointerException("No file send");
            }
            log.info("Try to upload file in {} with name {}", gateProperties.getCdaPath(), file.getOriginalFilename());
            file.transferTo(new File(resourceLoader.getResource(gateProperties.getCdaPath()).getURI().getPath() + file.getOriginalFilename()));
            log.info("File uploaded in {}", gateProperties.getCdaPath() + file.getOriginalFilename());
        } catch (IOException e) {
            log.error("Error when try to upload file to server", e);
        }
    }

    public String readFromFile(String file) throws IOException {
        Resource resource = tryOpenFile(file, XML_FILE_TYPE);
        if (!resource.exists()) {
            resource = tryOpenFile(file, JSON_FILE_TYPE);
        }

        if (resource.exists()) {
            return readFromInputStream(resource.getInputStream());
        }

        return null;
    }

    private Resource tryOpenFile(final String path, final String ext) {
        final String filePath = String.join(".", path, ext);
        log.info("try to open file : {}", filePath);
        return resourceLoader.getResource(filePath);
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
