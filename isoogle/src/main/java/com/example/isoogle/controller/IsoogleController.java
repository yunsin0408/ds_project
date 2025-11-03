package com.example.isoogle.controller;

import com.example.isoogle.service.GoogleQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class IsoogleController {

    @Value("${google.cse.enabled:true}")
    private boolean enabled;

    @Value("${GOOGLE_CSE_APIKEY}")
    private String apiKey;

    @Value("${GOOGLE_CSE_CX}")
    private String cx;

    @GetMapping("/api/cse")
    public ResponseEntity<Map<String, String>> search(@RequestParam(name = "query") String query) {
        if (!enabled) {
            return ResponseEntity.ok(Collections.emptyMap());
        }

        // Require CSE configuration;
        if (apiKey == null || apiKey.isEmpty() || cx == null || cx.isEmpty()) {
            return ResponseEntity.status(400)
                    .body(Collections.singletonMap("error", "CSE not configured: set google.cse.apiKey and google.cse.cx"));
        }

        try {
            GoogleQuery gq = new GoogleQuery(query);
            HashMap<String, String> results = gq.queryCse(query, apiKey, cx);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyMap());
        }
    }
}
