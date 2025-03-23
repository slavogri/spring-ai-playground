package eu.dreamix.ai.my_ai_playground.controller;

import eu.dreamix.ai.my_ai_playground.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * /api/chat/text-generation
     */
    @PostMapping("/text-generation")
    public String generateTextSynchronous(@RequestBody String text) {

        return chatService.generateTextSynchronous(text);
    }

}
