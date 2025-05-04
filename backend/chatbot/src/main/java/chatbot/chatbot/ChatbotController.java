package chatbot.chatbot;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.ChatResponse;

import reactor.core.publisher.Flux;

@Slf4j
@RestController
public class ChatbotController  {

    private final ChatbotService _chatbotService;
    public ChatbotController(ChatbotService chatbotService) {
        _chatbotService = chatbotService;
    }

    @PostMapping("/chatbot")
    public Map<String, String> sendMessage(@RequestBody ChatbotRequest request) {
        log.info("Message received: " + request.message() + " from user id: " + request.userId());
        var response = _chatbotService.handleMessageRequest(request.userId(), request.message());
        return Map.of("generation", response);
    }

    @GetMapping("/ai/generateStream")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return _chatbotService.generateStream(message);
    }
}
