package com.chrisV.hoursBackend.controller;

import com.chrisV.hoursBackend.model.TableRow;
import com.chrisV.hoursBackend.repo.TableRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/table")
@CrossOrigin("http://127.0.0.1:5500/")
public class TableController {

    @Autowired
    TableRepo repo;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return new ResponseEntity<>("Hello frontend", HttpStatus.OK);
    }

    @PostMapping("/saveTable")
    public ResponseEntity<Void> saveTableRows(@RequestBody List<Map<String, String>> rows) {

        System.out.println(rows.toString());

        List<TableRow> entities = rows
                .stream()
                .map(entity -> {
                    TableRow row = new TableRow();
                    row.setDate(LocalDate.parse(entity.get("date")));
                    row.setHours(new BigDecimal(entity.get("hours")));
                    row.setDescription(entity.get("description"));
                    return row;
                }).toList();

        repo.saveAll(entities);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getTotalHours")
    public ResponseEntity<BigDecimal> getTotalHours() {
        List<TableRow> entities = repo.findAll(); //try to optimize by making one calling maybe from9 a different method in the service hehe

        BigDecimal total = entities.stream()
                .map(TableRow::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ResponseEntity<>(total, HttpStatus.OK);
    }

    @GetMapping("/loadTable")
    public ResponseEntity<List<Map<String, String>>> loadTableRows() {

        return new ResponseEntity<>(repo.findAll().stream()
                .map(entity -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("date", String.valueOf(entity.getDate()));
                    m.put("hours", String.valueOf(entity.getHours()));
                    m.put("description", entity.getDescription());
                    return m;
                }).toList(), HttpStatus.OK);
    }



}
