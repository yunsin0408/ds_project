package com.example.stage4;

import com.example.stage3.*;
import java.util.*;
import com.example.stage4.KeywordExtractor;

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
     * Variant that performs one round of keyword derivation from top initial results,
     * expands the query, re-runs the search, and attaches the derived keywords to each returned SearchResult.
     */
    public static List<SearchResult> searchAndRankWithDerived(String userKeywords, int numResults, Map<String, Integer> keywordWeights) throws Exception {
        final int INITIAL_FETCH = Math.min(5, numResults);
        Map<String, Integer> enhancedWeights = new HashMap<>(keywordWeights);

        String[] isoOrgTerms = {"international", "organization", "standardization"};
        for (String term : isoOrgTerms) {
            if (!enhancedWeights.containsKey(term)) {
                enhancedWeights.put(term, 1);
            }
        }

        // Ensure user's query tokens are present as higher-weight keywords
        if (userKeywords != null && !userKeywords.isBlank()) {
            String[] parts = userKeywords.split("\\s+");
            for (String p : parts) {
                if (p == null) continue;
                String tok = p.trim().toLowerCase().replaceAll("[^\\p{L}0-9\\-]", "");
                if (tok.length() < 1) continue;
                enhancedWeights.putIfAbsent(tok, 10); // give user tokens a strong weight
            }
        }

        List<String> keywords = new ArrayList<>(enhancedWeights.keySet());

        // initial fetch
        List<SearchResult> initialResults = WebAnalyzer.analyzeGoogleRankedSites(userKeywords, keywords, enhancedWeights, INITIAL_FETCH);

        // derive keywords from top initial results
        Set<String> derivedSet = new LinkedHashSet<>();
        List<String> userKwList = new ArrayList<>();
        if (userKeywords != null && !userKeywords.isBlank()) {
            for (String s : userKeywords.split("\\s+")) if (!s.isBlank()) userKwList.add(s);
        }

        for (SearchResult sr : initialResults) {
            String content = sr.getContent() == null ? "" : sr.getContent();
            List<String> extracted = KeywordExtractor.extractKeywordsWithContext(content, 3, userKwList);
            for (String k : extracted) {
                String lk = k.toLowerCase();
                if (!enhancedWeights.containsKey(lk)) {
                    derivedSet.add(lk);
                }
            }
        }

        List<String> derivedKeywords = new ArrayList<>(derivedSet);

        // if there are derived keywords, expand weights and re-run final search
        if (!derivedKeywords.isEmpty()) {
            for (String d : derivedKeywords) {
                enhancedWeights.putIfAbsent(d, 5);
            }

            // build expanded query using top 3 derived
            List<String> topDerived = derivedKeywords.subList(0, Math.min(3, derivedKeywords.size()));
            String expandedQuery = userKeywords == null ? String.join(" ", topDerived) : userKeywords + " " + String.join(" ", topDerived);

            List<SearchResult> finalResults = WebAnalyzer.analyzeGoogleRankedSites(expandedQuery, new ArrayList<>(enhancedWeights.keySet()), enhancedWeights, numResults);
            finalResults.sort((a, b) -> b.getRankScore() - a.getRankScore());

            // attach derived keywords to each result for frontend
            // for (SearchResult r : finalResults) r.setDerivedKeywords(derivedKeywords);
            return finalResults;
        } else {
            // attach empty derived list and return initial results 
            // for (SearchResult r : initialResults) r.setDerivedKeywords(new ArrayList<String>());
            initialResults.sort((a, b) -> b.getRankScore() - a.getRankScore());
            return initialResults;
        }
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
