package com.example.isoogle.service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

 public class GoogleQuery 
 {
     public String searchKeyword;

     public GoogleQuery(String searchKeyword)
     {
         this.searchKeyword = searchKeyword;
     }

    /**
     * Query Google Custom Search JSON API (CSE) and return title->link map.
     * Uses RestTemplate to call the Google Custom Search API and parses the JSON response.
     */
    @SuppressWarnings({"unchecked"})
    public HashMap<String, String> query(String query, String apiKey, String cx)
    {
        HashMap<String, String> results = new HashMap<>();
        try {
            String q = java.net.URLEncoder.encode(query, StandardCharsets.UTF_8.name());
            String url = "https://www.googleapis.com/customsearch/v1?key=" + apiKey + "&cx=" + cx + "&num=10&q=" + q;

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map<String, Object>> resp = restTemplate.getForEntity(url, (Class<Map<String, Object>>) (Class<?>) Map.class);
            Map<String, Object> body = resp.getBody();
            if (body == null) {
                return results;
            }
            Object itemsObj = body.get("items");
            if (itemsObj instanceof List) {
                List<?> items = (List<?>) itemsObj;
                for (Object itemObj : items) {
                    if (itemObj instanceof Map) {
                        Map<String, Object> item = (Map<String, Object>) itemObj;
                        Object titleObj = item.get("title");
                        Object linkObj = item.get("link");
                        String title = titleObj == null ? null : titleObj.toString();
                        String link = linkObj == null ? null : linkObj.toString();
                        if (title != null && !title.isEmpty() && link != null && !link.isEmpty()) {
                            results.put(title, link);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Keep behavior similar to existing code: print stacktrace for debugging
            e.printStackTrace();
        }
        return results;
    }
 }
