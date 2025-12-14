import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Main.java
 * ----------------------------------------------------
 * Stage1 + Stage2 consolidated minimal version.
 *
 * - Manually define URLs & keywords
 * - Call WebAnalyzer
 * - Print results and ranking
 */
public class Main {

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

        // ----------------------------------------------------
        // 3. Run analysis
        // ----------------------------------------------------
        List<WebPageResult> results = WebAnalyzer.analyze(urls, keywords);

        // ----------------------------------------------------
        // 4. Results (before ranking)
        // ----------------------------------------------------


        for (WebPageResult r : results) {
            Map<String, Integer> countMap = r.getWordCountMap();

            int isoCount         = countMap.getOrDefault("ISO", 0);
            int standardCount    = countMap.getOrDefault("Standard", 0);
            int sustainCount     = countMap.getOrDefault("Sustain", 0);
            int certificateCount = countMap.getOrDefault("Certificate", 0);

            // Weighted formula
            int score =
                    (isoCount * 4) +
                    (standardCount * 3) +
                    (sustainCount * 2) +
                    (certificateCount * 1);

            r.setScore(score);
        }

        // ----------------------------------------------------
        // 5. Sort by total keyword count (simple scoring)
        // ----------------------------------------------------
        results.sort(new Comparator<WebPageResult>() {
            @Override
            public int compare(WebPageResult a, WebPageResult b) {
                return b.getScore() - a.getScore();  // descending
            }

        });

        // ----------------------------------------------------
        // 6. Print ranking
        // ----------------------------------------------------
        System.out.println("\n====================================================");
        System.out.println("                Ranking Result");
        System.out.println("====================================================");

        int rank = 1;
        for (WebPageResult r : results) {
            

            System.out.println(
                "# " + (rank++) + 
                " | " + r.getUrl() +
                " | SCORE = " + r.getScore()+
                " | Counts = " + r.getWordCountMap()
            );
        }
    }
}
