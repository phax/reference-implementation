package com.ingroupe.efti.eftigate.controller;

import com.ingroupe.efti.eftigate.config.security.Roles;
import com.ingroupe.efti.eftigate.dto.RequestUuidDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.service.ControlService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@AllArgsConstructor
@Slf4j
public class ControlController {

    private final ControlService controlService;

    @GetMapping("/{id}")
    @Secured(Roles.ROLE_ROAD_CONTROLER)
    public ResponseEntity<ControlEntity> getById(@PathVariable long id) {
        return new ResponseEntity<>(controlService.getById(id), HttpStatus.OK);
    }

    @PostMapping("/requestUil")
    @Secured(Roles.ROLE_ROAD_CONTROLER)
    public ResponseEntity<ControlEntity> requestUil(@RequestBody UilDto uilDto) {
        log.info("POST on /requestUil with param uuid {}", uilDto.getUuid());
        return new ResponseEntity<>(controlService.createControlEntity(uilDto), HttpStatus.ACCEPTED);
    }

    @GetMapping("/requestUil")
    @Secured(Roles.ROLE_ROAD_CONTROLER)
    public ResponseEntity<RequestUuidDto> getRequestUil(@Parameter String requestUuid) {
        log.info("GET on /requestUil with param requestUuid {}", requestUuid);
        return new ResponseEntity<>(controlService.getControlEntity(requestUuid), HttpStatus.OK);
    }
}
