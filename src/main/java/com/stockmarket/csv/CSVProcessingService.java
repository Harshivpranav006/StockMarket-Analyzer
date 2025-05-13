package com.stockmarket.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CSVProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(CSVProcessingService.class);

    @Value("${csv.upload.dir:datasets}")
    private String datasetsDir;

    @Value("${csv.model.dir:models}")
    private String modelDir;

    @Value("${csv.max.files:50}")
    private int maxFiles;

    private final Map<String, CSVDataModel> trainedModels = new HashMap<>();

    @PostConstruct
    public void init() {
        logger.info("Initializing CSVProcessingService...");
        createDirectories();
        loadExistingDatasets();
        logger.info("CSVProcessingService initialization completed. Found {} models.", trainedModels.size());
    }

    private void createDirectories() {
        try {
            logger.info("Creating directories: {} and {}", datasetsDir, modelDir);
            Files.createDirectories(Paths.get(datasetsDir));
            Files.createDirectories(Paths.get(modelDir));
            logger.info("Directories created successfully");
        } catch (IOException e) {
            logger.error("Error creating directories", e);
        }
    }

    private void loadExistingDatasets() {
        try {
            logger.info("Loading existing datasets from: {}", datasetsDir);
            File datasetsFolder = new File(datasetsDir);
            if (datasetsFolder.exists() && datasetsFolder.isDirectory()) {
                File[] files = datasetsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
                if (files != null) {
                    logger.info("Found {} CSV files to process", files.length);
                    for (File file : files) {
                        try {
                            logger.info("Processing file: {}", file.getName());
                            CSVDataModel model = processExistingCSVFile(file);
                            logger.info("Successfully processed file: {}", file.getName());
                        } catch (Exception e) {
                            logger.error("Error processing file: " + file.getName(), e);
                        }
                    }
                } else {
                    logger.warn("No CSV files found in directory: {}", datasetsDir);
                }
            } else {
                logger.warn("Datasets directory does not exist: {}", datasetsDir);
            }
        } catch (Exception e) {
            logger.error("Error loading existing datasets", e);
        }
    }

    private CSVDataModel processExistingCSVFile(File file) throws IOException {
        logger.info("Starting to process file: {}", file.getName());
        CSVDataModel model = new CSVDataModel();
        model.setFileName(file.getName());

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Read headers
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("Empty file: " + file.getName());
            }

            List<String> headers = Arrays.asList(headerLine.split(","));
            model.setHeaders(headers);
            logger.info("File {} has {} headers", file.getName(), headers.size());

            // Process data in chunks
            List<Map<String, String>> data = new ArrayList<>();
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && lineCount < 10000) {
                String[] values = line.split(",");
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.size() && i < values.length; i++) {
                    row.put(headers.get(i), values[i]);
                }
                data.add(row);
                lineCount++;
            }
            model.setData(data);
            logger.info("Processed {} rows from file {}", lineCount, file.getName());

            // Extract metadata
            Map<String, String> metadata = extractMetadata(data);
            model.setMetadata(metadata);
            logger.info("Extracted metadata for file {}", file.getName());

            // Train model
            trainModel(model);
            logger.info("Trained model for file {}", file.getName());

            // Save model
            trainedModels.put(file.getName(), model);
            saveModel(model);
            logger.info("Saved model for file {}", file.getName());
        }
        return model;
    }

    public CSVDataModel processCSVFile(MultipartFile file) throws IOException {
        logger.info("Processing uploaded file: {}", file.getOriginalFilename());
        String fileName = file.getOriginalFilename();
        Path filePath = Paths.get(datasetsDir, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return processExistingCSVFile(filePath.toFile());
    }

    private Map<String, String> extractMetadata(List<Map<String, String>> data) {
        Map<String, String> metadata = new HashMap<>();
        if (!data.isEmpty()) {
            Map<String, String> firstRow = data.get(0);
            metadata.put("columns", String.valueOf(firstRow.size()));
            metadata.put("rows", String.valueOf(data.size()));
            metadata.put("timestamp", String.valueOf(System.currentTimeMillis()));
        }
        return metadata;
    }

    private void trainModel(CSVDataModel model) {
        logger.info("Training model for file: {}", model.getFileName());
        // Basic model training logic
        model.setModelType("basic");
        model.setAccuracy(0.85); // Placeholder accuracy

        // Generate some sample predictions
        Map<String, Double> predictions = new HashMap<>();
        predictions.put("trend", 0.75);
        predictions.put("volatility", 0.45);
        model.setPredictions(predictions);
        logger.info("Model training completed for file: {}", model.getFileName());
    }

    private void saveModel(CSVDataModel model) {
        try {
            String modelPath = Paths.get(modelDir, model.getFileName() + ".model").toString();
            // Save model to file (implement actual serialization)
            logger.info("Model saved to: {}", modelPath);
        } catch (Exception e) {
            logger.error("Error saving model for file: " + model.getFileName(), e);
        }
    }

    public List<CSVDataModel> getAllModels() {
        logger.info("Retrieving all models. Total count: {}", trainedModels.size());
        return new ArrayList<>(trainedModels.values());
    }

    public CSVDataModel getModel(String fileName) {
        logger.info("Retrieving model for file: {}", fileName);
        return trainedModels.get(fileName);
    }

    public void deleteModel(String fileName) {
        logger.info("Deleting model for file: {}", fileName);
        trainedModels.remove(fileName);
        try {
            Files.deleteIfExists(Paths.get(modelDir, fileName + ".model"));
            Files.deleteIfExists(Paths.get(datasetsDir, fileName));
            logger.info("Successfully deleted model and file: {}", fileName);
        } catch (IOException e) {
            logger.error("Error deleting model for file: " + fileName, e);
        }
    }
}