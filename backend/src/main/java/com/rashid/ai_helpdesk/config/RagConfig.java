package com.rashid.ai_helpdesk.config;

import java.io.File;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class RagConfig {

    @Value("${app.resource}")
    private String resourcePath;

    // das ist suchindex

    @Value("${app.vectorstore.path:/tmp/vectorstore.json}")
    private String vectorStorePath;


    /*
    1. Sende Frage ans LLM
2. Hole Antwort zurück
     */
    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }


      @Bean
    SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {

        /*
        AI-Suchindex
         */

        SimpleVectorStore simpleVectorStore =
    SimpleVectorStore.builder(embeddingModel).build();
        File vectorStoreFile = new File(vectorStorePath);
      
        if (vectorStoreFile.exists()) { // load existing vect
        // or store if exists
            simpleVectorStore.load(vectorStoreFile);
        } else { // otherwise load the documents and save the vector store
            TikaDocumentReader documentReader = new TikaDocumentReader(resourcePath);
            List<Document> documents = documentReader.get();
            TextSplitter textSplitter = new TokenTextSplitter();
            List<Document> splitDocuments = textSplitter.apply(documents);
            simpleVectorStore.add(splitDocuments);
            simpleVectorStore.save(vectorStoreFile);
        }
        return simpleVectorStore;
    }

}
