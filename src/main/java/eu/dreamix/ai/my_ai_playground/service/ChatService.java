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
    private final Message systemMessage = new SystemMessage("You are a childish chatbot, you speak as a 5 years old. And you are very helpful. If a toolcall fails with an exception, take it into account,and don't make up the result.");

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String generateTextSynchronous(String text){
        List<Message> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.add(new UserMessage(text));
        Prompt prompt = new Prompt(messages);
        return chatClient.prompt(prompt).tools(new MyCalculatorTool()).call().content();
    }

}
