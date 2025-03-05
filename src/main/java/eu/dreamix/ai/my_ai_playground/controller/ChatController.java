package eu.dreamix.ai.my_ai_playground.controller;

import eu.dreamix.ai.my_ai_playground.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * /api/chat/text-generation
     */
    @PostMapping("/text-generation")
    public String generateTextSynchronous(@RequestBody String text) {

        return chatService.generateTextSynchronous(text);
    }

}
