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
        List<SearchResult> results = SearchService.searchAndRank(fullQuery, INITIAL_RESULTS, keywordWeights);

        // 把結果轉成 JSON-friendly 的 List<Map<...>>
        List<Map<String, Object>> out = new ArrayList<>();

        for (SearchResult sr : results) {
            String content = (sr.getContent() != null) ? sr.getContent() : "";
            double sim = CosineSimilarityRanker.calculateSimilarity(fullQuery, content);

            // 這裡先用 siteName 當 title（你們 class 裡確定有 getSiteName）
            // url 如果你們 SearchResult 沒有 getUrl()，等編譯錯誤出現我再教你換成正確 getter
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("title", sr.getSiteName());
            try {
                row.put("url", sr.getUrl());
            } catch (Throwable t) {
                row.put("url", "");
            }

            // mode=keyword 就回傳 keyword 分數；mode=semantic 就回 cosine
            if ("keyword".equalsIgnoreCase(mode)) {
                row.put("score", sr.getRankScore());
            } else {
                row.put("score", sim);
            }

            out.add(row);
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
