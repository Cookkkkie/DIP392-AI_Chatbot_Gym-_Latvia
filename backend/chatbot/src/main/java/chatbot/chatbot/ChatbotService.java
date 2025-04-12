package chatbot.chatbot;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatbotService {

    private final OllamaChatClient chatModel;
    private final StringBuilder conversationHistory = new StringBuilder();

    @Autowired
    public ChatbotService(OllamaChatClient _chatModel) {
        chatModel = _chatModel;
        conversationHistory.append(InitialPrompts.chatBot);
        conversationHistory.append(InitialPrompts.FAQ);
        conversationHistory.append(InitialPrompts.GeneralTerms);
        conversationHistory.append(InitialPrompts.internalRules);
        conversationHistory.append(InitialPrompts.policies);

    }

    public String handleMessageRequest(String message) {
        conversationHistory.append("\nUser: ").append(message);
        String response = chatModel.call(conversationHistory.toString());
        conversationHistory.append("\nAI: ").append(response);
        return response;
    }

    public Flux<ChatResponse> generateStream(String message) {
        conversationHistory.append("\nUser: ").append(message);
        Prompt prompt = new Prompt(new UserMessage(conversationHistory.toString()));
        Flux<ChatResponse> stream = chatModel.stream(prompt);
        return stream;
    }
}
