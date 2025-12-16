package com.example.stage5;

import com.example.stage3.SearchResult;
import com.example.stage4.SearchService;

import java.util.*;

public class SemanticSearch {

    private static final int INITIAL_RESULTS = 10;

    // 給 Spring Boot Controller 用的：/api/search?q=...&mode=...
    public static Map<String, Object> searchApi(String q, String mode) throws Exception {

        // 你們原本 stage5 的 main 是讀 stdin + parse quotes
        // 這邊 API 先簡化：直接用 q 當 query
        String fullQuery = q == null ? "" : q.trim();

        Map<String, Integer> keywordWeights = new HashMap<>();

        // 先拿 Stage4 的結果（裡面會用 Stage3 抓網頁內容）
        List<SearchResult> results = SearchService.searchAndRankWithDerived(fullQuery, INITIAL_RESULTS, keywordWeights);

        // 計算每一筆的 keyword score 與 cosine similarity
        List<Double> kwScores = new ArrayList<>();
        List<Double> sims = new ArrayList<>();
        List<Map<String, Object>> out = new ArrayList<>();

        for (SearchResult sr : results) {
            String content = (sr.getContent() != null) ? sr.getContent() : "";
            double sim = 0.0;
            try {
                sim = CosineSimilarityRanker.calculateSimilarity(fullQuery, content);
            } catch (Throwable t) {
                sim = 0.0;
            }

            double kw = (double) sr.getRankScore();
            kwScores.add(kw);
            sims.add(sim);
        }

        // Min-max normalize helper
        java.util.function.BiFunction<List<Double>, Integer, List<Double>> normalize = (list, unused) -> {
            List<Double> norm = new ArrayList<>();
            if (list.isEmpty()) return norm;
            double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
            for (double v : list) {
                if (Double.isFinite(v)) {
                    if (v < min) min = v;
                    if (v > max) max = v;
                }
            }
            if (min == Double.MAX_VALUE || max == -Double.MAX_VALUE) {
                for (int i = 0; i < list.size(); i++) norm.add(0.0);
                return norm;
            }
            if (Double.compare(max, min) == 0) {
                // If all values are identical and non-zero, give them a uniform non-zero score
                if (Double.compare(max, 0.0) != 0) {
                    for (int i = 0; i < list.size(); i++) norm.add(1.0);
                } else {
                    for (int i = 0; i < list.size(); i++) norm.add(0.0);
                }
                return norm;
            }
            for (double v : list) {
                if (!Double.isFinite(v)) {
                    norm.add(0.0);
                } else {
                    norm.add((v - min) / (max - min));
                }
            }
            return norm;
        };

        List<Double> kwNorm = normalize.apply(kwScores, 0);
        List<Double> simNorm = normalize.apply(sims, 0);

        // Combine into hybrid score (60% keyword + 40% similarity) by default
        double KW_WEIGHT = 0.6;
        double SIM_WEIGHT = 0.4;

        // Build result rows including explicit numeric score, then sort by that score
        for (int i = 0; i < results.size(); i++) {
            SearchResult sr = results.get(i);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("title", sr.getSiteName());
            try {
                row.put("url", sr.getUrl());
            } catch (Throwable t) {
                row.put("url", "");
            }

            double keywordComponent = (i < kwNorm.size() ? kwNorm.get(i) : 0.0);
            double simComponent = (i < simNorm.size() ? simNorm.get(i) : 0.0);

            double hybrid = KW_WEIGHT * keywordComponent + SIM_WEIGHT * simComponent;

            // Determine the numeric score to expose and use for ranking
            double outScore;
            if ("keyword".equalsIgnoreCase(mode)) {
                // Normalize raw keyword score into [0,1] if possible for comparability
                outScore = (i < kwNorm.size() ? kwNorm.get(i) : 0.0);
            } else if ("semantic".equalsIgnoreCase(mode)) {
                outScore = (i < simNorm.size() ? simNorm.get(i) : 0.0);
            } else {
                outScore = hybrid;
            }

            // Put numeric score (Double) and preserve original rankScore for debug if needed
            row.put("score", Double.valueOf(outScore));
            row.put("rawKeywordScore", sr.getRankScore());
            out.add(row);
        }

        // Sort descending by the numeric `score` field we added
        out.sort(Comparator.comparingDouble(m -> -((Number) m.getOrDefault("score", 0.0)).doubleValue()));

        // Add explicit rank field (1-based) to each result after sorting
        for (int i = 0; i < out.size(); i++) {
            out.get(i).put("rank", i + 1);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("query", fullQuery);
        resp.put("mode", mode);
        resp.put("count", out.size());
        resp.put("results", out);
        return resp;
    }

    // 原本 CLI 用的 main：你可以留著不用動
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter keywords for Google search (use quotes for phrases): ");
        String input = scanner.nextLine().trim();

        // 直接呼叫同一套 API 方法（讓 CLI 與 Web 共用邏輯）
        Map<String, Object> resp = searchApi(input, "semantic");
        System.out.println(resp);
    }
}
