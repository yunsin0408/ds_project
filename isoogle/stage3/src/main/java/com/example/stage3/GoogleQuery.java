package com.example.stage3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * GoogleQuery
 * Sends a query to Google Custom Search API and fetches titles and links from the JSON response
 */
public class GoogleQuery {

    private String apiKey;
    private String cx;
    private static final Pattern LINK_TITLE_PATTERN = Pattern.compile("\"title\"\\s*:\\s*\"([^\"]*)\"[^\\}]*\"link\"\\s*:\\s*\"([^\"]*)\"");

    public GoogleQuery() {
        // load from .env file
        try {
            java.io.File envFile = new java.io.File(".env");
            if (envFile.exists()) {
                java.util.Properties envProps = new java.util.Properties();
                envProps.load(new java.io.FileInputStream(envFile));
                apiKey = envProps.getProperty("GOOGLE_CSE_APIKEY");
                cx = envProps.getProperty("GOOGLE_CSE_CX");
            }
        } catch (Exception e) {
            // ignore
        }
        
        // Fall back to environment variables
        if (apiKey == null || cx == null) {
            apiKey = System.getenv("GOOGLE_CSE_APIKEY");
            cx = System.getenv("GOOGLE_CSE_CX");
        }
        
        // Fall back to properties file if not set
        if (apiKey == null || cx == null) {
            try {
                Properties prop = new Properties();
                prop.load(new FileInputStream("GoogleAPI.properties"));
                apiKey = prop.getProperty("google.api_key");
                cx = prop.getProperty("google.cx_id");
            } catch (IOException e) {
                throw new RuntimeException("Failed to load Google API properties. Set GOOGLE_CSE_APIKEY and GOOGLE_CSE_CX in .env, environment variables, or create GoogleAPI.properties");
            }
        }
        
        if (apiKey == null || cx == null) {
            throw new RuntimeException("Google API credentials not found. Set GOOGLE_CSE_APIKEY and GOOGLE_CSE_CX in .env or environment variables.");
        }
    }

    public HashMap<String, String> query(String query, int num) throws Exception {
        // Add "International Organization of Standardization" to the query
        String enhancedQuery = query + " International Organization of Standardization";
        String encoded_keyword = java.net.URLEncoder.encode(enhancedQuery, "UTF-8");
        String urlStr = "https://www.googleapis.com/customsearch/v1?key=" + apiKey + "&cx=" + cx + "&num=" + num + "&q=" + encoded_keyword;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        conn.disconnect();

        String json = sb.toString();
        HashMap<String, String> results = new HashMap<>();
        Matcher matcher = LINK_TITLE_PATTERN.matcher(json);
        int count = 0;
        
        while (matcher.find() && count < num) {
            String title = matcher.group(1);
            String link = matcher.group(2);
            
            if (!title.isEmpty() && !link.isEmpty()) {
                results.put(title, link);
                count++;
            }
        }

        return results;
    }
}
