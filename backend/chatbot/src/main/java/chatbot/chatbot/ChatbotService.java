package chatbot.chatbot;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
        String messageToOllamaFormatted = buildOllamaRequest(conversationHistory, message, InitialPrompts.HTML_PAGE != "" ? InitialPrompts.HTML_PAGE : ollamaPrompt);
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

    public void refreshSiteContent() {
        System.out.println("[INFO] Refreshing GymLatvia knowledge base from web...");

        InitialPrompts.HTML_PAGE = fetchAndStripHtml("https://www.gymlatvija.lv/faq", "HTML_PAGE fallback");
        System.out.println("[INFO] Fetched FAQ content. Length: " + InitialPrompts.HTML_PAGE.length());

        // this.termsText = fetchAndExtractPdf("https://static1.squarespace.com/...General-Terms-EN.pdf", "Terms fallback");
        // System.out.println("[INFO] Fetched General Terms content. Length: " + termsText.length());

        // this.rulesText = fetchAndExtractPdf("https://static1.squarespace.com/...Internal-Rules-EN-2025.pdf", "Rules fallback");
        // System.out.println("[INFO] Fetched Internal Rules content. Length: " + rulesText.length());

        // this.privacyText = fetchAndExtractPdf("https://static1.squarespace.com/...Privacy%2BPolicy-EN_2023.pdf", "Privacy fallback");
        // System.out.println("[INFO] Fetched Privacy Policy content. Length: " + privacyText.length());

        // this.cookiesText = fetchAndExtractPdf("https://static1.squarespace.com/...Cookie-Purpose.pdf", "Cookies fallback");
        // System.out.println("[INFO] Fetched Cookies Policy content. Length: " + cookiesText.length());

        System.out.println("[INFO] Site content parsing completed successfully.");
    }

    private String fetchAndStripHtml(String url, String fallback) {
        try {
            String html = WebClient.create().get().uri(url).retrieve().bodyToMono(String.class).block();
            if (html != null) {
                System.out.println(html.replaceAll("(?s)<[^>]*>", "").trim());
                return html.replaceAll("(?s)<[^>]*>", "").trim();
            }
        } catch (Exception e) {
            System.out.println("[WARN] Failed to fetch HTML from " + url + ": " + e.getMessage());
        }
        return fallback;
    }
}
