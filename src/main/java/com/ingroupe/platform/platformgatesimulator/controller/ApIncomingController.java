package com.ingroupe.platform.platformgatesimulator.controller;

import com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto;
import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import com.ingroupe.platform.platformgatesimulator.service.ApIncomingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/ws")
@AllArgsConstructor
@Slf4j
public class ApIncomingController {

    private final ApIncomingService apIncomingService;

    @PostMapping("/notification")
    public void getById(final @RequestBody ReceivedNotificationDto receivedNotificationDto) throws SendRequestException, IOException {
        System.out.println("Notif Reçus");
        apIncomingService.manageIncomingNotification(receivedNotificationDto);
    }
}
