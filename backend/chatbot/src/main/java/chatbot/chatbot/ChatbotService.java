package chatbot.chatbot;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;


@Service
public class ChatbotService {

    private final OllamaChatClient _chatModel;
    
    @Autowired
    public ChatbotService(OllamaChatClient chatModel) {
        _chatModel = chatModel;
    }

    public String handleMessageRequest(String message) {
        // here will be request to AI API to get response
        System.out.println("Message received in Service: " + message);
        // Prompt prompt = new Prompt(new UserMessage(message));
        return _chatModel.call(message);
    }  

    public Flux<ChatResponse> generateStream(String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return _chatModel.stream(prompt);
    }
}
