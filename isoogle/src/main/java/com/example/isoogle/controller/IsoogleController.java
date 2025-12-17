package com.example.isoogle.controller;

import com.example.stage3.WebAnalyzer;
import com.example.stage3.SearchResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class IsoogleController {

    @GetMapping("/api/cse")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "mode", required = false, defaultValue = "semantic") String mode
    ) {
        try {
            String m = mode == null ? "semantic" : mode.toLowerCase();

            switch (m) {
                case "iterative": {
                    Map<String, Object> resp = com.example.stage4.IterativeKeywordSearch.searchApi(query);
                    int count = resp.get("results") instanceof java.util.Collection
                            ? ((java.util.Collection<?>) resp.get("results")).size()
                            : (resp.get("count") instanceof Number ? ((Number) resp.get("count")).intValue() : 0);
                    resp.putIfAbsent("mode", "iterative");
                    resp.put("count", count);
                    return ResponseEntity.ok(resp);
                }

                case "cse":
                case "google": {
                    try {
                        List<String> keywords = Arrays.asList(query.toLowerCase().split("\\s+"));
                        Map<String, Integer> keywordWeights = new HashMap<>();
                        for (String kw : keywords) {
                            keywordWeights.put(kw, 4);
                        }

                        List<SearchResult> searchResults = WebAnalyzer.analyzeGoogleRankedSites(query, keywords, keywordWeights, 5);

                        List<Map<String, Object>> items = new ArrayList<>();
                        List<String> logs = new ArrayList<>();
                        logs.add("Fetched " + searchResults.size() + " results from Google CSE");
                        for (SearchResult sr : searchResults) {
                            Map<String, Object> it = new HashMap<>();
                            it.put("title", sr.getSiteName());
                            it.put("url", sr.getUrl());
                            it.put("score", sr.getRankScore());
                            items.add(it);
                            if (sr.getContent() == null || sr.getContent().trim().isEmpty()) {
                                logs.add("Empty content for: " + sr.getUrl());
                            }
                        }

                        Map<String, Object> resp = new HashMap<>();
                        resp.put("originalKeywords", Collections.singletonList(query));
                        resp.put("derivedKeywords", Collections.emptyList());
                        resp.put("mode", "cse");
                        resp.put("results", items);
                        resp.put("count", items.size());
                        resp.put("logs", logs);
                        return ResponseEntity.ok(resp);
                    } catch (Exception e) {
                        Map<String, Object> errorResp = new HashMap<>();
                        errorResp.put("error", e.getMessage());
                        errorResp.put("logs", Collections.singletonList("Keyword search failed: " + e.getMessage()));
                        return ResponseEntity.status(500).body(errorResp);
                    }
                }

                case "semantic":
                default: {
                    Map<String, Object> resp = com.example.stage5.SemanticSearch.searchApi(query, "semantic");
                    // Standardize keys
                    if (resp.containsKey("query")) {
                        resp.put("originalKeywords", resp.remove("query"));
                    }
                    if (!resp.containsKey("derivedKeywords")) {
                        resp.put("derivedKeywords", Collections.emptyList()); // no derived for semantic
                    }
                    int count = resp.get("results") instanceof java.util.Collection
                            ? ((java.util.Collection<?>) resp.get("results")).size()
                            : (resp.get("count") instanceof Number ? ((Number) resp.get("count")).intValue() : 0);
                    resp.putIfAbsent("mode", "semantic");
                    resp.put("count", count);
                    return ResponseEntity.ok(resp);
                }
            }

        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "internal error"));
        }
    }

    /**
     * Backwards-compatible alias for the frontend which previously called `/api/search?q=...`.
     */
    @GetMapping("/api/search")
    public ResponseEntity<Map<String, Object>> searchAlias(
            @RequestParam(name = "q") String q,
            @RequestParam(name = "mode", required = false, defaultValue = "semantic") String mode
    ) {
        return search(q, mode);
    }
}