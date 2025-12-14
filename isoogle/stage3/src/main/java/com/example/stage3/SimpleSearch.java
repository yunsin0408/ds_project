package com.example.stage3;

import java.util.*;

/**
 * SimpleSearch
 * Stage 3: Basic Google search with keyword frequency ranking
 */
public class SimpleSearch {
    private static final int ORIGINAL_WEIGHT = 4;
    private static final int NUM_RESULTS = 10;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter keywords for Google search (use quotes for phrases): ");
        String input = scanner.nextLine().trim();
        
        // Parse quoted phrases
        List<String> userKeywords = parseKeywords(input);

        Map<String, Integer> keywordWeights = new HashMap<>();

        // Add query keywords 
        for (String kw : userKeywords) {
            keywordWeights.put(kw.toLowerCase(), ORIGINAL_WEIGHT);
        }

        System.out.println("\n=== Stage 3: Basic Keyword Search ===");
        System.out.println("Keywords: " + String.join(", ", userKeywords));
        System.out.println();

        // Call WebAnalyzer 
        String fullQuery = String.join(" ", userKeywords);
        List<String> keywords = new ArrayList<>(keywordWeights.keySet());
        List<SearchResult> results = WebAnalyzer.analyzeGoogleRankedSites(fullQuery, keywords, keywordWeights, NUM_RESULTS);

        System.out.println("\n====================================================");
        System.out.println("            Google Ranked Sites Result");
        System.out.println("====================================================");
        int rank = 1;
        for (SearchResult sr : results) {
            System.out.println("Rank #" + (rank++) + " [RankScore: " + sr.getRankScore() + "]");
            System.out.println("Site Name : " + sr.getSiteName());
            System.out.println("URL       : " + sr.getUrl());
            System.out.println("Content   : " + (sr.getContent() != null ? sr.getContent().substring(0, Math.min(200, sr.getContent().length())) + "..." : "No Content"));
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
}
