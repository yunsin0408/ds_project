package com.example.stage5;

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
        // 潃??寥?銵停憟踝??澆?迤??Stage5 ?摩
        return SemanticSearch.searchApi(q, mode);
    }
}
