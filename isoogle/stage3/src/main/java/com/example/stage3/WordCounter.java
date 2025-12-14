package com.example.stage3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WordCounter
 * Counts occurrences of keywords inside a plain cleaned string.
 */
public class WordCounter {

    /**
     * Count occurrences of the given keywords inside the given text.
     */
    public static Map<String, Integer> countWords(String text, List<String> keywords) {
        Map<String, Integer> result = new HashMap<>();

        if (text == null || text.isEmpty() || keywords == null) {
            return result;
        }

        String lowerText = text.toLowerCase();

        for (String key : keywords) {
            if (key == null || key.trim().isEmpty()) continue;

            String lowerKey = key.toLowerCase();
            int count = 0;
            int index = 0;

            while ((index = lowerText.indexOf(lowerKey, index)) != -1) {
                count++;
                index += lowerKey.length();
            }

            result.put(key, count);
        }

        return result;
    }
}
