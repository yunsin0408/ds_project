package com.example.stage1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * HTMLFetcher
 * ----------------------------------------------------
 * Responsible for connecting to a URL and downloading
 * the raw HTML content as a String.
 * 
 * Provides:
 *   - Basic HTTP GET request
 *   - Customizable User-Agent
 *   - UTF-8 reading support
 */
public class HTMLFetcher {

    /**
     * Fetches the raw HTML content from the given URL.
     *
     * @param urlString The webpage URL
     * @return Raw HTML content as String
     */
    public static String fetchHTML(String urlString) {
        StringBuilder html = new StringBuilder();
        BufferedReader reader = null;

        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();

            // Set a fake browser user agent to avoid rejection
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            reader = new BufferedReader(
                        new InputStreamReader(
                            conn.getInputStream(), "UTF-8"
                        )
                     );

            String line;
            while ((line = reader.readLine()) != null) {
                html.append(line).append("\n");
            }

        } catch (Exception e) {
            System.out.println("[ERROR] Unable to fetch URL: " + urlString);
            e.printStackTrace();
            return "";       // return empty, avoid crashing later
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (Exception ignore) {}
        }

        return html.toString();
    }
}
