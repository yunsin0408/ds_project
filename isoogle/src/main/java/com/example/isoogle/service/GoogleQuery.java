package com.example.isoogle.service;

 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
 import java.util.List;
 import java.util.LinkedHashMap;
 import java.util.Map;

 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

 public class GoogleQuery 
 {
     public String searchKeyword;
     public String url;
     public String content;

     public GoogleQuery(String searchKeyword)
     {
         this.searchKeyword = searchKeyword;
         try 
         {
             // This part has been specially handled for Chinese keyword processing. 
             // You can comment out the following two lines 
             // and use the line of code in the lower section. 
             // Also, consider why the results might be incorrect 
             // when entering Chinese keywords.
             String encodeKeyword=java.net.URLEncoder.encode(searchKeyword,"utf-8");
             this.url = "https://www.google.com/search?q="+encodeKeyword+"&oe=utf8&num=20";

             // this.url = "https://www.google.com/search?q="+searchKeyword+"&oe=utf8&num=20";
         }
         catch (Exception e)
         {
             System.out.println(e.getMessage());
         }
     }

     private String fetchContent() throws IOException
     {
         String retVal = "";

         URL u = new URL(url);
         URLConnection conn = u.openConnection();
         //set HTTP header
         conn.setRequestProperty("User-agent", "Chrome/107.0.5304.107");
         InputStream in = conn.getInputStream();

         InputStreamReader inReader = new InputStreamReader(in, "utf-8");
         BufferedReader bufReader = new BufferedReader(inReader);
         String line = null;

         while((line = bufReader.readLine()) != null)
         {
             retVal += line;
         }
         return retVal;
     }

     public HashMap<String, String> query() throws IOException
     {
         if(content == null)
         {
             content = fetchContent();
         }

         HashMap<String, String> retVal = new HashMap<String, String>();

         /* 
          * some Jsoup source
          * https://jsoup.org/apidocs/org/jsoup/nodes/package-summary.html
          * https://www.1ju.org/jsoup/jsoup-quick-start
          */

         //using Jsoup analyze html string
         Document doc = Jsoup.parse(content);

         //select particular element(tag) which you want 
         Elements lis = doc.select("div");
         lis = lis.select(".kCrYT");

         for(Element li : lis)
         {
             try 
             {
                 String citeUrl = li.select("a").get(0).attr("href").replace("/url?q=", "");
                 String title = li.select("a").get(0).select(".vvjwJb").text();

                 if(title.equals("")) 
                 {
                     continue;
                 }

                 System.out.println("Title: " + title + " , url: " + citeUrl);

                 //put title and pair into HashMap
                 retVal.put(title, citeUrl);

             } catch (IndexOutOfBoundsException e) 
             {
 //				e.printStackTrace();
             }
         }

         return retVal;
     }

    /**
     * Query Google Custom Search JSON API (CSE) and return title->link map.
     * This is a simple helper that uses RestTemplate and parses the JSON body
     * into a LinkedHashMap to preserve ordering.
     */
    @SuppressWarnings({"unchecked"})
    public HashMap<String, String> queryCse(String query, String apiKey, String cx)
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

    @SuppressWarnings("unchecked")
    private Map<String, String> parseCseResponse(Map<String, Object> body)
    {
        Map<String, String> results = new LinkedHashMap<>();
        if (body == null) return results;
        Object itemsObj = body.get("items");
        if (!(itemsObj instanceof List)) return results;

        List<?> items = (List<?>) itemsObj;
        for (Object itemObj : items) {
            if (!(itemObj instanceof Map)) continue;
            Map<String, Object> item = (Map<String, Object>) itemObj;
            Object titleObj = item.get("title");
            Object linkObj = item.get("link");
            String title = titleObj == null ? null : titleObj.toString();
            String link = linkObj == null ? null : linkObj.toString();
            if (title != null && !title.isEmpty() && link != null && !link.isEmpty()) {
                results.put(title, link);
            }
        }
        return results;
    }
 }
