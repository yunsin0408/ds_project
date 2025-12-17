package com.example.stage4;

import com.example.stage3.SearchResult;
import java.util.*;

public class IterativeKeywordSearch {

    private static final int ORIGINAL_WEIGHT = 10;
    private static final int DERIVED_WEIGHT = 5;
    private static final int INITIAL_RESULTS = 5;
    private static final int FINAL_RESULTS = 10;
    private static final int KEYWORDS_PER_PAGE = 3;

    // ✅ 給 API/Controller 呼叫的入口
    public static List<SearchResult> run(String input) throws Exception {
        input = input == null ? "" : input.trim();
        if (input.isEmpty()) return List.of();

        List<String> userKeywords = parseKeywords(input);

        Map<String, SearchResult> allResults = new LinkedHashMap<>();
        Set<String> allKeywords = new HashSet<>();
        for (String kw : userKeywords) allKeywords.add(kw.toLowerCase());

        // Iteration 1
        Map<String, Integer> iteration1Weights = new HashMap<>();
        for (String kw : userKeywords) iteration1Weights.put(kw.toLowerCase(), ORIGINAL_WEIGHT);

        String fullQuery1 = String.join(" ", userKeywords);
        List<SearchResult> results1 = SearchService.searchAndRank(fullQuery1, INITIAL_RESULTS, iteration1Weights);

        for (SearchResult sr : results1) allResults.put(sr.getUrl(), sr);

        // Extract derived keywords
        List<String> derivedKeywords = new ArrayList<>();
        for (int i = 0; i < Math.min(INITIAL_RESULTS, results1.size()); i++) {
            SearchResult sr = results1.get(i);
            String content = sr.getContent() != null ? sr.getContent() : "";

            List<String> extracted = KeywordExtractor.extractKeywordsWithContext(
                    content, KEYWORDS_PER_PAGE, userKeywords
            );

            for (String keyword : extracted) {
                String low = keyword.toLowerCase();
                if (!allKeywords.contains(low)) {
                    derivedKeywords.add(keyword);
                    allKeywords.add(low);
                }
            }
        }

        // Iteration 2
        if (!derivedKeywords.isEmpty()) {
            Map<String, Integer> iteration2Weights = new HashMap<>();
            for (String kw : userKeywords) iteration2Weights.put(kw.toLowerCase(), ORIGINAL_WEIGHT);
            for (String kw : derivedKeywords) iteration2Weights.put(kw.toLowerCase(), DERIVED_WEIGHT);

            List<String> topDerived = derivedKeywords.subList(0, Math.min(3, derivedKeywords.size()));
            List<String> combinedQuery = new ArrayList<>(userKeywords);
            combinedQuery.addAll(topDerived);

            String fullQuery2 = String.join(" ", combinedQuery);
            List<SearchResult> results2 = SearchService.searchAndRank(fullQuery2, FINAL_RESULTS, iteration2Weights);

            for (SearchResult sr : results2) {
                allResults.putIfAbsent(sr.getUrl(), sr);
            }
        }

        // Final ranking
        List<SearchResult> finalResults = new ArrayList<>(allResults.values());
        finalResults.sort((a, b) -> Integer.compare(b.getRankScore(), a.getRankScore()));

        return finalResults.subList(0, Math.min(FINAL_RESULTS, finalResults.size()));
    }

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
                    current.setLength(0);
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
