package com.example.stage2;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * WebPageResult
 * ----------------------------------------------------
 * Holds the full analysis result for a single webpage.
 * 
 * With Depth-Base Weighting:
 *   - depth 0 (root): weight = 1.0
 *   - depth 1 (child): weight = 0.5
 *   - site_score = root_score + Σ(child_scores)
 */
public class WebPageResult {

    private String url;
    private String rawHTML;
    private String cleanText;
    private Map<String, Integer> wordCountMap;
    private int score = 0;
    
    // Depth-Base Weighting fields
    private int depth = 0;                    // 0 = root, 1 = child
    private double depthWeight = 1.0;         // 1.0 for root, 0.5 for child
    private double weightedScore = 0.0;       // score * depthWeight
    private List<WebPageResult> childPages;   // Child pages (for root pages)
    private String parentUrl = null;          // Parent URL (for child pages)

    public WebPageResult(String url) {
        this.url = url;
        this.childPages = new ArrayList<>();
    }
    
    public WebPageResult(String url, int depth) {
        this.url = url;
        this.depth = depth;
        this.depthWeight = getWeightForDepth(depth);
        this.childPages = new ArrayList<>();
    }
    
    /**
     * Returns the weight multiplier based on depth.
     * root (depth 0): 1.0
     * child (depth 1): 0.5
     * deeper: 0.25, 0.125, etc. (halves each level)
     */
    private static double getWeightForDepth(int depth) {
        if (depth == 0) return 1.0;
        if (depth == 1) return 0.5;
        return Math.pow(0.5, depth);  // For deeper levels if needed
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

    public void setScore(int score) {
        this.score = score;
        this.weightedScore = score * this.depthWeight;
    }
    
    public void setDepth(int depth) {
        this.depth = depth;
        this.depthWeight = getWeightForDepth(depth);
        // Recalculate weighted score if score is already set
        this.weightedScore = this.score * this.depthWeight;
    }
    
    public void setParentUrl(String parentUrl) {
        this.parentUrl = parentUrl;
    }
    
    public void addChildPage(WebPageResult child) {
        this.childPages.add(child);
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

    public int getScore() {
        return score;
    }
    
    public int getDepth() {
        return depth;
    }
    
    public double getDepthWeight() {
        return depthWeight;
    }
    
    public double getWeightedScore() {
        return weightedScore;
    }
    
    public List<WebPageResult> getChildPages() {
        return childPages;
    }
    
    public String getParentUrl() {
        return parentUrl;
    }
    
    /**
     * Calculate the total site score including all child pages.
     * site_score = root_score × 1.0 + Σ(child_score × 0.5)
     */
    public double getSiteScore() {
        double siteScore = this.weightedScore;
        for (WebPageResult child : childPages) {
            siteScore += child.getWeightedScore();
        }
        return siteScore;
    }

    // For debugging / printing to console
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("URL: ").append(url).append("\n");
        sb.append("Depth: ").append(depth).append(" (weight: ").append(depthWeight).append(")\n");
        sb.append("Raw Score: ").append(score).append("\n");
        sb.append("Weighted Score: ").append(String.format("%.2f", weightedScore)).append("\n");
        if (!childPages.isEmpty()) {
            sb.append("Site Score (with children): ").append(String.format("%.2f", getSiteScore())).append("\n");
            sb.append("Child Pages: ").append(childPages.size()).append("\n");
        }
        sb.append("--------------------------------------------------\n");
        sb.append("Word Count: ").append(wordCountMap).append("\n");
        return sb.toString();
    }
}
