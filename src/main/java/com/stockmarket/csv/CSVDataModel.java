package com.stockmarket.csv;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CSVDataModel {
    private String fileName;
    private List<String> headers;
    private List<Map<String, String>> data;
    private Map<String, String> metadata;
    private String modelType;
    private double accuracy;
    private Map<String, Double> predictions;
}