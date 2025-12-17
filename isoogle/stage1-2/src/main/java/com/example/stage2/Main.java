package com.example.stage2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Main.java
 * ----------------------------------------------------
 * Stage1 + Stage2 consolidated version with Depth-Base Weighting.
 *
 * Depth-Base Weighting:
 *   - root_score = page_score × 1.0
 *   - child_score = page_score × 0.5
 *   - site_score = root_score + Σ(child_scores)
 *
 * - Manually define URLs & keywords
 * - Call WebAnalyzer with depth weighting
 * - Print results and ranking
 */
public class Main {

    // Enable/disable depth-based child page analysis
    private static final boolean ENABLE_DEPTH_WEIGHTING = true;

    public static void main(String[] args) {
        
        System.out.println("====================================================");
        System.out.println("      Web Analyzer with Depth-Base Weighting");
        System.out.println("====================================================");
        System.out.println("Depth Weighting: " + (ENABLE_DEPTH_WEIGHTING ? "ENABLED" : "DISABLED"));
        System.out.println();
        System.out.println("Score Formulation:");
        System.out.println("  Stage 1 (Page): score = Σ(count × weight)");
        System.out.println("  Stage 2 (Site): site_score = root_score + Σ(child_scores)");
        System.out.println();
        System.out.println("Depth-Base Weighting:");
        System.out.println("  - Root page (depth 0): weight = 1.0");
        System.out.println("  - Child pages (depth 1): weight = 0.5, max 3 children");
        System.out.println("====================================================\n");

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
        // 3. Run analysis (with or without depth weighting)
        // ----------------------------------------------------
        List<WebPageResult> results = WebAnalyzer.analyze(urls, keywords, ENABLE_DEPTH_WEIGHTING);

        // ----------------------------------------------------
        // 4. Calculate scores with keyword weights
        // ----------------------------------------------------
        for (WebPageResult r : results) {
            // Calculate score for root page
            calculateScore(r);
            
            // Calculate scores for child pages if depth weighting is enabled
            if (ENABLE_DEPTH_WEIGHTING) {
                for (WebPageResult child : r.getChildPages()) {
                    calculateScore(child);
                }
            }
        }

        // ----------------------------------------------------
        // 5. Sort by site score (includes child pages if enabled)
        // ----------------------------------------------------
        results.sort(new Comparator<WebPageResult>() {
            @Override
            public int compare(WebPageResult a, WebPageResult b) {
                // Use site score for sorting (includes weighted child scores)
                double scoreA = ENABLE_DEPTH_WEIGHTING ? a.getSiteScore() : a.getWeightedScore();
                double scoreB = ENABLE_DEPTH_WEIGHTING ? b.getSiteScore() : b.getWeightedScore();
                return Double.compare(scoreB, scoreA);  // descending
            }
        });

        // ----------------------------------------------------
        // 6. Print ranking
        // ----------------------------------------------------
        System.out.println("\n====================================================");
        System.out.println("                Ranking Result");
        System.out.println("====================================================");
        System.out.println("(Sorted by " + (ENABLE_DEPTH_WEIGHTING ? "Site Score" : "Weighted Score") + ")\n");

        int rank = 1;
        for (WebPageResult r : results) {
            double siteScore = ENABLE_DEPTH_WEIGHTING ? r.getSiteScore() : r.getWeightedScore();

            System.out.println("# " + (rank++) + " | " + r.getUrl());
            System.out.println("    Raw Score: " + r.getScore() + 
                               " × " + r.getDepthWeight() + " = " + 
                               String.format("%.2f", r.getWeightedScore()));
            
            if (ENABLE_DEPTH_WEIGHTING && !r.getChildPages().isEmpty()) {
                System.out.println("    Child Pages: " + r.getChildPages().size());
                double childSum = 0;
                for (WebPageResult child : r.getChildPages()) {
                    childSum += child.getWeightedScore();
                }
                System.out.println("    Child Scores Sum: " + String.format("%.2f", childSum));
            }
            
            System.out.println("    SITE SCORE: " + String.format("%.2f", siteScore));
            System.out.println("    Counts: " + r.getWordCountMap());
            System.out.println();
        }
        
        // Print summary
        System.out.println("====================================================");
        System.out.println("                    Summary");
        System.out.println("====================================================");
        System.out.println("Total root pages analyzed: " + results.size());
        if (ENABLE_DEPTH_WEIGHTING) {
            int totalChildren = 0;
            for (WebPageResult r : results) {
                totalChildren += r.getChildPages().size();
            }
            System.out.println("Total child pages analyzed: " + totalChildren);
            System.out.println("Depth weighting formula:");
            System.out.println("  site_score = root_score × 1.0 + Σ(child_score × 0.5)");
        }
    }
    
    /**
     * Calculate the keyword-weighted score for a page.
     * Formula: ISO×4 + Standard×3 + Sustain×2 + Certificate×1
     */
    private static void calculateScore(WebPageResult page) {
        Map<String, Integer> countMap = page.getWordCountMap();
        if (countMap == null) {
            page.setScore(0);
            return;
        }

        int isoCount         = countMap.getOrDefault("ISO", 0);
        int standardCount    = countMap.getOrDefault("Standard", 0);
        int sustainCount     = countMap.getOrDefault("Sustain", 0);
        int certificateCount = countMap.getOrDefault("Certificate", 0);

        // Weighted formula for keyword importance
        int score =
                (isoCount * 4) +
                (standardCount * 3) +
                (sustainCount * 2) +
                (certificateCount * 1);

        page.setScore(score);  // This also calculates weightedScore based on depth
    }
}
