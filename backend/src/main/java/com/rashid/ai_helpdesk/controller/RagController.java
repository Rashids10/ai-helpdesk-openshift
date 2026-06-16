package com.rashid.ai_helpdesk.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;


@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/rag")

public class RagController {

    private final ChatClient chatClient;

    public RagController(ChatClient ragChatClient) {
        this.chatClient = ragChatClient;
    }

    @PostMapping("ask")
    public String getAnswerUsingRag(@RequestParam String query) {
        return chatClient.prompt()
                .user(query)
                .call()
                .content();
    }

}
