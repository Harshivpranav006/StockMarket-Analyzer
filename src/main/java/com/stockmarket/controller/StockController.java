package com.stockmarket.controller;

import com.stockmarket.model.NewsItem;
import com.stockmarket.model.StockData;
import com.stockmarket.service.NewsService;
import com.stockmarket.service.StockService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StockController {

    private final StockService stockService;
    private final NewsService newsService;

    public StockController(StockService stockService, NewsService newsService) {
        this.stockService = stockService;
        this.newsService = newsService;
    }

    @GetMapping("/stock/{symbol}")
    public StockData getStockData(@PathVariable String symbol) {
        StockData data = stockService.getStockData(symbol);
        // If the API fails or returns empty/default data, return a mock object
        if (data == null || data.getPrice() == 0.0) {
            data = new StockData();
            data.setSymbol(symbol);
            data.setPrice(1234.56); // mock price
            data.setChange(1.23); // mock change
            data.setVolume(100000); // mock volume
            data.setTimestamp(System.currentTimeMillis());
        }
        return data;
    }

    @GetMapping("/news")
    public List<NewsItem> getMarketNews() {
        return newsService.getMarketNews();
    }
}