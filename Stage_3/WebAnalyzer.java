import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebAnalyzer
 * ----------------------------------------------------
 * Core logic that:
 * 1. Fetches HTML for each URL
 * 2. Cleans it
 * 3. Counts keyword occurrences
 * 4. Produces WebPageResult objects
 */
public class WebAnalyzer {

    // Linit for max sub-pages number
    private static final int MAX_SUB_PAGES = 3;

    /**
     * Analyze multiple URLs and return their results.
     *
     * @param urls     List of URLs to fetch
     * @param keywords List of keywords to search
     * @return List<WebPageResult>(each is a root node containing children)
     */
    public static List<WebPageResult> analyzeSites(List<String> urls, List<String> keywords) {

        List<WebPageResult> results = new ArrayList<>();

        for (String url : urls) {

            System.out.println("\n=== Fetching: " + url + " ===");
            // 1. Analyze the Main Page (Root)
            WebPageResult rootResult = analyzeSinglePage(url, keywords);

            // 2. If it's a regular website (not YouTube), crawl sub-pages
            if (!isYouTubeUrl(url)) {
                System.out.println("  [Info] Scanning for sub-pages...");
                
                // Extract links restricted to the same domain
                List<String> subLinks = HTMLFetcher.extractLinks(rootResult.getRawHTML(), url);
                
                int count = 0;
                for (String subLink : subLinks) {
                    if (count >= MAX_SUB_PAGES) break; // Limit the number of sub-pages
                    System.out.println("    -> Fetching sub-page (" + (count + 1) + "/" + MAX_SUB_PAGES + "): " + subLink);
                    
                    // Analyze the sub-page
                    WebPageResult subResult = analyzeSinglePage(subLink, keywords);
                    
                    // Add to root's children
                    rootResult.addChild(subResult);
                    count++;
                }
                System.out.println("  [Info] Finished sub-pages. Found: " + count);
            }

            results.add(rootResult);
        }

        return results;
    }




            //---Helper method to fetch and analyze a single URL (without recursion).

            private static WebPageResult analyzeSinglePage(String url, List<String> keywords) {

            WebPageResult pageResult = new WebPageResult(url);

            String cleanText = "";

            // Check if URL is a YouTube video
            if (isYouTubeUrl(url)) {
                System.out.println("[INFO] Detected YouTube URL - fetching transcript...");
                
                // Step 1: Fetch YouTube transcript
                String transcript = YouTubeTranscriptFetcher.fetchTranscript(url);
                pageResult.setRawHTML(transcript);  // Store transcript as "raw" content
                
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
     * Analyze multiple URLs from Custom Search JSON API and return results (as SearchResult).
     *
     * @param userKeywords User input keywords for Google search
     * @param keywords List of keywords to search
     * @param keywordWeights Map of keywords and their weights for scoring
     * @param numResults Number of Google search results to fetch
     * @return List<SearchResult> (container for saving results)
     */
    public static List<SearchResult> analyzeGoogleRankedSites(String userKeywords, List<String> keywords, Map<String, Integer> keywordWeights, int numResults) throws Exception {
        
        // 1. Connect to API and fetch the Urls & Topics
        GoogleQuery gq = new GoogleQuery(); 
        String fullQuery = userKeywords + " International Organization of Standardization";
        HashMap<String, String> urlsToAnalyze = gq.query(fullQuery, numResults); 
        
        // Add manual YouTube test URL to ensure YouTubeAPI work correctly
        // String testYouTubeUrl = "https://youtu.be/Nhlv_3-dQSk?si=qm_Nop7a2pJCkdeP";
        // if (!urlsToAnalyze.containsValue(testYouTubeUrl)) {
        //     urlsToAnalyze.put("MANUAL_YOUTUBE_TEST", testYouTubeUrl);
        // }

        // 2. Analyze the fetched URLs (use the analyzeSites method)
        List<String> urls = new ArrayList<>(urlsToAnalyze.values());
        List<WebPageResult> analyzedResults = analyzeSites(urls, keywords);
        

        // 3. Map results to SearchResult objects and CALCULATE TOTAL SCORE
        List<SearchResult> searchResults = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, String> entry : urlsToAnalyze.entrySet()) {
            String title = entry.getKey();
            String url = entry.getValue();

            WebPageResult root = analyzedResults.get(index);
            
            // Cal the total score as ParentScore + all RootScore
            int totalSiteScore = calculatePageScore(root, keywordWeights);
            
            for (WebPageResult child : root.getChildren()) {
                totalSiteScore += calculatePageScore(child, keywordWeights);
            }
            
            SearchResult searchResult = new SearchResult(title, url);

            double finalScore = totalSiteScore; 
            String cleanText = root.getCleanText(); 

            searchResult.setRankScore(finalScore);
            searchResult.setContent(cleanText);

            searchResults.add(searchResult);
            index++;
        }

        // 4. Sort SearchResults by RankScore descending
        Collections.sort(searchResults, Comparator.comparingDouble(SearchResult::getRankScore).reversed());

        return searchResults;
    }
    /**
     * Helper to calculate score for a single page based on keyword weights.
     */
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
    /**
     * Checks if a URL is a YouTube video URL.
     * 
     * @param url The URL to check
     * @return true if it's a YouTube URL, false otherwise
     */
    private static boolean isYouTubeUrl(String url) {
        return url.contains("youtube.com/watch") || 
               url.contains("youtu.be/") ||
               url.contains("youtube.com/embed/") ||
               url.contains("youtube.com/v/");
    }

}
