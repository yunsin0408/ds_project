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
        
        SemanticSearchResult result = performSemanticSearch(input);
        
        System.out.println("\n=== Using Original Keywords (Semantic Search Handles Synonyms) ===");
        System.out.println("Keywords: " + String.join(", ", result.userKeywords));
        System.out.println();

        System.out.println("\n=== Ranking Top " + FINAL_TOP_RESULTS + " Results by Cosine Similarity ===");
        
        System.out.println("\n====================================================");
        System.out.println("    Top " + result.rankedResults.size() + " Results (Ranked by Combined Score)");
        System.out.println("    Weighting: " + (int)(result.keywordWeight*100) + "% Keyword + " + (int)(result.similarityWeight*100) + "% Similarity");
        System.out.println("====================================================");
        
        int rank = 1;
        for (RankedResult rr : result.rankedResults) {
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
     * Performs the semantic search logic, shared between main() and searchApi().
     */
    private static SemanticSearchResult performSemanticSearch(String input) throws Exception {
        List<String> userKeywords = parseKeywords(input);
        
        SemanticSearchResult result = new SemanticSearchResult();
        result.userKeywords = userKeywords;
        result.logs = new ArrayList<>();
        result.logs = new ArrayList<>();
        
        Map<String, Integer> keywordWeights = new HashMap<>();
        for (String kw : userKeywords) {
            keywordWeights.put(kw.toLowerCase(), ORIGINAL_WEIGHT);
        }

        String fullQuery = String.join(" ", userKeywords);
        List<SearchResult> results = SearchService.searchAndRank(fullQuery, INITIAL_RESULTS, keywordWeights);
        result.logs.add("Fetched " + results.size() + " initial results using keyword ranking");

        // Calculate cosine similarity on page content
        List<RankedResult> rankedResults = new ArrayList<>();
        
        for (int i = 0; i < Math.min(FINAL_TOP_RESULTS, results.size()); i++) {
            SearchResult sr = results.get(i);
            String content = sr.getContent() != null ? sr.getContent() : "";
            
            // Calculate cosine similarity between query and full content
            double similarity = CosineSimilarityRanker.calculateSimilarity(fullQuery, content);
            if (Double.isNaN(similarity)) similarity = 0.0;
            
            rankedResults.add(new RankedResult(sr, content, similarity));
        }
        result.logs.add("Calculated cosine similarity for " + rankedResults.size() + " top results");
        
        // Min-Max Normalization with (6:4) combination
        double minSimilarity = rankedResults.stream().mapToDouble(r -> r.similarity).min().orElse(0.0);
        double maxSimilarity = rankedResults.stream().mapToDouble(r -> r.similarity).max().orElse(1.0);
        int minKeywordScore = rankedResults.stream().mapToInt(r -> r.result.getRankScore()).min().orElse(0);
        int maxKeywordScore = rankedResults.stream().mapToInt(r -> r.result.getRankScore()).max().orElse(1);
        
        result.keywordWeight = 0.6;
        result.similarityWeight = 0.4;
        result.logs.add("Combined scores with " + (int)(result.keywordWeight*100) + "% keyword frequency + " + (int)(result.similarityWeight*100) + "% cosine similarity");
        
        for (RankedResult rr : rankedResults) {
            // Normalize both scores to [0, 1]
            double normalizedSimilarity = (maxSimilarity - minSimilarity) > 0 
                ? (rr.similarity - minSimilarity) / (maxSimilarity - minSimilarity)
                : 0.5;  // default to 0.5 if all similarities are equal
            
            double normalizedKeyword = (maxKeywordScore - minKeywordScore) > 0
                ? (double)(rr.result.getRankScore() - minKeywordScore) / (maxKeywordScore - minKeywordScore)
                : 0.5;  // default to 0.5 if all keyword scores are equal
            
            // Weighted combination (0-100)
            rr.combinedScore = 100 * ((result.keywordWeight * normalizedKeyword) + (result.similarityWeight * normalizedSimilarity));
            rr.normalizedSim = normalizedSimilarity;
            rr.normalizedKw = normalizedKeyword;
        }
        
        // Sort by combined score (descending)
        rankedResults.sort((a, b) -> Double.compare(b.combinedScore, a.combinedScore));
        
        result.rankedResults = rankedResults;
        
        return result;
    }

    /**
     * REST-friendly API wrapper used by the web controller.
     * Returns a Map for JSON serialization.
     */
    public static Map<String, Object> searchApi(String query, String mode) throws Exception {
        SemanticSearchResult result = performSemanticSearch(query == null ? "" : query.trim());
        
        List<Map<String, Object>> respResults = new ArrayList<>();
        
        for (RankedResult rr : result.rankedResults) {
            Map<String, Object> item = new HashMap<>();
            item.put("siteName", rr.result.getSiteName());
            item.put("url", rr.result.getUrl());
            item.put("score", rr.combinedScore);
            item.put("keywordScore", rr.result.getRankScore());
            item.put("similarity", rr.similarity);
            item.put("preview", rr.summary == null ? "" : (rr.summary.length() > 300 ? rr.summary.substring(0, 300) + "..." : rr.summary));
            respResults.add(item);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("query", query);
        resp.put("mode", mode == null ? "semantic" : mode);
        resp.put("results", respResults);
        resp.put("logs", result.logs);
        return resp;
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
    
    /**
     * Helper class to hold semantic search results
     */
    private static class SemanticSearchResult {
        List<String> userKeywords;
        List<RankedResult> rankedResults;
        double keywordWeight;
        double similarityWeight;
        List<String> logs;
    }
}
