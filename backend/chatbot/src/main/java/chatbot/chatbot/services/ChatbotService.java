package chatbot.chatbot.services;

import chatbot.chatbot.config.InitialPrompts;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Service
public class ChatbotService {

    private final OllamaChatClient chatModel;
    private final RedisService redisService;
    private final HtmlReadService parser;

    @Autowired
    public ChatbotService(OllamaChatClient _chatModel, RedisService _redisService, HtmlReadService _parser) {
        chatModel = _chatModel;
        redisService = _redisService;
        parser = _parser;
    }

    public String handleMessageRequest(String userId, String message) {
        log.info("Received message from userId={}, messageLength={} chars", userId, message == null ? 0 : message.length());
        String conversationHistory = redisService.getHistory(userId);
        log.debug("UserId={} conversationHistory length={} chars", userId, conversationHistory == null ? 0 : conversationHistory.length());

        String messageToOllamaFormatted = buildOllamaRequest(conversationHistory, message, InitialPrompts.FAQ, InitialPrompts.chatBot);
        log.debug("Built Ollama prompt for userId={}, promptLength={} chars", userId, messageToOllamaFormatted.length());

        String response = chatModel.call(messageToOllamaFormatted);
        log.info("Received response from Ollama for userId={}, responseLength={} chars", userId, response.length());

        redisService.saveHistory(userId, "\nUser:\n" + message + "\nAI:\n" + response);
        log.debug("Updated conversationHistory for userId={}, newLength={} chars", userId, redisService.getHistory(userId).length());

        return response;
    }

    private String buildOllamaRequest(String history,
                                      String userMessage,
                                      String faq,
                                      String systemMessage) {

        StringBuilder sb = new StringBuilder();

        sb.append("You are a GymLatvija chatbot. Follow these rules:\n")
                .append(systemMessage).append("\n\n");

        if (faq == null || faq.isEmpty()) {
            log.warn("FAQ knowledge base is unavailable, user queries will not have FAQ context");
            sb.append("The knowledge base is not available at the moment. Suggest the customer contacts support at info@gymlatija.lv.");
        } else {
            sb.append("Reference FAQ:\n").append(faq).append("\n\n");
        }

        if (history != null && !history.isBlank()) {
            sb.append("Conversation history:\n").append(history).append("\n\n");
        }

        sb.append("Based on context, answer message. Use the language of the message provided: ").append(userMessage).append("\n");

        return sb.toString();
    }

    @PostConstruct
    public void refreshSiteContent() {
        log.info("Loading GymLatvia FAQ at startup...");
        try {
            InitialPrompts.FAQ = parser.getFaqEntries();
            log.info("Successfully loaded FAQ, length {} chars", InitialPrompts.FAQ.length());
        } catch (IOException e) {
            log.error("Failed to load FAQ on startup", e);
            throw new RuntimeException("Could not initialize FAQ", e);
        }
    }
}


//    public Flux<ChatResponse> generateStream(String message) {
//        // Prompt prompt = new Prompt(new UserMessage(conversationHistory.toString()));
//        // Flux<ChatResponse> stream = chatModel.stream(prompt);
//        return null;
//    }