package com.example.stage4;

import com.example.stage3.*;
import java.util.*;

/**
 * SearchService
 * Backend logic for searching, analyzing, and reranking search results
 */
public class SearchService {

    /**
     * Main backend logic: takes user keywords, fetches from Google API,
     * analyzes, and returns ranked results.
     * Automatically adds low-weight ISO organization terms to help with ranking
     */
    public static List<SearchResult> searchAndRank(String userKeywords, int numResults, Map<String, Integer> keywordWeights) throws Exception {
        // Add ISO organization keywords with low weight (1) if not already present
        Map<String, Integer> enhancedWeights = new HashMap<>(keywordWeights);
        
        String[] isoOrgTerms = {"international", "organization", "standardization"};
        for (String term : isoOrgTerms) {
            if (!enhancedWeights.containsKey(term)) {
                enhancedWeights.put(term, 1);
            }
        }
        
        List<String> keywords = new ArrayList<>(enhancedWeights.keySet());
        
        List<SearchResult> searchResults = WebAnalyzer.analyzeGoogleRankedSites(
            userKeywords, 
            keywords, 
            enhancedWeights, 
            numResults
        );
        // Sort by rank score descending
        searchResults.sort((a, b) -> b.getRankScore() - a.getRankScore());
        
        return searchResults;
    }

    /**
     * Overloaded version with default keyword weights
     */
    public static List<SearchResult> searchAndRank(String userKeywords, int numResults) throws Exception {
        Map<String, Integer> defaultWeights = new HashMap<>();
        defaultWeights.put("ISO", 4);
        defaultWeights.put("Standard", 3);
        defaultWeights.put("Sustain", 2);
        defaultWeights.put("Certificate", 1);
        
        return searchAndRank(userKeywords, numResults, defaultWeights);
    }
}
