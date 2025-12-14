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
    private static final int FINAL_RESULTS = 10;
    private static final int KEYWORDS_PER_PAGE = 3;
    private static final int MAX_ITERATIONS = 1;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter keywords for Google search (use quotes for phrases): ");
        String input = scanner.nextLine().trim();
        
        // Parse quoted phrases
        List<String> userKeywords = parseKeywords(input);
        
        System.out.println("\n====================================================");
        System.out.println("       STAGE 4: ITERATIVE KEYWORD SEARCH");
        System.out.println("====================================================");

        // Track all unique results across iterations
        Map<String, SearchResult> allResults = new LinkedHashMap<>();
        Set<String> allKeywords = new HashSet<>(userKeywords);
        
        // Iteration 1: Search with original keywords
        System.out.println("\n=== ITERATION 1: Search with Original Keywords ===");
        System.out.println("Keywords: " + String.join(", ", userKeywords));
        
        Map<String, Integer> iteration1Weights = new HashMap<>();
        for (String kw : userKeywords) {
            iteration1Weights.put(kw.toLowerCase(), ORIGINAL_WEIGHT);
        }
        
        String fullQuery1 = String.join(" ", userKeywords);
        List<SearchResult> results1 = SearchService.searchAndRank(fullQuery1, INITIAL_RESULTS, iteration1Weights);
        
        System.out.println("Found " + results1.size() + " results");
        
        // Store iteration 1 results
        for (SearchResult sr : results1) {
            allResults.put(sr.getUrl(), sr);
        }
        
        // Extract keywords from top results
        System.out.println("\n=== Extracting Keywords from Top Results ===");
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
            
            System.out.println("  From result #" + (i + 1) + ": " + 
                             (extracted.isEmpty() ? "none" : String.join(", ", extracted)));
        }
        
        System.out.println("\nDerived " + derivedKeywords.size() + " new keywords:");
        System.out.println(String.join(", ", derivedKeywords));
        
        // Iteration 2: Search with original + derived keywords
        if (!derivedKeywords.isEmpty()) {
            System.out.println("\n=== ITERATION 2: Search with Original + Derived Keywords ===");
            
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
            
            System.out.println("Expanded query: " + String.join(", ", combinedQuery));
            
            String fullQuery2 = String.join(" ", combinedQuery);
            List<SearchResult> results2 = SearchService.searchAndRank(fullQuery2, FINAL_RESULTS, iteration2Weights);
            
            System.out.println("Found " + results2.size() + " results");
            
            // Merge new results
            int newCount = 0;
            for (SearchResult sr : results2) {
                if (!allResults.containsKey(sr.getUrl())) {
                    allResults.put(sr.getUrl(), sr);
                    newCount++;
                }
            }
            
            System.out.println("Added " + newCount + " new unique results");
        }
        
        // Re-rank all collected results
        System.out.println("\n=== FINAL RANKING: All Discovered Results ===");
        List<SearchResult> finalResults = new ArrayList<>(allResults.values());
        
        // Sort by rank score
        finalResults.sort((a, b) -> b.getRankScore() - a.getRankScore());
        
        System.out.println("\n====================================================");
        System.out.println("   TOP " + Math.min(FINAL_RESULTS, finalResults.size()) + " RESULTS (from " + finalResults.size() + " total discovered)");
        System.out.println("====================================================");
        
        int rank = 1;
        for (SearchResult sr : finalResults.subList(0, Math.min(FINAL_RESULTS, finalResults.size()))) {
            System.out.println("\nRank #" + (rank++) + " [Score: " + sr.getRankScore() + "]");
            System.out.println("Site: " + sr.getSiteName());
            System.out.println("URL: " + sr.getUrl());
            System.out.println("Preview: " + (sr.getContent() != null ? 
                             sr.getContent().substring(0, Math.min(200, sr.getContent().length())) + "..." : "No content"));
        }
        
        System.out.println("\n====================================================");
        System.out.println("Total iterations: 2");
        System.out.println("Total unique results discovered: " + finalResults.size());
        System.out.println("Original keywords: " + userKeywords.size());
        System.out.println("Derived keywords: " + derivedKeywords.size());
        System.out.println("====================================================");
        
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
}
