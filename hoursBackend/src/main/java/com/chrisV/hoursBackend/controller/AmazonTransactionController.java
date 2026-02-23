package com.chrisV.hoursBackend.controller;

import com.chrisV.hoursBackend.model.AmazonNames;
import com.chrisV.hoursBackend.service.AmazonServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/amzTransaction")
@CrossOrigin("http://127.0.0.1:5500/")
public class AmazonTransactionController {

    @Autowired
    AmazonServices service;

    @GetMapping("/test")
    public void testingAmazon() {
        System.out.println("Endpoint hit for amazon hehe");
    }

    @PostMapping("/saveTable")
    public ResponseEntity<Void> saveAmzRows(@RequestBody List<Map<String, String>> transactions) {
        service.saveAmzRows(transactions);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getAllRows")
    public ResponseEntity<List<Map<String, String>>> loadAllAmzRows() {
        return new ResponseEntity<>(service.loadAllAmzRows(), HttpStatus.OK);
    }

    @GetMapping("/getAllWorkerNames")
    public ResponseEntity<AmazonNames[]> getAllWorkerName() {
        return new ResponseEntity<>(service.getAllWorkerName(), HttpStatus.OK);
    }

    @GetMapping("/getAllTotalPerWeek")
    public ResponseEntity<List<Map<String, String>>> loadTotalPerWeek() {
        return new ResponseEntity<>(service.loadWeeklyTotalPerPerson(), HttpStatus.OK);
    }
        /*
        using this list will allow me to generate a total per person, I will make individual methods afterward
        essentially this logic will be done in 2 methods, one for complete total and the other one will be for
        filter out totals per person
         */

        //need to filter this string response for packages then use same logic for the rest of them
        //instead of getting the string version of the list I need to filter out the original obj
        //then I need to filter and passed that into the converter to String list
        //So separating concerns for these conversions.

    @GetMapping("/getWeeklyTotalsPerPerson")
    public ResponseEntity<List<Map<String, String>>> loadWeeklyTotalPerPerson() {
        return new ResponseEntity<>(service.loadWeeklyTotalPerPerson(), HttpStatus.OK);
    }
}




