package com.example.stage5;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import java.util.*;

/**
 * CosineSimilarityRanker
 * Ranks search results based on cosine similarity between query and page content
 */
public class CosineSimilarityRanker {
    
    // Use Apache Lucene's comprehensive English stopword list
    private static final CharArraySet STOPWORDS = EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;
    

    public static double calculateSimilarity(String query, String document) {
        if (query == null || document == null || query.isEmpty() || document.isEmpty()) {
            return 0.0;
        }
        
        // Convert to lowercase and split into words
        String[] queryWords = query.toLowerCase().split("\\s+");
        String[] docWords = document.toLowerCase().split("\\s+");
        
        // Create word frequency vectors
        Map<String, Integer> queryVector = createFrequencyVector(queryWords);
        Map<String, Integer> docVector = createFrequencyVector(docWords);
        
        // Get all unique words
        Set<String> allWords = new HashSet<>();
        allWords.addAll(queryVector.keySet());
        allWords.addAll(docVector.keySet());
        
        // Calculate dot product and magnitudes
        double dotProduct = 0.0;
        double queryMagnitude = 0.0;
        double docMagnitude = 0.0;
        
        for (String word : allWords) {
            int queryFreq = queryVector.getOrDefault(word, 0);
            int docFreq = docVector.getOrDefault(word, 0);
            
            dotProduct += queryFreq * docFreq;
            queryMagnitude += queryFreq * queryFreq;
            docMagnitude += docFreq * docFreq;
        }
        
        queryMagnitude = Math.sqrt(queryMagnitude);
        docMagnitude = Math.sqrt(docMagnitude);
        
        if (queryMagnitude == 0 || docMagnitude == 0) {
            return 0.0;
        }
        
        return dotProduct / (queryMagnitude * docMagnitude);
    }
    
    /**
     * Create frequency vector from words
     */
    private static Map<String, Integer> createFrequencyVector(String[] words) {
        Map<String, Integer> vector = new HashMap<>();
        for (String word : words) {
            // Remove punctuation and filter stopwords
            String cleaned = word.replaceAll("[^a-z0-9]", "");
            if (!cleaned.isEmpty() && !STOPWORDS.contains(cleaned)) {
                vector.put(cleaned, vector.getOrDefault(cleaned, 0) + 1);
            }
        }
        return vector;
    }
}
