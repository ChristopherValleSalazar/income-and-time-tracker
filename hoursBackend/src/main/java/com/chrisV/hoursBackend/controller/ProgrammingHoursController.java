package com.chrisV.hoursBackend.controller;

import com.chrisV.hoursBackend.model.ProgrammingHours;
import com.chrisV.hoursBackend.service.ProgrammingHoursService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/table")
@CrossOrigin("http://127.0.0.1:5500/")
public class ProgrammingHoursController {

    @Autowired
    ProgrammingHoursService service;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return new ResponseEntity<>("Hello frontend", HttpStatus.OK);
    }

    @PostMapping("/saveTable")
    public ResponseEntity<Void> saveProgrammingHours(@RequestBody List<ProgrammingHours> rows) {

        service.saveProgrammingHours(rows);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getTotalHours")
    public ResponseEntity<BigDecimal> getTotalHours() {
        return new ResponseEntity<>(service.getTotalHours(), HttpStatus.OK);
    }

    @GetMapping("/loadTable")
    public ResponseEntity<List<ProgrammingHours>> loadProgrammingHours() {
        return new ResponseEntity<>(service.loadProgrammingHours(), HttpStatus.OK);
    }
}
