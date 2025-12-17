package com.example.stage4;

import com.example.stage3.*;
import java.util.*;

/**
 * IterativeKeywordSearch
 * Stage 4: Iteratively derives keywords from discovered websites and expands search
 */
public class IterativeKeywordSearch {
    private static final int ORIGINAL_WEIGHT = 10;
    private static final int DERIVED_WEIGHT = 5;
    private static final int INITIAL_RESULTS = 5;
    private static final int FINAL_RESULTS = 5;
    private static final int KEYWORDS_PER_PAGE = 3;
    private static final int MAX_ITERATIONS = 1;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter keywords for Google search (use quotes for phrases): ");
        String input = scanner.nextLine().trim();
        
        IterativeSearchResult result = performIterativeSearch(input);
        
        System.out.println("\n====================================================");
        System.out.println("       STAGE 4: ITERATIVE KEYWORD SEARCH");
        System.out.println("====================================================");
        
        System.out.println("\n=== ITERATION 1: Search with Original Keywords ===");
        System.out.println("Keywords: " + String.join(", ", result.originalKeywords));
        
        System.out.println("Found " + result.iteration1Results.size() + " results");
        
        System.out.println("\n=== Extracting Keywords from Top Results ===");
        System.out.println("Derived " + result.derivedKeywords.size() + " new keywords:");
        System.out.println(String.join(", ", result.derivedKeywords));
        
        if (!result.derivedKeywords.isEmpty()) {
            System.out.println("\n=== ITERATION 2: Search with Original + Derived Keywords ===");
            System.out.println("Expanded query: " + String.join(", ", result.combinedQuery));
            System.out.println("Found additional results, total unique: " + result.allResults.size());
            System.out.println("Added " + (result.allResults.size() - result.iteration1Results.size()) + " new unique results");
        }
        
        System.out.println("\n=== FINAL RANKING: All Discovered Results ===");
        System.out.println("\n====================================================");
        System.out.println("   TOP " + Math.min(FINAL_RESULTS, result.finalResults.size()) + " RESULTS (from " + result.finalResults.size() + " total discovered)");
        System.out.println("====================================================");
        
        int rank = 1;
        for (SearchResult sr : result.finalResults.subList(0, Math.min(FINAL_RESULTS, result.finalResults.size()))) {
            System.out.println("\nRank #" + (rank++) + " [Score: " + sr.getRankScore() + "]");
            System.out.println("Site: " + sr.getSiteName());
            System.out.println("URL: " + sr.getUrl());
            System.out.println("Preview: " + (sr.getContent() != null ? 
                             sr.getContent().substring(0, Math.min(200, sr.getContent().length())) + "..." : "No content"));
        }
        
        System.out.println("\n====================================================");
        System.out.println("Total iterations: " + (result.derivedKeywords.isEmpty() ? 1 : 2));
        System.out.println("Total unique results discovered: " + result.finalResults.size());
        System.out.println("Original keywords: " + result.originalKeywords.size());
        System.out.println("Derived keywords: " + result.derivedKeywords.size());
        System.out.println("====================================================");
        
        scanner.close();
    }

    /**
     * Performs the iterative search logic, shared between main() and searchApi().
     */
    private static IterativeSearchResult performIterativeSearch(String input) throws Exception {
        List<String> userKeywords = parseKeywords(input);
        
        IterativeSearchResult result = new IterativeSearchResult();
        result.originalKeywords = userKeywords;
        result.logs = new ArrayList<>();
        
        // Track all unique results across iterations
        Map<String, SearchResult> allResults = new LinkedHashMap<>();
        Set<String> allKeywords = new HashSet<>(userKeywords);
        
        // Iteration 1: Search with original keywords
        Map<String, Integer> iteration1Weights = new HashMap<>();
        for (String kw : userKeywords) {
            iteration1Weights.put(kw.toLowerCase(), ORIGINAL_WEIGHT);
        }
        
        String fullQuery1 = String.join(" ", userKeywords);
        List<SearchResult> results1 = SearchService.searchAndRank(fullQuery1, INITIAL_RESULTS, iteration1Weights);
        
        result.iteration1Results = new ArrayList<>(results1);
        result.logs.add("Iteration 1: Searched with " + userKeywords.size() + " original keywords, found " + results1.size() + " results");
        
        // Store iteration 1 results
        for (SearchResult sr : results1) {
            allResults.put(sr.getUrl(), sr);
        }
        
        // Extract keywords from top results
        List<String> derivedKeywords = new ArrayList<>();
        
        for (int i = 0; i < Math.min(INITIAL_RESULTS, results1.size()); i++) {
            SearchResult sr = results1.get(i);
            String content = sr.getContent() != null ? sr.getContent() : "";
            
            List<String> extracted = KeywordExtractor.extractKeywordsWithContext(content, KEYWORDS_PER_PAGE, userKeywords);
            
            // Filter out duplicate keywords 
            for (String keyword : extracted) {
                if (!allKeywords.contains(keyword.toLowerCase())) {
                    derivedKeywords.add(keyword);
                    allKeywords.add(keyword.toLowerCase());
                }
            }
        }
        
        result.derivedKeywords = derivedKeywords;
        result.logs.add("Derived " + derivedKeywords.size() + " new keywords from top results");
        
        // Iteration 2: Search with original + derived keywords
        if (!derivedKeywords.isEmpty()) {
            Map<String, Integer> iteration2Weights = new HashMap<>();
            
            // Add original keywords with higher weight
            for (String kw : userKeywords) {
                iteration2Weights.put(kw.toLowerCase(), ORIGINAL_WEIGHT);
            }
            
            // Add derived keywords with lower weight
            for (String kw : derivedKeywords) {
                iteration2Weights.put(kw.toLowerCase(), DERIVED_WEIGHT);
            }
            
            // Use top derived keywords for the query
            List<String> topDerived = derivedKeywords.subList(0, Math.min(3, derivedKeywords.size()));
            List<String> combinedQuery = new ArrayList<>(userKeywords);
            combinedQuery.addAll(topDerived);
            
            result.combinedQuery = combinedQuery;
            
            String fullQuery2 = String.join(" ", combinedQuery);
            List<SearchResult> results2 = SearchService.searchAndRank(fullQuery2, FINAL_RESULTS, iteration2Weights);
            result.logs.add("Iteration 2: Expanded search with " + combinedQuery.size() + " keywords, found " + results2.size() + " results");
            
            // Merge new results
            for (SearchResult sr : results2) {
                if (!allResults.containsKey(sr.getUrl())) {
                    allResults.put(sr.getUrl(), sr);
                }
            }
        }
        
        // Re-rank all collected results
        result.finalResults = new ArrayList<>(allResults.values());
        
        // Sort by rank score
        result.finalResults.sort((a, b) -> b.getRankScore() - a.getRankScore());
        
        result.allResults = new ArrayList<>(allResults.values());
        
        return result;
    }

    /**
     * REST-friendly API wrapper used by the web controller.
     */
    public static java.util.Map<String, Object> searchApi(String input) throws Exception {
        IterativeSearchResult result = performIterativeSearch(input == null ? "" : input.trim());
        
        Map<String, Object> resp = new HashMap<>();
        resp.put("originalKeywords", result.originalKeywords);
        resp.put("derivedKeywords", result.derivedKeywords);
        
        // Prepare response result list (top FINAL_RESULTS)
        List<Map<String, Object>> items = new ArrayList<>();
        for (SearchResult sr : result.finalResults.subList(0, Math.min(FINAL_RESULTS, result.finalResults.size()))) {
            Map<String, Object> it = new HashMap<>();
            it.put("siteName", sr.getSiteName());
            it.put("url", sr.getUrl());
            it.put("score", sr.getRankScore());
            it.put("preview", sr.getContent() == null ? "" : (sr.getContent().length() > 200 ? sr.getContent().substring(0,200)+"..." : sr.getContent()));
            items.add(it);
        }

        resp.put("results", items);
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
     * Helper class to hold iterative search results
     */
    private static class IterativeSearchResult {
        List<String> originalKeywords;
        List<String> derivedKeywords;
        List<String> combinedQuery;
        List<SearchResult> iteration1Results;
        List<SearchResult> allResults;
        List<SearchResult> finalResults;
        List<String> logs;
    }
}
