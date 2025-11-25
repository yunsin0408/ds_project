import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main.java
 * ----------------------------------------------------
 * Stage 1+2: Site Ranking Version
 * - Defines target Sites and Keywords
 * - Calls WebAnalyzer.analyzeSites()
 * - Calculates Site Score (Main Page + Sub Pages)
 * - Prints Tree Structure Ranking

 */
public class Main {
    // Define weights map globally for easy access
    private static final Map<String, Integer> KEYWORD_WEIGHTS = new HashMap<>();


    public static void main(String[] args) {
        

        // ----------------------------------------------------
        // 1. Hard-coded URLs (modify freely)
        // ----------------------------------------------------
        List<String> urls = new ArrayList<>();
        urls.add("https://www.iso.org/home.html");
        urls.add("https://en.wikipedia.org/wiki/International_Organization_for_Standardization");
        urls.add("https://www.cyberark.com/what-is/iso/");
        
        // YouTube URLs (with transcript support)
        urls.add("https://youtu.be/a4cyMAIyWIQ?si=qxS0dFr3dVrgt_Xh");
        urls.add("https://youtu.be/Nhlv_3-dQSk?si=qm_Nop7a2pJCkdeP");
        // Add more…

        // ----------------------------------------------------
        // 2. Hard-coded keywords
        // ----------------------------------------------------
        List<String> keywords = new ArrayList<>();
        keywords.add("ISO");
        keywords.add("Standard");
        keywords.add("Sustain");
        keywords.add("Certificate");
        // Add more…

        KEYWORD_WEIGHTS.put("ISO", 4);
        KEYWORD_WEIGHTS.put("Standard", 3);
        KEYWORD_WEIGHTS.put("Sustain", 2);
        KEYWORD_WEIGHTS.put("Certificate", 1);


        // ----------------------------------------------------
        // 3. Run analysis
        // ----------------------------------------------------
        System.out.println("Starting Stage 2 Site Analysis...");
        List<WebPageResult> siteResults = WebAnalyzer.analyzeSites(urls, keywords);

        // ----------------------------------------------------
        // 4. Calculate Scores (Site Score = Root Score + SubPages Score)
        // ----------------------------------------------------
        for (WebPageResult root : siteResults) {
            // 4.1 Calculate Root Page Score
            int rootScore = calculatePageScore(root);
            root.setScore(rootScore);
            
            // 4.2 Add Sub-pages Score to Root Score (Accumulate)
            int totalSiteScore = rootScore;
            
            for (WebPageResult child : root.getChildren()) {
                int childScore = calculatePageScore(child);
                child.setScore(childScore);
                totalSiteScore += childScore; // Add child score to total
            }
            
            // Update root score to represent the TOTAL Site Score
            // Note: In a real tree, you might want to store 'siteScore' separately,
            // but here we overwrite score for sorting purposes.
            root.setScore(totalSiteScore); 
        }


        // ----------------------------------------------------
        // 5. Sort by Total Site Score
        // ----------------------------------------------------
        siteResults.sort((a, b) -> b.getScore() - a.getScore()); // Descending


                // ----------------------------------------------------
        // 6. Print Tree Structure Ranking
        // ----------------------------------------------------
        System.out.println("\n====================================================");
        System.out.println("                Site Ranking Result");
        System.out.println("====================================================");

        int rank = 1;
        for (WebPageResult root : siteResults) {
            System.out.println("Rank #" + (rank++) + " [Total Score: " + root.getScore() + "]");
            System.out.println("Site: " + root.getUrl());
            
            // Print Root Details
            // Only print keyword counts if they are > 0 to keep it clean
            System.out.print("  └─ Main Page Score: " + calculatePageScore(root)); // Recalculate pure page score for display
            System.out.println(" | Counts: " + root.getWordCountMap());

            // Print Children Details
            List<WebPageResult> children = root.getChildren();
            if (!children.isEmpty()) {
                System.out.println("  └─ Sub-pages (" + children.size() + "):");
                for (WebPageResult child : children) {
                    System.out.println("      ├─ " + child.getUrl());
                    System.out.println("      │   Score: " + child.getScore() + " | Counts: " + child.getWordCountMap());
                }
            } else {
                System.out.println("  └─ (No sub-pages analyzed)");
            }
            System.out.println("----------------------------------------------------");
        }
    }

    /**
     * Helper to calculate score for a single page based on keyword weights.
     */
    private static int calculatePageScore(WebPageResult page) {
        int score = 0;
        Map<String, Integer> counts = page.getWordCountMap();
        
        for (String keyword : KEYWORD_WEIGHTS.keySet()) {
            int count = counts.getOrDefault(keyword, 0);
            int weight = KEYWORD_WEIGHTS.get(keyword);
            score += (count * weight);
        }
        return score;
    }
}