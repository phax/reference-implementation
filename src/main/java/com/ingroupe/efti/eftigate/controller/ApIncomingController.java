package com.ingroupe.efti.eftigate.controller;

import com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto;
import com.ingroupe.efti.eftigate.service.ApIncomingService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/ws")
@AllArgsConstructor
@Slf4j
public class ApIncomingController {

    private final ApIncomingService apIncomingService;

    @PostMapping("/notification")
    public ResponseEntity<Void> incoming(final @RequestBody ReceivedNotificationDto receivedNotificationDto) {
        log.info("receive notification !");
        apIncomingService.manageIncomingNotification(receivedNotificationDto);
        return ResponseEntity.ok().build();
    }
}
