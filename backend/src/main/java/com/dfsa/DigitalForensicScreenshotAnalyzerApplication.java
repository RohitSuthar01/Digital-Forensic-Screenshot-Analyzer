package com.dfsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DigitalForensicScreenshotAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalForensicScreenshotAnalyzerApplication.class, args);
    }

}