package eu.dreamix.ai.my_ai_playground.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatService {
    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    HashMap <String, String> documentsByFilename = new HashMap<>();
    HashMap <String, String> topicsByFilename = new HashMap<>();


    @PostConstruct
    public void init() {
        getAllDocsResources().stream().forEach(resource -> {
            String filename = resource.getFilename();
            String content = readResourceContent(resource);
            documentsByFilename.put(filename, content);

            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage("You are a text analyzing tool. Please extract the topics from the following text and return them as a comma-separated list. Text: " + content));
            Prompt prompt = new Prompt(messages);
            String topics = chatClient.prompt(prompt).call().content();
            log.info("Extracted topics for {}: {}", filename, topics);
            topicsByFilename.put(filename, topics);
        });
    }


    public String generateTextSynchronous(String userPrompt){
        Map<String, Double> relevanceProbabilities = new HashMap<>();
        for (Map.Entry<String, String> entry : topicsByFilename.entrySet()) {
            String filename = entry.getKey();
            String topics = entry.getValue();
            
            List<Message> relevanceMessages = new ArrayList<>();
            relevanceMessages.add(new SystemMessage(
                "You are a relevance analyzer. Return only a number between 0 and 1 that represents the probability " +
                "that the user's question is related to the following topics: " + topics + 
                "\nConsider 0 as completely unrelated and 1 as highly related. Up to 3 Decimal points are expected." +
                "Respond ONLY with the number, no other text."
            ));
            relevanceMessages.add(new UserMessage(userPrompt));
            Prompt relevancePrompt = new Prompt(relevanceMessages);
            String probabilityStr = chatClient.prompt(relevancePrompt).call().content().trim();
            
            try {
                double probability = Double.parseDouble(probabilityStr);
                // Ensure the probability is between 0 and 1
                probability = Math.min(1.0, Math.max(0.0, probability));
                relevanceProbabilities.put(filename, probability);
            } catch (NumberFormatException e) {
                log.error("Failed to parse probability for file {}: {}", filename, probabilityStr);
                relevanceProbabilities.put(filename, 0.0);
            }
        }

        // Find the document with the highest relevance probability
        String mostRelevantDoc = relevanceProbabilities.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // Create the system message with additional context if a relevant document was found
        SystemMessage systemMessage;
        if (mostRelevantDoc != null && relevanceProbabilities.get(mostRelevantDoc) > 0.5) {
            String docContent = documentsByFilename.get(mostRelevantDoc);
            systemMessage = new SystemMessage(
                "You are a helpful assistant. Use the following additional context to help answer the question. " +
                        "If the context is relevant, use it in your response. If not, you can ignore it.:\n\n" +
                        "Context:\n" + docContent + "\n\n"
            );
        } else {
            systemMessage = new SystemMessage("You are a helpful assistant.");
        }


        List<Message> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.add(new UserMessage(userPrompt));
        Prompt prompt = new Prompt(messages);
        return chatClient.prompt(prompt).tools(new MyCalculatorTool()).call().content();
    }




    public List<Resource> getAllDocsResources() {
        List<Resource> resources = new ArrayList<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            // Load all resources from the docs directory
            Resource[] docsResources = resolver.getResources("classpath:docs/*");
            for (Resource resource : docsResources) {
                resources.add(resource);
            }
        } catch (IOException e) {
            log.error("Unable to load the resources.", e);
        }
        return resources;
    }
    private String readResourceContent(Resource resource) {
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            log.error("Unable to read the resource." + resource.getFilename(), e);
            return null;
        }
    }



}
