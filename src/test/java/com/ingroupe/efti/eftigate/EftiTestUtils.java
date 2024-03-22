package com.ingroupe.efti.eftigate;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.apache.commons.io.IOUtils.readLines;
import static org.apache.commons.lang3.StringUtils.join;

public class EftiTestUtils {
    public static String testFile(String fileName) {
        return testFile(fileName, "");
    }

    public static String testFile(String fileName, String separator) {
        return join(testFileLines(fileName), separator);
    }

    public static List<String> testFileLines(String fileName) {
        InputStream resource = testFileResource(fileName);
        try {
            return readLines(resource, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("failed to load test file " + fileName, e);
        }
    }

    private static InputStream testFileResource(String filePath) {
        InputStream resource = EftiTestUtils.class.getResourceAsStream(filePath);
        if (resource == null) {
            throw new RuntimeException(filePath + " not found");
        }
        return resource;
    }
}
