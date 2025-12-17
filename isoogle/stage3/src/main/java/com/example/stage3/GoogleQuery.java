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
        // Try environment variables first
        apiKey = System.getenv("GOOGLE_CSE_APIKEY");
        cx = System.getenv("GOOGLE_CSE_CX");
        
        // Fall back to properties file if env vars not set
        if (apiKey == null || cx == null) {
            try {
                Properties prop = new Properties();
                prop.load(new FileInputStream("GoogleAPI.properties"));
                apiKey = prop.getProperty("google.api_key");
                cx = prop.getProperty("google.cx_id");
            } catch (IOException e) {
                throw new RuntimeException("Failed to load Google API properties. Set GOOGLE_CSE_APIKEY and GOOGLE_CSE_CX environment variables or create GoogleAPI.properties");
            }
        }
        
        if (apiKey == null || cx == null) {
            throw new RuntimeException("Google API credentials not found. Set GOOGLE_CSE_APIKEY and GOOGLE_CSE_CX environment variables.");
        }
    }

    public HashMap<String, String> query(String query, int num) throws Exception {
        // Append the ISO phrase only when appropriate.
        // Controlled by environment variable `GOOGLE_CSE_APPEND_ISO` (true/false),
        // or by `SEARCH_BIAS_ISO=true` to force bias toward ISO pages.
        String enhancedQuery = query == null ? "" : query.trim();
        boolean appendIso = false;
        String env = System.getenv("GOOGLE_CSE_APPEND_ISO");
        String biasIso = System.getenv("SEARCH_BIAS_ISO");
        if (env != null && (env.equalsIgnoreCase("1") || env.equalsIgnoreCase("true") || env.equalsIgnoreCase("yes"))) {
            appendIso = true;
        }

        String low = enhancedQuery.toLowerCase();
        if (low.contains("iso") || low.contains("standard") || low.contains("international") || low.contains("organization")) {
            // If the query already mentions ISO-related terms, it's appropriate to append (or it's redundant but harmless)
            appendIso = true;
        }

        // If SEARCH_BIAS_ISO is explicitly enabled, force append regardless of other checks
        if ((biasIso != null && (biasIso.equalsIgnoreCase("1") || biasIso.equalsIgnoreCase("true") || biasIso.equalsIgnoreCase("yes"))) || (appendIso && !low.contains("international organization of standardization"))) {
            enhancedQuery = enhancedQuery + " International Organization of Standardization";
        }
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
