package com.chrisV.hoursBackend.controller;

import com.chrisV.hoursBackend.dto.WeeklyReportGeneral;
import com.chrisV.hoursBackend.dto.WeeklyReportPerPerson;
import com.chrisV.hoursBackend.model.AmazonNames;
import com.chrisV.hoursBackend.model.AmazonTransaction;
import com.chrisV.hoursBackend.service.AmazonServices;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/amzTransaction")
@CrossOrigin(origins = {"http://raspberrypi:5500", "http://chris-fedora:5500"})
public class AmazonTransactionController {

    @Autowired
    AmazonServices service;

    @GetMapping("/test")
    public void testingAmazon() {
        System.out.println("Endpoint hit for amazon hehe");
    }

    @PostMapping("/saveTable")
    public ResponseEntity<Void> saveAmzRows(@RequestBody List<AmazonTransaction> transactions) {
        service.saveAmzRows(transactions);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getAllRows")
    public ResponseEntity<Page<AmazonTransaction>> loadAllAmzRows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size
    ) {
        return new ResponseEntity<>(service.loadAmzRowsNew(page, size), HttpStatus.OK);
    }

    @GetMapping("/getAllWorkerNames")
    public ResponseEntity<AmazonNames[]> getAllWorkerName() {
        return new ResponseEntity<>(service.getAllWorkerName(), HttpStatus.OK);
    }

    @GetMapping("/getAllTotalPerWeek")
    public ResponseEntity<Page<WeeklyReportGeneral>> loadTotalPerWeek(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size
    ) {
        return new ResponseEntity<>(service.loadWeeklyTotal(page, size), HttpStatus.OK);
    }

    @GetMapping("/getWeeklyTotalsPerPerson")
    public ResponseEntity<Page<WeeklyReportPerPerson>> loadWeeklyTotalPerPerson(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size
    ) {
        return new ResponseEntity<>(service.loadWeeklyTotalPerPerson(page, size), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAmzRow(@PathVariable Long id) {
        service.deleteAmzRowById(id);
        return ResponseEntity.noContent().build();
    }
}




