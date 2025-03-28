package chatbot.chatbot;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
public class ChatbotController  {

    private final ChatbotService _chatbotService;
    public ChatbotController(ChatbotService chatbotService) {
        _chatbotService = chatbotService;
    }

    @PostMapping("/chatbot")
    public Flux<ChatResponse> sendMessage(@RequestBody ChatbotRequest request) {
        System.out.println("Message received: " + request.message());
        return _chatbotService.handleMessageRequest(request.message());
    }
}
