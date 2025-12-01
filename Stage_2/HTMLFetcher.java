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
            //e.printStackTrace();
            return "";       // return empty, avoid crashing later
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (Exception ignore) {}
        }

        return html.toString();
    }




    // ----------------------------------------------------
    // Stage 2: New Link Extraction Functionality
    // ----------------------------------------------------

    /**
     * Extracts links from HTML content and filters for same-domain links.
     *
     * @param htmlContent The raw HTML string.
     * @param baseUrl The base URL of the current page (used for domain filtering and resolving relative links).
     * @return List of unique, absolute URLs within the same domain.
     */
    public static List<String> extractLinks(String htmlContent, String baseUrl) {
        List<String> links = new ArrayList<>();
        
        // 取得根網址的 Domain (e.g., iso.org)
        String baseDomain = getDomain(baseUrl);
        if (baseDomain == null) {
            return links;
        }

        // Regex pattern for <a> tags' href attribute
        // 尋找 <a href="..."> 中的內容，並排除 anchor (#) 或 mailto
        Pattern pattern = Pattern.compile("<a\\s+(?:[^>]*?\\s+)?href=([\"'])(.*?)\\1");
        Matcher matcher = pattern.matcher(htmlContent);

        while (matcher.find()) {
            String rawLink = matcher.group(2).trim();
            
            // 排除 anchor 連結、javascript 連結或 mailto 連結
            if (rawLink.startsWith("#") || rawLink.startsWith("javascript") || rawLink.startsWith("mailto")) {
                continue;
            }

            try {
                // 將相對路徑解析為絕對路徑 (e.g., /home/index.html -> https://www.example.com/home/index.html)
                URL absoluteUrl = new URL(new URL(baseUrl), rawLink);
                String absoluteUrlString = absoluteUrl.toExternalForm();
                
                // 排除查詢參數 (Query Parameters) 避免重複爬取相同頁面
                // 僅保留 URL 的路徑部分，如果需要可以取消此步驟以保留參數
                int queryIndex = absoluteUrlString.indexOf('?');
                if (queryIndex != -1) {
                    absoluteUrlString = absoluteUrlString.substring(0, queryIndex);
                }
                
                // 檢查是否屬於同一個 Domain (Stage 2 核心需求)
                if (baseDomain.equals(getDomain(absoluteUrlString))) {
                    // 確保連結不是目標網站的根網址 itself
                    if (!absoluteUrlString.equals(baseUrl) && !links.contains(absoluteUrlString)) {
                        links.add(absoluteUrlString);
                    }
                }
                
            } catch (Exception e) {
                // 忽略無效的 URL 解析錯誤
            }
        }
        
        return links;
    }
    
    /**
     * Extracts the host domain (e.g., "www.iso.org" or "iso.org") from a full URL.
     * * @param urlString The full URL.
     * @return The domain string, or null if parsing fails.
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
