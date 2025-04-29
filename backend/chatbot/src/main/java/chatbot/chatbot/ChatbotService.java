package chatbot.chatbot;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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
        redisService.saveHistory(userId, "\nUser:\n" + message);
        String conversationHistory = redisService.getHistory(userId);
        System.out.println("\nHistory of conversation" + conversationHistory);
        String response = chatModel.call(ollamaPrompt + conversationHistory);
        redisService.saveHistory(userId, "\nAI:\n" + response);
        System.out.println("\nHistory of conversation after response:\n" + conversationHistory);
        return response;
    }

    public Flux<ChatResponse> generateStream(String message) {
        // Prompt prompt = new Prompt(new UserMessage(conversationHistory.toString()));
        // Flux<ChatResponse> stream = chatModel.stream(prompt);
        return null;
    }
}
