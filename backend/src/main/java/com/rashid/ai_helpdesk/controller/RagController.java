package com.rashid.ai_helpdesk.controller;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;


@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/rag")
public class RagController {

    private static final String FALLBACK_ANSWER =
            "Keine Information gefunden. Bitte wenden Sie sich an IT-Support.";

    private final VectorStore vectorStore;

    public RagController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostMapping(value = "/ask", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getAnswerUsingRag(@RequestParam String query) {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(1)
                .similarityThreshold(0.3)
                .build());

        if (documents == null || documents.isEmpty()) {
            return ResponseEntity.ok(FALLBACK_ANSWER);
        }

        String answer = documents.getFirst().getText();

        if (answer == null || answer.isBlank()) {
            return ResponseEntity.ok(FALLBACK_ANSWER);
        }

        return ResponseEntity.ok(answer);
    }

}
