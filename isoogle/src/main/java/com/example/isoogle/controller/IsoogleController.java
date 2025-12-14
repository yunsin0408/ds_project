package com.example.isoogle.controller;

import com.example.stage3.GoogleQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class IsoogleController {

    @Autowired
    private GoogleQuery googleQuery;

    @GetMapping("/api/cse")
    public ResponseEntity<Map<String, String>> search(@RequestParam(name = "query") String query) {
        try {
            HashMap<String, String> results = googleQuery.query(query);
            return ResponseEntity.ok(results);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyMap());
        }
    }
}
