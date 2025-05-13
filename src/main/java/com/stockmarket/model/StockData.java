package com.stockmarket.model;

import lombok.Data;

@Data
public class StockData {
    private String symbol;
    private double price;
    private double change;
    private long volume;
    private long timestamp;
} 