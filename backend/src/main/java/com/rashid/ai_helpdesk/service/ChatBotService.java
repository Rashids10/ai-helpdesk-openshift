/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.rashid.ai_helpdesk.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service

public class ChatBotService {

  private ResourceLoader  resourceLoader;

@Value("classpath:prompts/chatbot-rag.prompt.st")

private  Resource  promptResource;



// public String getBotResponse(String userQuery) {
//     try {

//         // // Liest den gesamten Inhalt der Prompt-Datei
// // // und wandelt die Bytes in einen UTF-8 String um
//         String promptTemplate = new String(
//                 promptResource.getInputStream().readAllBytes(),
//                 StandardCharsets.UTF_8
//         );

//         System.out.println(promptTemplate);

//         return promptTemplate;
//     } catch (java.io.IOException e) {
//         throw new RuntimeException("Prompt-Datei konnte nicht geladen werden", e);
//     }
// }


// private String fetchSementicContext(String userQuery){


//     List<Document>  documents =vectorStore.similaritySearch(SearchRequest.builder().query(userQuery).topK(3).similarityThreshold(0.7f)
//     build());
//     return null;
// }


}

