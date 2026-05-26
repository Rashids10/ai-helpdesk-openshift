package com.rashid.ai_helpdesk.config;


import java.util.Arrays;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DataInitializer {

 @Autowired 
  private  VectorStore vectorStore;


  @PostConstruct
    public void initData(){

        TextReader textReader = new TextReader(new ClassPathResource("/docs/rag-knowledge-base.md"));
        // TokenTextSplitter splitter = TokenTextSplitter.builder().build();


        // TokenTextSplitter splitter = TokenTextSplitter.builder()
        // .withChunkSize(100)
        // .withMinChunkSizeChars(20)
        // .withMinChunkLengthToEmbed(5)
        // .withMaxNumChunks(1000)
        // .build();
        
        // // List<Document> documents = splitter.split(textReader.get());      
    

     String content = textReader.get().getFirst().getText();

String[] faqChunks = content.split("---");

List<Document> documents = Arrays.stream(faqChunks)
    .map(Document::new)
    .toList();

vectorStore.add(documents);
    }    
}
