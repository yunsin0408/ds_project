package com.example.stage3;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        if (keywords == null) return result;
        if (text == null || text.isEmpty()) {
            for (String k : keywords) if (k != null) result.put(k, 0);
            return result;
        }

        // Normalize to NFKC and lowercase for stable matching
        String normText = Normalizer.normalize(text, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);

        // Precompute a unicode-aware token stream for Latin to allow word-boundary matching
        for (String rawKey : keywords) {
            if (rawKey == null || rawKey.trim().isEmpty()) continue;
            String key = Normalizer.normalize(rawKey, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT).trim();

            int count = 0;

            // Detect CJK presence in keyword: if contains Han/Hiragana/Katakana use substring matching
            boolean isCJK = key.codePoints().anyMatch(cp -> {
                Character.UnicodeScript s = Character.UnicodeScript.of(cp);
                return s == Character.UnicodeScript.HAN || s == Character.UnicodeScript.HIRAGANA || s == Character.UnicodeScript.KATAKANA;
            });

            if (isCJK) {
                // literal substring count
                int idx = 0;
                while ((idx = normText.indexOf(key, idx)) != -1) {
                    count++;
                    idx += Math.max(1, key.length());
                }
            } else {
                // Use word-boundary regex for Latin and other scripts
                String pattern = "\\b" + Pattern.quote(key) + "\\b";
                Pattern p = Pattern.compile(pattern, Pattern.UNICODE_CHARACTER_CLASS);
                Matcher m = p.matcher(normText);
                while (m.find()) count++;
            }

            result.put(rawKey, count);
        }

        return result;
    }
}
