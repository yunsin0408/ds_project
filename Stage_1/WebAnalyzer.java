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

    /**
     * Analyze multiple URLs and return their results.
     *
     * @param urls     List of URLs to fetch
     * @param keywords List of keywords to search
     * @return List<WebPageResult>
     */
    public static List<WebPageResult> analyze(List<String> urls, List<String> keywords) {

        List<WebPageResult> results = new ArrayList<>();

        for (String url : urls) {

            System.out.println("\n=== Fetching: " + url + " ===");

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

            // Store result
            results.add(pageResult);
        }

        return results;
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
