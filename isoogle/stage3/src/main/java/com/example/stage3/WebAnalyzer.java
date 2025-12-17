package com.example.stage3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebAnalyzer
 * Core logic that fetches HTML, cleans it, counts keyword occurrences, and produces WebPageResult objects
 */
public class WebAnalyzer {

    private static final int MAX_SUB_PAGES = 2;
    private static final long MAX_SUBLINK_TIME_MS = 3000;  // 3 seconds max for all sublinks per site
    private static final long MAX_SITE_TIME_MS = 5000;     // 5 seconds max per site (main + sublinks)

    /**
     * Analyze multiple URLs and return their results.
     */
    public static List<WebPageResult> analyzeSites(List<String> urls, List<String> keywords) {
        List<WebPageResult> results = new ArrayList<>();

        for (String url : urls) {
            long siteStartTime = System.currentTimeMillis();
            System.out.println("\n=== Fetching: " + url + " ===");
            WebPageResult rootResult = analyzeSinglePage(url, keywords);
            
            // Check if main page fetch already exceeded time limit
            if (System.currentTimeMillis() - siteStartTime > MAX_SITE_TIME_MS) {
                System.out.println("  [TIMEOUT] Main page took too long, skipping sublinks");
                results.add(rootResult);
                continue;
            }

            if (!isYouTubeUrl(url)) {
                System.out.println("  [Info] Scanning for sub-pages...");
                
                List<String> subLinks = HTMLFetcher.extractLinks(rootResult.getRawHTML(), url);
                
                int count = 0;
                for (String subLink : subLinks) {
                    if (count >= MAX_SUB_PAGES) break;
                    
                    // Check both sublink time and total site time
                    long elapsed = System.currentTimeMillis() - siteStartTime;
                    if (elapsed > MAX_SITE_TIME_MS) {
                        System.out.println("  [TIMEOUT] Site processing took too long, stopping at " + count + " sublinks");
                        break;
                    }
                    
                    System.out.println("    -> Fetching sub-page (" + (count + 1) + "/" + MAX_SUB_PAGES + "): " + subLink);
                    
                    WebPageResult subResult = analyzeSinglePage(subLink, keywords);
                    rootResult.addChild(subResult);
                    count++;
                }
                System.out.println("  [Info] Finished sub-pages. Found: " + count);
            }

            results.add(rootResult);
        }

        return results;
    }

    private static WebPageResult analyzeSinglePage(String url, List<String> keywords) {
        WebPageResult pageResult = new WebPageResult(url);
        String cleanText = "";

        if (isYouTubeUrl(url)) {
            System.out.println("[INFO] Detected YouTube URL - fetching transcript...");
            String transcript = YouTubeTranscriptFetcher.fetchTranscript(url);
            pageResult.setRawHTML(transcript);
            cleanText = transcript;
            pageResult.setCleanText(cleanText);
        } else {
            System.out.println("[INFO] Detected regular webpage - fetching HTML...");
            String rawHTML = HTMLFetcher.fetchHTML(url);
            pageResult.setRawHTML(rawHTML);
            cleanText = TextPreprocessor.cleanHTML(rawHTML);
            pageResult.setCleanText(cleanText);
        }

        Map<String, Integer> wordCountMap = WordCounter.countWords(cleanText, keywords);
        pageResult.setWordCountMap(wordCountMap);

        return pageResult;
    }

    /**
     * Analyze multiple URLs from Custom Search JSON API and return results.
     */
    public static List<SearchResult> analyzeGoogleRankedSites(String userKeywords, List<String> keywords, Map<String, Integer> keywordWeights, int numResults) throws Exception {
        
        GoogleQuery gq = new GoogleQuery(); 
        String fullQuery = userKeywords + " International Organization of Standardization";
        HashMap<String, String> urlsToAnalyze = gq.query(fullQuery, numResults); 

        List<String> urls = new ArrayList<>(urlsToAnalyze.values());
        List<WebPageResult> analyzedResults = analyzeSites(urls, keywords);

        List<SearchResult> searchResults = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, String> entry : urlsToAnalyze.entrySet()) {
            String title = entry.getKey();
            String url = entry.getValue();

            WebPageResult root = analyzedResults.get(index);
            
            int totalSiteScore = calculatePageScore(root, keywordWeights);
            
            // Aggregate all text: main page + sublinks
            StringBuilder aggregatedContent = new StringBuilder(root.getCleanText());
            
            for (WebPageResult child : root.getChildren()) {
                totalSiteScore += calculatePageScore(child, keywordWeights);
                // Append sublink content with separator
                aggregatedContent.append("\n\n").append(child.getCleanText());
            }
            
            SearchResult searchResult = new SearchResult(title, url);
            searchResult.setRankScore(totalSiteScore);
            String agg = aggregatedContent.toString();
            searchResult.setContent(agg);

            // Log if the aggregated content is empty (fetch failures or no textual content)
            if (agg == null || agg.trim().isEmpty()) {
                System.out.println("[WARN] Empty aggregated content for URL: " + url + " (title='" + title + "')");
            }

            searchResults.add(searchResult);
            index++;
        }

        Collections.sort(searchResults, Comparator.comparingDouble(SearchResult::getRankScore).reversed());

        return searchResults;
    }

    private static int calculatePageScore(WebPageResult page, Map<String, Integer> keywordWeights) {
        int score = 0;
        Map<String, Integer> counts = page.getWordCountMap(); 
        
        for (String keyword : keywordWeights.keySet()) {
            int count = counts.getOrDefault(keyword, 0);
            int weight = keywordWeights.get(keyword);
            score += (count * weight);
        }
        return score;
    }

    private static boolean isYouTubeUrl(String url) {
        return url.contains("youtube.com/watch") || 
               url.contains("youtu.be/") ||
               url.contains("youtube.com/embed/") ||
               url.contains("youtube.com/v/");
    }
}
