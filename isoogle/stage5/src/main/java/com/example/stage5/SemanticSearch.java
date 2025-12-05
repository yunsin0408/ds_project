package com.example.stage5;

import com.example.stage3.SearchResult;
import com.example.stage4.SearchService;

import java.util.*;

/**
 * Stage 5: Hybrid ranking using original keywords with cosine similarity + keyword frequency
 */
public class SemanticSearch {
    private static final int ORIGINAL_WEIGHT = 4;
    private static final int INITIAL_RESULTS = 10;
    private static final int FINAL_TOP_RESULTS = 5;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter keywords for Google search (use quotes for phrases): ");
        String input = scanner.nextLine().trim();
        
        // Parse quoted phrases
        List<String> userKeywords = parseKeywords(input);

        Map<String, Integer> keywordWeights = new HashMap<>();

        // Add original keywords
        for (String kw : userKeywords) {
            keywordWeights.put(kw.toLowerCase(), ORIGINAL_WEIGHT);
        }

        System.out.println("\n=== Using Original Keywords (Semantic Search Handles Synonyms) ===");
        System.out.println("Keywords: " + String.join(", ", userKeywords));
        System.out.println();

        // Call search service
        String fullQuery = String.join(" ", userKeywords);
        List<SearchResult> results = SearchService.searchAndRank(fullQuery, INITIAL_RESULTS, keywordWeights);

        System.out.println("\n=== Ranking Top " + FINAL_TOP_RESULTS + " Results by Cosine Similarity ===");
        
        // Calculate cosine similarity on page content
        List<RankedResult> rankedResults = new ArrayList<>();
        
        for (int i = 0; i < Math.min(FINAL_TOP_RESULTS, results.size()); i++) {
            SearchResult sr = results.get(i);
            System.out.println("Processing " + (i + 1) + "/" + FINAL_TOP_RESULTS + ": " + sr.getSiteName());
            
            // page content cosine similarity 
            String content = sr.getContent() != null ? sr.getContent() : "";
            
            // Calculate cosine similarity between query and full content
            double similarity = CosineSimilarityRanker.calculateSimilarity(fullQuery, content);
            
            rankedResults.add(new RankedResult(sr, content, similarity));
        }
        
        // Min-Max Normalization with (6:4) combination
        double minSimilarity = rankedResults.stream().mapToDouble(r -> r.similarity).min().orElse(0.0);
        double maxSimilarity = rankedResults.stream().mapToDouble(r -> r.similarity).max().orElse(1.0);
        int minKeywordScore = rankedResults.stream().mapToInt(r -> r.result.getRankScore()).min().orElse(0);
        int maxKeywordScore = rankedResults.stream().mapToInt(r -> r.result.getRankScore()).max().orElse(1);
        
        final double KEYWORD_WEIGHT = 0.6;
        final double SIMILARITY_WEIGHT = 0.4;
        
        for (RankedResult rr : rankedResults) {
            // Normalize both scores to [0, 1]
            double normalizedSimilarity = (maxSimilarity - minSimilarity) > 0 
                ? (rr.similarity - minSimilarity) / (maxSimilarity - minSimilarity)
                : rr.similarity;
            
            double normalizedKeyword = (maxKeywordScore - minKeywordScore) > 0
                ? (double)(rr.result.getRankScore() - minKeywordScore) / (maxKeywordScore - minKeywordScore)
                : (rr.result.getRankScore() > 0 ? 1.0 : 0.0);
            
            // Weighted combination (0-100)
            rr.combinedScore = 100 * ((KEYWORD_WEIGHT * normalizedKeyword) + (SIMILARITY_WEIGHT * normalizedSimilarity));
            rr.normalizedSim = normalizedSimilarity;
            rr.normalizedKw = normalizedKeyword;
        }
        
        // Sort by combined score (descending)
        rankedResults.sort((a, b) -> Double.compare(b.combinedScore, a.combinedScore));

        // Display final ranked results
        System.out.println("\n====================================================");
        System.out.println("    Top " + FINAL_TOP_RESULTS + " Results (Ranked by Combined Score)");
        System.out.println("    Weighting: " + (int)(KEYWORD_WEIGHT*100) + "% Keyword + " + (int)(SIMILARITY_WEIGHT*100) + "% Similarity");
        System.out.println("====================================================");
        
        int rank = 1;
        for (RankedResult rr : rankedResults) {
            System.out.println("Rank #" + (rank++) + " [Score: " + String.format("%.2f", rr.combinedScore) + "/100]");
            System.out.println("  → Keyword: " + rr.result.getRankScore() + " (norm: " + String.format("%.3f", rr.normalizedKw) + ")");
            System.out.println("  → Similarity: " + String.format("%.4f", rr.similarity) + " (norm: " + String.format("%.3f", rr.normalizedSim) + ")");
            System.out.println("Site Name : " + rr.result.getSiteName());
            System.out.println("URL       : " + rr.result.getUrl());
            System.out.println("Preview   : " + (rr.summary.length() > 200 ? rr.summary.substring(0, 200) + "..." : rr.summary));
            System.out.println("----------------------------------------------------");
        }
        
        scanner.close();
    }
    
    /**
     * Parse input string to extract keywords and quoted phrases
     */
    private static List<String> parseKeywords(String input) {
        List<String> keywords = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if ((c == ' ' || c == ',') && !inQuotes) {
                if (current.length() > 0) {
                    keywords.add(current.toString().trim());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            keywords.add(current.toString().trim());
        }
        
        return keywords;
    }
    
    /**
     * Helper class to hold result with summary and similarity score
     */
    private static class RankedResult {
        SearchResult result;
        String summary;
        double similarity;
        double combinedScore;
        double normalizedSim;  
        double normalizedKw;   
        
        RankedResult(SearchResult result, String summary, double similarity) {
            this.result = result;
            this.summary = summary;
            this.similarity = similarity;
            this.combinedScore = 0.0;
            this.normalizedSim = 0.0;
            this.normalizedKw = 0.0;
        }
    }
}
