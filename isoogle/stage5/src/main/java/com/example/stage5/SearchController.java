package com.example.stage5;

import com.example.stage4.IterativeKeywordSearch;
import com.example.stage3.SearchResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class SearchController {

    @GetMapping("/api/ping")
    public Map<String, Object> ping() {
        return Map.of("ok", true);
    }

    @GetMapping("/api/search")
    public Map<String, Object> search(
            @RequestParam("q") String q,
            @RequestParam(value = "mode", defaultValue = "keyword") String mode
    ) throws Exception {

        if ("iterative".equalsIgnoreCase(mode)) {
            List<SearchResult> results = IterativeKeywordSearch.run(q);

            List<Map<String, Object>> simple = new ArrayList<>();
            for (SearchResult sr : results) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("title", sr.getSiteName());     // 顯示名稱
                item.put("url", sr.getUrl());            // 連結
                item.put("score", sr.getRankScore());    // 可選：debug用
                simple.add(item);
            }

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("mode", "iterative");
            resp.put("count", simple.size());
            resp.put("results", simple);
            return resp;
        }

        // 其它模式維持原本行為（keyword/semantic）
        return SemanticSearch.searchApi(q, mode);
    }
}
