package eu.dreamix.ai.my_ai_playground.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String generateTextSynchronous(String text){

        return chatClient.prompt(text).call().content();

    }

}
