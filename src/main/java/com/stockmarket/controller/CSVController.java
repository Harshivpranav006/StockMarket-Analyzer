package com.stockmarket.controller;

import com.stockmarket.csv.CSVDataModel;
import com.stockmarket.csv.CSVProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/csv")
@CrossOrigin(origins = "*")
public class CSVController {

    private final CSVProcessingService csvProcessingService;

    public CSVController(CSVProcessingService csvProcessingService) {
        this.csvProcessingService = csvProcessingService;
    }

    @PostMapping("/upload")
    public ResponseEntity<CSVDataModel> uploadCSV(@RequestParam("file") MultipartFile file) {
        try {
            CSVDataModel model = csvProcessingService.processCSVFile(file);
            return ResponseEntity.ok(model);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/models")
    public ResponseEntity<List<CSVDataModel>> getAllModels() {
        return ResponseEntity.ok(csvProcessingService.getAllModels());
    }

    @GetMapping("/models/{fileName}")
    public ResponseEntity<CSVDataModel> getModel(@PathVariable String fileName) {
        CSVDataModel model = csvProcessingService.getModel(fileName);
        return model != null ? ResponseEntity.ok(model) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/models/{fileName}")
    public ResponseEntity<Void> deleteModel(@PathVariable String fileName) {
        csvProcessingService.deleteModel(fileName);
        return ResponseEntity.ok().build();
    }
}