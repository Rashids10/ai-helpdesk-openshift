package com.rashid.ai_helpdesk.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;


@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/rag")

public class RagController {

    private final ChatClient chatModel;

   
    // private ChatBotService chatBotService;

  


    @Autowired
    private EmbeddingModel embaEmbeddingModel;

    @Autowired
    private VectorStore vectorStore;

    public RagController(
            VectorStore vectorStore,
            OllamaChatModel chatModel
      ) {

        this.vectorStore = vectorStore;
        this.chatModel = ChatClient.create(chatModel);
   
    }

    @PostMapping("ask")

    public String getAnswerUsingRag(@RequestParam String query) {

        ChatResponse chatResponse = chatModel
.prompt("""
Du bist ein IT-FAQ Assistent.

WICHTIG:
- Antworte NUR mit Informationen aus dem bereitgestellten Kontext.
- Wenn keine passende Information im Kontext vorhanden ist,
  antworte EXAKT mit:

"Keine Information gefunden. Bitte wenden Sie sich an IT-Support."

- Erfinde keine Informationen.
- Gib keine allgemeinen Tipps.
- Gib keine Vermutungen.
- Gib keine generischen IT-Erklärungen.

Frage:
""" + query)
    .advisors(
        QuestionAnswerAdvisor.builder(vectorStore)
            .searchRequest(
                SearchRequest.builder()
                    .topK(2)
                    .similarityThreshold(0.7f)
                    .build()
            )
            .build()
    )
    .call()
    .chatResponse();

        vectorStore.similaritySearch(
        SearchRequest.builder()
                .query(query)
                .topK(2)
                .build()
);
// .forEach(doc -> System.out.println(doc.getText()));
// String answer = chatModel.prompt().advisors(retrievalAugmentationAdvisor).user(query).call().content();

        return chatResponse.getResult().getOutput().getText();
    }


//  @GetMapping("/bot")
// public ResponseEntity<String> askBot(@RequestParam String userQuerry) {

//     String response= chatBotService.getBotResponse(userQuerry);

//     return ResponseEntity.ok(response);

// }

}
