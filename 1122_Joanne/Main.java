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
        

        // ----------------------------------------------------
        // 2. Hard-coded keywords
        // ----------------------------------------------------
        List<String> keywords = new ArrayList<>();
        keywords.add("ISO");
        keywords.add("standard");
        keywords.add("sustain");
        keywords.add("certificate");
        

        // ----------------------------------------------------
        // 3. Run analysis
        // ----------------------------------------------------
        List<WebPageResult> results = WebAnalyzer.analyze(urls, keywords);

        // ----------------------------------------------------
        // 4. Print results (before ranking)
        // ----------------------------------------------------
        System.out.println("\n====================================================");
        System.out.println("          Raw Web Page Analysis Result");
        System.out.println("====================================================");

        for (WebPageResult r : results) {
            System.out.println(r);
        }

        // ----------------------------------------------------
        // 5. Sort by total keyword count (simple scoring)
        // ----------------------------------------------------
        results.sort(new Comparator<WebPageResult>() {
            @Override
            public int compare(WebPageResult a, WebPageResult b) {
                return sum(b.getWordCountMap()) - sum(a.getWordCountMap());
            }

            private int sum(Map<String, Integer> map) {
                return map.values().stream().mapToInt(Integer::intValue).sum();
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
            int totalCount = r.getWordCountMap()
                    .values().stream().mapToInt(i -> i).sum();

            System.out.println(
                "# " + (rank++) + " | " + r.getUrl() +
                " | SCORE = " + totalCount +
                " | Counts = " + r.getWordCountMap()
            );
        }
    }
}
