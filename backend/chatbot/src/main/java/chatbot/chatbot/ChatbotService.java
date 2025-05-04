package chatbot.chatbot;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Service
public class ChatbotService {

    private final OllamaChatClient chatModel;
    private final RedisService redisService;
    private final String ollamaPrompt;

    @Autowired
    public ChatbotService(OllamaChatClient _chatModel, RedisService _redisService) {
        chatModel = _chatModel;
        redisService = _redisService;
        this.ollamaPrompt = InitialPrompts.chatBot
                    + InitialPrompts.FAQ
                    + InitialPrompts.GeneralTerms
                    + InitialPrompts.InternalRules
                    + InitialPrompts.Policies;
    }

    public String handleMessageRequest(String userId, String message) {
        String conversationHistory = redisService.getHistory(userId);
        log.info("\nHistory of conversation" + conversationHistory);

        String messageToOllamaFormatted = buildOllamaRequest(conversationHistory, message, ollamaPrompt);
        String response = chatModel.call(messageToOllamaFormatted);

        redisService.saveHistory(userId, "\nUser:\n" + message + "\nAI:\n" + response);
        log.info("\nHistory of conversation after response:\n" + redisService.getHistory(userId));
        return response;
    }

    public Flux<ChatResponse> generateStream(String message) {
        // Prompt prompt = new Prompt(new UserMessage(conversationHistory.toString()));
        // Flux<ChatResponse> stream = chatModel.stream(prompt);
        return null;
    }

    private String buildOllamaRequest(String conversationHistory, String message, String ollamaPrompt) {
        if (conversationHistory == null || conversationHistory.isBlank()) {
            return String.format("Answer the following message: %s using this knowledge base: %s", message, ollamaPrompt);
        }
    
        return String.format("According to previous history %s, answer the following message: %s using this knowledge base: %s",
                             conversationHistory, message, ollamaPrompt);
    }
}
