package com.example.stage4;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * KeywordExtractor
 * Extracts keywords from text using term frequency analysis
 * Supports multilingual text processing
 */
public class KeywordExtractor {
    
    private static final CharArraySet STOPWORDS = EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;
    
    /**
     * Detect if text contains primarily non-Latin characters
     */
    private static boolean isNonLatin(String text) {
        if (text == null || text.isEmpty()) return false;
        long nonLatinCount = text.chars()
            .filter(c -> c > 0x024F) // Beyond Latin Extended-B
            .count();
        return nonLatinCount > text.length() * 0.3; // >30% non-Latin
    }
    
    /**
     * Extract top N keywords from text based on term frequency (Filter out stopwords and very short words)
     * Supports both Latin and non-Latin scripts
     * @param text The text to extract keywords from
     * @param topN Number of top keywords to extract
     * @return List of extracted keywords sorted by frequency
     */
    public static List<String> extractKeywords(String text, int topN) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        
        boolean isNonLatin = isNonLatin(text);
        
        // Tokenize based on script
        String[] words;
        if (isNonLatin) {
            // For CJK languages, keep all Unicode characters
            words = text.toLowerCase()
                .replaceAll("[\\p{Punct}\\s]+", " ")
                .split("\\s+");
        } else {
            // For Latin scripts, keep only a-z and accented characters
            words = text.toLowerCase()
                .replaceAll("[^\\p{L}0-9\\s]", " ")
                .split("\\s+");
        }
        
        // Count word frequencies 
        Map<String, Integer> wordFreq = new HashMap<>();
        for (String word : words) {
            
            int minLength = isNonLatin ? 2 : 3;
            
            if (word.length() > minLength && 
                !STOPWORDS.contains(word) && 
                !word.matches(".*\\d.*")) {
                wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
            }
        }
        
        // Sort by frequency and return top N
        return wordFreq.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(topN)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Extract keywords that co-occur with context keywords (original query terms)
     * This ensures extracted keywords are contextually related to the original search intent
     * 
     * @param text The text to extract keywords from
     * @param topN Number of top keywords to extract
     * @param contextKeywords Original query keywords to filter for co-occurrence
     * @return List of extracted keywords that appear near context keywords
     */
    public static List<String> extractKeywordsWithContext(String text, int topN, List<String> contextKeywords) {
        if (text == null || text.isEmpty() || contextKeywords == null || contextKeywords.isEmpty()) {
            return extractKeywords(text, topN);
        }
        
        // Filter out ISO organization terms from context keywords to avoid bias
        List<String> filteredContext = new ArrayList<>();
        Set<String> isoOrgTerms = new HashSet<>(Arrays.asList("international", "organization", "standardization", "standards"));
        
        for (String kw : contextKeywords) {
            if (!isoOrgTerms.contains(kw.toLowerCase())) {
                filteredContext.add(kw);
            }
        }
        
        // If no meaningful context keywords remain, use basic extraction
        if (filteredContext.isEmpty()) {
            return extractKeywords(text, topN);
        }
        
        // Split into sentences/chunks
        String[] chunks = text.toLowerCase().split("[.!?\\n]+");
        Map<String, Integer> contextualWordFreq = new HashMap<>();
        
        // Only count words that appear in chunks containing context keywords
        // Use ALL logic for single/few keywords, ANY logic for many keywords
        boolean useStrictMatching = filteredContext.size() <= 2;
        
        for (String chunk : chunks) {
            boolean isRelevantChunk = false;
            
            if (useStrictMatching) {
                // Require ALL context keywords (strict for focused queries like "camera iso")
                isRelevantChunk = true;
                for (String contextKw : filteredContext) {
                    if (!chunk.contains(contextKw.toLowerCase())) {
                        isRelevantChunk = false;
                        break;
                    }
                }
            } else {
                // Require ANY context keyword (relaxed for broad queries)
                for (String contextKw : filteredContext) {
                    if (chunk.contains(contextKw.toLowerCase())) {
                        isRelevantChunk = true;
                        break;
                    }
                }
            }
            
            if (isRelevantChunk) {
                boolean isNonLatin = isNonLatin(chunk);
                String[] words;
                
                if (isNonLatin) {
                    words = chunk.replaceAll("[\\p{Punct}\\s]+", " ").split("\\s+");
                } else {
                    words = chunk.replaceAll("[^\\p{L}0-9\\s]", " ").split("\\s+");
                }
                
                int minLength = isNonLatin ? 2 : 3;
                
                for (String word : words) {
                    if (word.length() > minLength && !STOPWORDS.contains(word) && !word.matches(".*\\d.*") && !isoOrgTerms.contains(word)) {
                        // Skip if it's already a context keyword
                        boolean isContextKw = false;
                        for (String contextKw : filteredContext) {
                            if (word.equals(contextKw.toLowerCase())) {
                                isContextKw = true;
                                break;
                            }
                        }
                        if (!isContextKw) {
                            contextualWordFreq.put(word, contextualWordFreq.getOrDefault(word, 0) + 1);
                        }
                    }
                }
            }
        }
        
        // If no contextual keywords found, fall back to basic extraction with ISO terms filtered
        if (contextualWordFreq.isEmpty()) {
            List<String> basicKeywords = extractKeywords(text, topN * 2);
            return basicKeywords.stream()
                .filter(kw -> !isoOrgTerms.contains(kw.toLowerCase()))
                .limit(topN)
                .collect(Collectors.toList());
        }
        
        // Return top N contextual keywords
        return contextualWordFreq.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(topN)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Extract keywords from multiple texts and merge them
     * 
     * @param texts List of text contents
     * @param topN Number of top keywords to extract from each text
     * @return List of unique keywords
     */
    public static List<String> extractFromMultiple(List<String> texts, int topN) {
        Set<String> allKeywords = new HashSet<>();
        
        for (String text : texts) {
            List<String> keywords = extractKeywords(text, topN);
            allKeywords.addAll(keywords);
        }
        
        return new ArrayList<>(allKeywords);
    }
}
