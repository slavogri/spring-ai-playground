package eu.dreamix.ai.my_ai_playground.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class StructuredChatService {
    private final ChatClient chatClient;
    private final SimpleVectorStore vectorStore;


    public CarDTO generateTextSynchronous(String userPrompt) {
        List<Message> messages = new ArrayList<>();
        BeanOutputConverter<CarDTO> outputConverter = new BeanOutputConverter<>(CarDTO.class);
        String systemMessage = String.format("""
                You are a helpful chat assistant,
                always in a good mood, making jokes of everything, but still polite and respectful.
                %s""", outputConverter.getFormat());
        messages.add(new SystemMessage(systemMessage));

        new PromptTemplate(userPrompt);

        Prompt prompt = new Prompt(messages);

        return outputConverter.convert(chatClient.prompt(prompt).call().content());
    }

}
