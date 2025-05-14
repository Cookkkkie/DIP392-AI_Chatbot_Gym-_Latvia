package chatbot.chatbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import chatbot.chatbot.ChatbotRequest;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Autowired
    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }



    @PostMapping("/message")
    public String handleMessage(@RequestBody ChatbotRequest request) {
        System.out.println("Got message: " + request.message + " from user: " + request.userId);
        return "Test OK: received message";
    }


    @GetMapping("/refresh")
    public String refreshData() {
        chatbotService.refreshSiteContent();
        return "Knowledge base has been refreshed successfully.";
    }
}
