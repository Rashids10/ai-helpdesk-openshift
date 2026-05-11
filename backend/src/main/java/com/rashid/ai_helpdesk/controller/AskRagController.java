package com.rashid.ai_helpdesk.controller;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rashid.ai_helpdesk.payload.request.QuestionRequest;
import com.rashid.ai_helpdesk.payload.response.AnswerResponse;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/ask")

public class AskRagController {

    private final ChatClient aiClient;
    private final VectorStore vectorStore;

    @Value("classpath:/rag-prompt-template.st")
    private Resource ragPromptTemplate;

    public AskRagController(
            ChatClient aiClient,
            VectorStore vectorStore) {

        this.aiClient = aiClient;
        this.vectorStore = vectorStore;

    }

    /*
     * @PostMapping
     * public AnswerResponse ask(@RequestBody QuestionRequest request) {
     * 
     * List<Document> similarDocuments = vectorStore
     * .similaritySearch(
     * SearchRequest.query(request.question())
     * .withTopK(2));
     * 
     * List<String> contentList = similarDocuments.stream()
     * .map(Document::getContent)
     * .toList();
     * 
     * String answer = aiClient.prompt()
     * .user(userSpec -> userSpec
     * .text(ragPromptTemplate)
     * .param("input", request.question())
     * .param("documents", String.join("\n", contentList)))
     * .call()
     * .content();
     * 
     * return new AnswerResponse(answer);
     * }
     */

    
    @PostMapping
    public AnswerResponse ask(@RequestBody QuestionRequest request) {

        List<Document> similarDocuments =
        vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(request.question())
                        .topK(2)
                        .build()
        );

    List<String> contentList = similarDocuments.stream()
        .map(document -> document.getFormattedContent())
        .toList();


        return new AnswerResponse("Demo");
    }

}
