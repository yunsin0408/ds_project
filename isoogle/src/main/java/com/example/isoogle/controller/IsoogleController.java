package com.example.isoogle.controller;

import com.example.stage3.GoogleQuery;
import com.example.stage5.SemanticSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
public class IsoogleController {

    @Autowired
    private GoogleQuery googleQuery; // left for backward compatibility / other use

    @GetMapping("/api/cse")
    public ResponseEntity<Map<String, Object>> search(@RequestParam(name = "query") String query) {
        try {
            // Delegate to Stage5 SemanticSearch so the API returns ranked results (score + rank)
            Map<String, Object> resp = SemanticSearch.searchApi(query, "semantic");
            return ResponseEntity.ok(resp);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "internal error"));
        }
    }
}
