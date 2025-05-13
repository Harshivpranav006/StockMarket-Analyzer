package com.stockmarket.service;

import com.stockmarket.model.NewsItem;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class NewsService {

    private final OkHttpClient client = new OkHttpClient();

    @Value("${newsapi.api.key}")
    private String apiKey;

    public List<NewsItem> getMarketNews() {
        try {
            String url = "https://newsapi.org/v2/top-headlines?category=business&language=en&apiKey=" + apiKey;

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful())
                    throw new IOException("Unexpected response " + response);

                JSONObject json = new JSONObject(response.body().string());
                JSONArray articles = json.getJSONArray("articles");
                List<NewsItem> newsItems = new ArrayList<>();

                for (int i = 0; i < articles.length(); i++) {
                    JSONObject article = articles.getJSONObject(i);
                    NewsItem newsItem = new NewsItem();
                    newsItem.setTitle(article.getString("title"));
                    newsItem.setSummary(article.getString("description"));
                    newsItem.setUrl(article.getString("url"));
                    newsItem.setPublishedAt(article.getLong("publishedAt"));
                    newsItems.add(newsItem);
                }

                return newsItems;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}