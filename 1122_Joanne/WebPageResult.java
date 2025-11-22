import java.util.Map;

/**
 * WebPageResult
 * ----------------------------------------------------
 * Holds the full analysis result for a single webpage.
 */
public class WebPageResult {

    private String url;
    private String rawHTML;
    private String cleanText;
    private Map<String, Integer> wordCountMap;

    public WebPageResult(String url) {
        this.url = url;
    }

    // ------------------ Setters ------------------

    public void setRawHTML(String rawHTML) {
        this.rawHTML = rawHTML;
    }

    public void setCleanText(String cleanText) {
        this.cleanText = cleanText;
    }

    public void setWordCountMap(Map<String, Integer> map) {
        this.wordCountMap = map;
    }

    // ------------------ Getters ------------------

    public String getUrl() {
        return url;
    }

    public String getRawHTML() {
        return rawHTML;
    }

    public String getCleanText() {
        return cleanText;
    }

    public Map<String, Integer> getWordCountMap() {
        return wordCountMap;
    }

    // For debugging / printing to console
    @Override
    public String toString() {
        return "URL: " + url + "\n"
             + "--------------------------------------------------\n"
             + "Word Count: " + wordCountMap + "\n";
    }
}
