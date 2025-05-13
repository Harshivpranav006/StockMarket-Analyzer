package com.stockmarket.service;

import com.stockmarket.model.StockData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StockService {

    private final OkHttpClient client = new OkHttpClient();
    private final Map<String, StockData> stockCache = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${alphavantage.api.key}")
    private String apiKey;

    public StockService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public StockData getStockData(String symbol) {
        try {
            String url = String.format("https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                    symbol, apiKey);

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful())
                    throw new IOException("Unexpected response " + response);

                JSONObject json = new JSONObject(response.body().string());
                JSONObject quote = json.getJSONObject("Global Quote");

                StockData stockData = new StockData();
                stockData.setSymbol(symbol);
                stockData.setPrice(Double.parseDouble(quote.getString("05. price")));
                stockData.setChange(Double.parseDouble(quote.getString("10. change percent").replace("%", "")));
                stockData.setVolume(Long.parseLong(quote.getString("06. volume")));
                stockData.setTimestamp(System.currentTimeMillis());

                stockCache.put(symbol, stockData);
                return stockData;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return stockCache.getOrDefault(symbol, createEmptyStockData(symbol));
        }
    }

    @Scheduled(fixedRate = 5000)
    public void updateStockData() {
        stockCache.keySet().forEach(symbol -> {
            StockData data = getStockData(symbol);
            messagingTemplate.convertAndSend("/topic/stock/" + symbol, data);
        });
    }

    private StockData createEmptyStockData(String symbol) {
        StockData data = new StockData();
        data.setSymbol(symbol);
        data.setPrice(0.0);
        data.setChange(0.0);
        data.setVolume(0);
        data.setTimestamp(System.currentTimeMillis());
        return data;
    }
}