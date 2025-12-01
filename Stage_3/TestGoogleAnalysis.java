import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * TestGoogleAnalysis
 * ----------------------------------------------------
 * 1. Prompts user for search keywords and number of URLs
 * 2. Calls WebAnalyzer.analyzeGoogleRankedSites()
 * 3. Calculates scores based on hard-coded keyword weights
 * 4. Ranks and prints the results
 */

public class TestGoogleAnalysis {

    // ----------------------------------------------------
    // Hard-coded keywords
    // ----------------------------------------------------
    private static final Map<String, Integer> KEYWORD_WEIGHTS = new HashMap<>();
    static {
        KEYWORD_WEIGHTS.put("ISO", 4);
        KEYWORD_WEIGHTS.put("Standard", 3);
        KEYWORD_WEIGHTS.put("Sustain", 2);
        KEYWORD_WEIGHTS.put("Certificate", 1);
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {

            // ----------------------------------------------------
            // 1. Enter search keywords and number of URLs
            // ----------------------------------------------------
            System.out.print("Enter Keyword for Search: ");
            String userKeywords = scanner.nextLine();
            System.out.print("Enter Number of Urls for Search: ");
            int numResults = Integer.parseInt(scanner.nextLine());

            // ----------------------------------------------------
            // 2. Call the urls from GoogleQuery and analyze
            // ----------------------------------------------------
            List<String> keywords = new ArrayList<>(KEYWORD_WEIGHTS.keySet());
            List<SearchResult> searchResults = WebAnalyzer.analyzeGoogleRankedSites(userKeywords, keywords, KEYWORD_WEIGHTS, numResults);

            // ----------------------------------------------------
            // 3. Rank the results based on calculated scores
            // ----------------------------------------------------
            searchResults.sort((a, b) -> b.getRankScore() - a.getRankScore());

            // ----------------------------------------------------
            // 4. Print the results
            // ----------------------------------------------------
            System.out.println("\n====================================================");
            System.out.println("            Google Ranked Sites Result");
            System.out.println("====================================================");

            int rank = 1;
            for (SearchResult sr : searchResults) {
                System.out.println("Rank #" + (rank++) + " [RankScore: " + sr.getRankScore() + "]");
                System.out.println("Site Name : " + sr.getSiteName());
                System.out.println("URL       : " + sr.getUrl());
                System.out.println("Content   : " + (sr.getContent() != null ? sr.getContent().substring(0, Math.min(200, sr.getContent().length())) + "..." : "No Content"));
                System.out.println("----------------------------------------------------");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper to calculate score for a single page based on keyword weights.
     */
    private static int countOccurrences(String text, String keyword) {
        int count = 0;
        int index = 0;
        while ((index = text.toLowerCase().indexOf(keyword.toLowerCase(), index)) != -1) {
            count++;
            index += keyword.length();
        }
        return count;
    }
}
