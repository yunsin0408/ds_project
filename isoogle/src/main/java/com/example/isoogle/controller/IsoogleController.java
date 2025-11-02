package com.example.isoogle.controller;

import com.example.isoogle.config.GoogleCseProperties;
import com.example.isoogle.service.GoogleQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class IsoogleController {

    private final GoogleCseProperties props;

    public IsoogleController(GoogleCseProperties props) {
        this.props = props;
    }

    @GetMapping("/api/cse")
    public ResponseEntity<Map<String, String>> search(@RequestParam(name = "query") String query) {
        if (!props.isEnabled()) {
            return ResponseEntity.ok(Collections.emptyMap());
        }

        try {
            GoogleQuery gq = new GoogleQuery(query);
            HashMap<String, String> results = gq.queryCse(query, props.getApiKey(), props.getCx());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyMap());
        }
    }
}
