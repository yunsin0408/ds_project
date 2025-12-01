import java.util.ArrayList;
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
    //取最大子網頁數量
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
