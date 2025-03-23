package eu.dreamix.ai.my_ai_playground.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {
    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;


    HashMap <String, String> documentsByFilename = new HashMap<>();
//    HashMap <String, String> topicsByFilename = new HashMap<>();
    HashMap <String, float[]> embedingsByFilename = new HashMap<>();


    @PostConstruct
    public void init() {
        getAllDocsResources().stream().forEach(resource -> {
            String filename = resource.getFilename();
            String content = readResourceContent(resource);
            documentsByFilename.put(filename, content);


            float[] embeding = embeddingModel.embed(content);
            embedingsByFilename.put(filename,embeding);
//            List<Message> messages = new ArrayList<>();
//            messages.add(new SystemMessage("You are a text analyzing tool. Please extract the topics from the following text and return them as a comma-separated list. Text: " + content));
//            Prompt prompt = new Prompt(messages);
//            String topics = chatClient.prompt(prompt).call().content();
//            log.info("Extracted topics for {}: {}", filename, topics);
//            topicsByFilename.put(filename, topics);
        });
    }


    public String generateTextSynchronous(String userPrompt){
        float[] userPromptEmbeding = embeddingModel.embed(userPrompt);

        Map<String, Double> currentPromptSimilarityToDocumentByFilename = new HashMap<>();
        for (Map.Entry<String, float[]> entry : embedingsByFilename.entrySet()) {
            String filename = entry.getKey();
            float[] embeding = entry.getValue();

            // Calculate cosine similarity between user prompt embedding and document embedding
            double similarity = calculateSimilarity(userPromptEmbeding, embeding);
            currentPromptSimilarityToDocumentByFilename.put(filename, similarity);
        }

        // Find the document with the highest relevance probability
        String[] relevantDoc = getRelevantDocuments(currentPromptSimilarityToDocumentByFilename, 2, 0.6);

        SystemMessage systemMessage = new SystemMessage(
                "You are a helpful assistant. Use the following additional context to help answer the question. " +
                        "If the context is relevant, use it in your response. If not, you can ignore it.:\n\n" +
                        "Context:\n" + Arrays.toString(relevantDoc) + "\n"
        );

        List<Message> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.add(new UserMessage(userPrompt));
        Prompt prompt = new Prompt(messages);
            return chatClient.prompt(prompt).tools(new MyCalculatorTool()).call().content();
    }

    private String[] getRelevantDocuments(Map<String, Double> currentPromptSimilarityToDocumentByFilename, int firstNDocuments, double similarityThreshold) {
        String [] fileNames = currentPromptSimilarityToDocumentByFilename.entrySet().stream()
                .filter(entry -> entry.getValue() > similarityThreshold)
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(firstNDocuments).map(Map.Entry::getKey).toArray(String[]::new);

        String[] documents = new String[fileNames.length];
        for (int j = 0; j < fileNames.length; j++) {
            documents[j] = documentsByFilename.get(fileNames[j]);
        }
        return documents;
    }

    private static double calculateSimilarity(float[] userPromptEmbeding, float[] embeding) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < userPromptEmbeding.length; i++) {
            dotProduct += userPromptEmbeding[i] * embeding[i];
            normA += userPromptEmbeding[i] * userPromptEmbeding[i];
            normB += embeding[i] * embeding[i];
        }
        double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        return similarity;
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
