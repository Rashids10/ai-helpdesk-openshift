package com.rashid.ai_helpdesk.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

class AiConfigTest {

    @Test
    void ragClientSearchesWithRawQuestionAndInjectsRetrievedContext() {
        VectorStore vectorStore = mock(VectorStore.class);
        OllamaChatModel chatModel = mock(OllamaChatModel.class);
        String question = "How do I request vacation?";
        String context = """
                ## Vacation Request
                1. Open the Vacation Request portal.
                2. Select the desired dates.
                3. Submit the request.
                4. Wait for manager approval.
                """;

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(new Document(context)));
        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(
                        new Generation(new AssistantMessage("Open the Vacation Request portal.")))));

        AiConfig config = new AiConfig();
        QuestionAnswerAdvisor advisor = config.questionAnswerAdvisor(vectorStore);
        ChatClient chatClient = config.ragChatClient(chatModel, advisor);

        String answer = chatClient.prompt()
                .user(question)
                .call()
                .content();

        ArgumentCaptor<SearchRequest> searchRequest = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(searchRequest.capture());
        assertThat(searchRequest.getValue().getQuery()).isEqualTo(question);

        ArgumentCaptor<Prompt> prompt = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(prompt.capture());
        assertThat(prompt.getValue().getUserMessage().getText())
                .contains(question)
                .contains(context.trim());
        assertThat(prompt.getValue().getSystemMessage().getText())
                .contains("ausschließlich mit Informationen aus dem bereitgestellten Kontext");
        assertThat(answer).isEqualTo("Open the Vacation Request portal.");
    }

}
