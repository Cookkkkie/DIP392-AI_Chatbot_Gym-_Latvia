package chatbot.chatbot;

import chatbot.chatbot.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class ChatbotService {

    private final OllamaChatClient chatModel;
    private final RedisService redisService;

    private String faqText;
    private String termsText;
    private String rulesText;
    private String privacyText;
    private String cookiesText;

    public ChatbotService(OllamaChatClient chatModel, RedisService redisService) {
        this.chatModel = chatModel;
        this.redisService = redisService;
        refreshSiteContent();  // Load data once at startup
    }

    public void refreshSiteContent() {
        System.out.println("[INFO] Refreshing GymLatvia knowledge base from web...");

        this.faqText = fetchAndStripHtml("https://www.gymlatvija.lv/faq", "FAQ fallback");
        System.out.println("[INFO] Fetched FAQ content. Length: " + faqText.length());

        this.termsText = fetchAndExtractPdf("https://static1.squarespace.com/...General-Terms-EN.pdf", "Terms fallback");
        System.out.println("[INFO] Fetched General Terms content. Length: " + termsText.length());

        this.rulesText = fetchAndExtractPdf("https://static1.squarespace.com/...Internal-Rules-EN-2025.pdf", "Rules fallback");
        System.out.println("[INFO] Fetched Internal Rules content. Length: " + rulesText.length());

        this.privacyText = fetchAndExtractPdf("https://static1.squarespace.com/...Privacy%2BPolicy-EN_2023.pdf", "Privacy fallback");
        System.out.println("[INFO] Fetched Privacy Policy content. Length: " + privacyText.length());

        this.cookiesText = fetchAndExtractPdf("https://static1.squarespace.com/...Cookie-Purpose.pdf", "Cookies fallback");
        System.out.println("[INFO] Fetched Cookies Policy content. Length: " + cookiesText.length());

        System.out.println("[INFO] Site content parsing completed successfully.");
    }

    public String handleMessageRequest(String userId, String message) {
        System.out.println("[INFO] Received userId=" + userId + " message=\"" + message + "\"");

        String conversationHistory = redisService.getHistory(userId);
        System.out.println("[INFO] Loaded conversation history. Length: " + (conversationHistory != null ? conversationHistory.length() : 0));

        String fullPrompt = buildOllamaRequest(conversationHistory, message);
        System.out.println("[INFO] Built request prompt. Length: " + fullPrompt.length());

        String response = chatModel.call(fullPrompt);
        System.out.println("[INFO] Received AI response. Length: " + response.length());

        redisService.saveHistory(userId, "\nUser:\n" + message + "\nAI:\n" + response);
        System.out.println("[INFO] Updated Redis history for userId=" + userId);

        return response;
    }

    private String buildOllamaRequest(String history, String message) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("You are chatbot for GymLatvia. Answer only gym-related questions.\n\n");

        promptBuilder.append("FAQ:\n").append(faqText).append("\n\n");
        promptBuilder.append("General Terms:\n").append(termsText).append("\n\n");
        promptBuilder.append("Internal Rules:\n").append(rulesText).append("\n\n");
        promptBuilder.append("Privacy Policy:\n").append(privacyText).append("\n\n");
        promptBuilder.append("Cookies Policy:\n").append(cookiesText).append("\n\n");

        if (history != null && !history.isBlank()) {
            promptBuilder.append("Conversation history:\n").append(history).append("\n\n");
        }

        promptBuilder.append("User message:\n").append(message);

        return promptBuilder.toString();
    }

    private String fetchAndStripHtml(String url, String fallback) {
        try {
            String html = WebClient.create().get().uri(url).retrieve().bodyToMono(String.class).block();
            if (html != null) {
                return html.replaceAll("(?s)<[^>]*>", "").trim();
            }
        } catch (Exception e) {
            System.out.println("[WARN] Failed to fetch HTML from " + url + ": " + e.getMessage());
        }
        return fallback;
    }

    private String fetchAndExtractPdf(String url, String fallback) {
        try {
            WebClient.ResponseSpec responseSpec = WebClient.builder()
                    .defaultHeader("Referer", "https://www.gymlatvija.lv/")
                    .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36")
                    .defaultHeader("Accept", "application/pdf")
                    .defaultHeader("Accept-Language", "en-US,en;q=0.9")
                    .defaultHeader("Connection", "keep-alive")
                    .defaultHeader("Sec-Fetch-Dest", "document")
                    .defaultHeader("Sec-Fetch-Mode", "navigate")
                    .defaultHeader("Sec-Fetch-Site", "same-origin")
                    .defaultHeader("Sec-Fetch-User", "?1")
                    .build()
                    .get()
                    .uri(url)
                    .retrieve();

            byte[] pdf = responseSpec.bodyToMono(byte[].class).block();

            var responseEntity = responseSpec.toBodilessEntity().block();
            System.out.println("[DEBUG] URL: " + url);
            System.out.println("[DEBUG] Status Code: " + responseEntity.getStatusCode());
            System.out.println("[DEBUG] Headers: " + responseEntity.getHeaders());

            if (pdf != null) {
                try (PDDocument doc = PDDocument.load(pdf)) {
                    return new PDFTextStripper().getText(doc).replaceAll("(?m)^\\s+", "").trim();
                }
            }
        } catch (Exception e) {
            System.out.println("[WARN] PDF fetch failed: " + url + " - " + e.getMessage());
        }
        return fallback;
    }




}


