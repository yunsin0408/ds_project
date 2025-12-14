package com.example.stage3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTMLFetcher
 * Responsible for connecting to a URL and downloading the raw HTML content
 */
public class HTMLFetcher {

    /**
     * Fetches the raw HTML content from the given URL.
     */
    public static String fetchHTML(String urlString) {
        StringBuilder html = new StringBuilder();
        BufferedReader reader = null;

        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(1500);  // 1.5 second connect timeout
            conn.setReadTimeout(2500);     // 2.5 second read timeout

            reader = new BufferedReader(
                        new InputStreamReader(
                            conn.getInputStream(), "UTF-8"
                        )
                     );

            String line;
            while ((line = reader.readLine()) != null) {
                html.append(line).append("\n");
            }

        } catch (java.net.SocketTimeoutException e) {
            System.out.println("[TIMEOUT] Skipping slow URL: " + urlString);
            return "";
        } catch (Exception e) {
            System.out.println("[ERROR] Unable to fetch URL: " + urlString);
            return "";
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (Exception ignore) {}
        }

        return html.toString();
    }

    /**
     * Extracts links from HTML content and filters for same-domain links.
     */
    public static List<String> extractLinks(String htmlContent, String baseUrl) {
        List<String> links = new ArrayList<>();
        
        String baseDomain = getDomain(baseUrl);
        if (baseDomain == null) {
            return links;
        }

        Pattern pattern = Pattern.compile("<a\\s+(?:[^>]*?\\s+)?href=([\"'])(.*?)\\1");
        Matcher matcher = pattern.matcher(htmlContent);

        while (matcher.find()) {
            String rawLink = matcher.group(2).trim();
            
            if (rawLink.startsWith("#") || rawLink.startsWith("javascript") || rawLink.startsWith("mailto")) {
                continue;
            }

            try {
                URL absoluteUrl = new URL(new URL(baseUrl), rawLink);
                String absoluteUrlString = absoluteUrl.toExternalForm();
                
                int queryIndex = absoluteUrlString.indexOf('?');
                if (queryIndex != -1) {
                    absoluteUrlString = absoluteUrlString.substring(0, queryIndex);
                }
                
                if (baseDomain.equals(getDomain(absoluteUrlString))) {
                    if (!absoluteUrlString.equals(baseUrl) && !links.contains(absoluteUrlString)) {
                        links.add(absoluteUrlString);
                    }
                }
                
            } catch (Exception e) {
                // Ignore invalid URL parsing errors
            }
        }
        
        return links;
    }
    
    /**
     * Extracts the host domain from a full URL.
     */
    public static String getDomain(String urlString) {
        try {
            URL url = new URL(urlString);
            return url.getHost();
        } catch (Exception e) {
            return null;
        }
    }
}
