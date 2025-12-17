package com.example.stage2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WebAnalyzer
 * ----------------------------------------------------
 * Core logic that:
 * 1. Fetches HTML for each URL
 * 2. Cleans it
 * 3. Counts keyword occurrences
 * 4. Produces WebPageResult objects
 * 
 * With Depth-Base Weighting:
 *   - root_score = page_score × 1.0
 *   - child_score = page_score × 0.5
 *   - site_score = root_score + Σ(child_scores)
 */
public class WebAnalyzer {

    private static final int MAX_CHILD_PAGES = 3;  // Limit child pages to analyze (3 direct children)

    /**
     * Analyze multiple URLs and return their results (without depth weighting).
     * This is the original method for backward compatibility.
     *
     * @param urls     List of URLs to fetch
     * @param keywords List of keywords to search
     * @return List<WebPageResult>
     */
    public static List<WebPageResult> analyze(List<String> urls, List<String> keywords) {
        return analyze(urls, keywords, false);
    }
    
    /**
     * Analyze multiple URLs with optional depth-based child page analysis.
     *
     * @param urls              List of URLs to fetch
     * @param keywords          List of keywords to search
     * @param analyzeChildPages Whether to analyze child pages (depth weighting)
     * @return List<WebPageResult>
     */
    public static List<WebPageResult> analyze(List<String> urls, List<String> keywords, boolean analyzeChildPages) {

        List<WebPageResult> results = new ArrayList<>();

        for (String url : urls) {
            // Analyze root page (depth = 0)
            WebPageResult pageResult = analyzeSinglePage(url, keywords, 0);
            
            // If depth weighting is enabled and this is a regular webpage, analyze child pages
            if (analyzeChildPages && !isYouTubeUrl(url) && pageResult.getRawHTML() != null) {
                System.out.println("[INFO] Analyzing child pages with depth weighting...");
                
                // Extract links from root page
                List<String> childUrls = extractLinks(pageResult.getRawHTML(), url);
                System.out.println("[INFO] Found " + childUrls.size() + " child links, analyzing up to " + MAX_CHILD_PAGES);
                
                int childCount = 0;
                for (String childUrl : childUrls) {
                    if (childCount >= MAX_CHILD_PAGES) break;
                    if (isYouTubeUrl(childUrl)) continue;  // Skip YouTube links in children
                    
                    System.out.println("  [CHILD " + (childCount + 1) + "] " + childUrl);
                    
                    // Analyze child page (depth = 1)
                    WebPageResult childResult = analyzeSinglePage(childUrl, keywords, 1);
                    childResult.setParentUrl(url);
                    
                    // Add child to parent
                    pageResult.addChildPage(childResult);
                    childCount++;
                }
                
                System.out.println("[INFO] Root score: " + pageResult.getWeightedScore() + 
                                   ", Site score (with children): " + pageResult.getSiteScore());
            }
            
            results.add(pageResult);
        }

        return results;
    }
    
    /**
     * Analyze a single page and return its result.
     *
     * @param url      URL to fetch
     * @param keywords List of keywords to search
     * @param depth    Page depth (0 = root, 1 = child)
     * @return WebPageResult
     */
    public static WebPageResult analyzeSinglePage(String url, List<String> keywords, int depth) {
        String depthLabel = depth == 0 ? "ROOT" : "CHILD";
        System.out.println("\n=== [" + depthLabel + "] Fetching: " + url + " ===");

        WebPageResult pageResult = new WebPageResult(url, depth);

            String cleanText = "";

            // Check if URL is a YouTube video
            if (isYouTubeUrl(url)) {
                System.out.println("[INFO] Detected YouTube URL - fetching transcript...");
                
                // Step 1: Fetch YouTube transcript
                String transcript = YouTubeTranscriptFetcher.fetchTranscript(url);
            pageResult.setRawHTML(transcript);
                
                // Step 2: Transcript is already clean text
                cleanText = transcript;
                pageResult.setCleanText(cleanText);
                
            } else {
                System.out.println("[INFO] Detected regular webpage - fetching HTML...");
                
                // Step 1: Fetch HTML
                String rawHTML = HTMLFetcher.fetchHTML(url);
                pageResult.setRawHTML(rawHTML);

                // Step 2: Clean HTML → plain text
                cleanText = TextPreprocessor.cleanHTML(rawHTML);
                pageResult.setCleanText(cleanText);
            }

            // Step 3: Count keywords
            Map<String, Integer> wordCountMap = WordCounter.countWords(cleanText, keywords);
            pageResult.setWordCountMap(wordCountMap);

        return pageResult;
    }
    
    /**
     * Extract internal links from HTML content.
     *
     * @param html    Raw HTML content
     * @param baseUrl The base URL for resolving relative links
     * @return List of absolute URLs found in the page
     */
    public static List<String> extractLinks(String html, String baseUrl) {
        Set<String> links = new HashSet<>();
        
        // Extract base domain for filtering internal links
        String baseDomain = extractDomain(baseUrl);
        
        // Pattern to find href attributes
        Pattern pattern = Pattern.compile("href=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        
        while (matcher.find()) {
            String link = matcher.group(1);
            
            // Skip empty, anchor-only, javascript, and mailto links
            if (link.isEmpty() || link.startsWith("#") || 
                link.startsWith("javascript:") || link.startsWith("mailto:")) {
                continue;
            }
            
            // Convert relative URLs to absolute
            String absoluteUrl = resolveUrl(link, baseUrl);
            
            // Only include links from the same domain (internal links)
            if (absoluteUrl != null && extractDomain(absoluteUrl).equals(baseDomain)) {
                links.add(absoluteUrl);
            }
        }
        
        return new ArrayList<>(links);
    }
    
    /**
     * Extract domain from URL.
     */
    private static String extractDomain(String url) {
        try {
            url = url.replaceFirst("^(https?://)", "");
            int slashIndex = url.indexOf('/');
            if (slashIndex > 0) {
                url = url.substring(0, slashIndex);
            }
            return url.toLowerCase();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Resolve a relative URL to an absolute URL.
     */
    private static String resolveUrl(String link, String baseUrl) {
        try {
            if (link.startsWith("http://") || link.startsWith("https://")) {
                return link;
        }

            // Get base (protocol + domain)
            int protocolEnd = baseUrl.indexOf("://");
            if (protocolEnd < 0) return null;
            
            String protocol = baseUrl.substring(0, protocolEnd + 3);
            String rest = baseUrl.substring(protocolEnd + 3);
            int slashIndex = rest.indexOf('/');
            String domain = slashIndex > 0 ? rest.substring(0, slashIndex) : rest;
            
            if (link.startsWith("//")) {
                return protocol.replace("://", ":") + link;
            } else if (link.startsWith("/")) {
                return protocol + domain + link;
            } else {
                // Relative to current path
                String basePath = slashIndex > 0 ? rest.substring(slashIndex) : "/";
                int lastSlash = basePath.lastIndexOf('/');
                String parentPath = lastSlash > 0 ? basePath.substring(0, lastSlash + 1) : "/";
                return protocol + domain + parentPath + link;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if a URL is a YouTube video URL.
     * 
     * @param url The URL to check
     * @return true if it's a YouTube URL, false otherwise
     */
    public static boolean isYouTubeUrl(String url) {
        return url.contains("youtube.com/watch") || 
               url.contains("youtu.be/") ||
               url.contains("youtube.com/embed/") ||
               url.contains("youtube.com/v/");
    }
}
