package com.example.stage2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WordCounter
 * ----------------------------------------------------
 * Count occurrences of keywords inside a plain cleaned string.
 * This class supports:
 *   - Case-insensitive matching
 *   - Returning result as a Map<String, Integer>
 */
public class WordCounter {

    /**
     * Count occurrences of the given keywords inside the given text.
     *
     * @param text      Cleaned text content
     * @param keywords  List of keywords to search for
     * @return Map: keyword -> frequency
     */
    public static Map<String, Integer> countWords(String text, List<String> keywords) {

        Map<String, Integer> result = new HashMap<>();

        if (text == null || text.isEmpty() || keywords == null) {
            return result;
        }

        // case-insensitive → everything to lowercase
        String lowerText = text.toLowerCase();

        for (String key : keywords) {
            if (key == null || key.trim().isEmpty()) continue;

            String lowerKey = key.toLowerCase();

            int count = 0;
            int index = 0;

            // count all occurrences of 'lowerKey' in 'lowerText'
            while ((index = lowerText.indexOf(lowerKey, index)) != -1) {
                count++;
                index += lowerKey.length();
            }

            result.put(key, count);
        }

        return result;
    }
}
