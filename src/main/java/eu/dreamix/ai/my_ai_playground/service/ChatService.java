package eu.dreamix.ai.my_ai_playground.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final Message systemMessage = new SystemMessage("You are a scientific chatbot, you speak very exactly. And you are very helpful.");

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String generateTextSynchronous(String text){
        List<Message> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.add(new UserMessage(text));

        Prompt prompt = new Prompt(messages);
        List<Generation> results = chatClient.prompt(prompt).call().chatResponse().getResults();
        return chatClient.prompt(prompt).call().content();
    }

}
