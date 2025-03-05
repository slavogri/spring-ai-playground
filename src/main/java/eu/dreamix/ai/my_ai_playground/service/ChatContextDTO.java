package eu.dreamix.ai.my_ai_playground.service;

import java.util.List;

public class ChatContextDTO {
    List<MessageDTO> messages;

    // some inner structure...
    public record MessageDTO (String message, Role role){}
    public enum Role {
        DEVELOPER("developer"),
        USER("user"),
        ASSISTANT("assistant");

        private final String roleName;

        Role(String roleName) {
            this.roleName = roleName;
        }
        public String getRoleName() {
            return roleName;
        }
    }
}
