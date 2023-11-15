package com.ingroupe.efti.eftigate.Controller;

import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.service.ControlService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/control")
@AllArgsConstructor
public class ControlController {

    private final ControlService controlService;

    @GetMapping("/{id}")
    public ResponseEntity<ControlEntity> getById(@PathVariable long id) {
        return new ResponseEntity<>(controlService.getById(id), HttpStatus.OK);
    }
}
