package com.stockmarket.model;

import lombok.Data;

@Data
public class NewsItem {
    private String title;
    private String summary;
    private String url;
    private long publishedAt;
} 