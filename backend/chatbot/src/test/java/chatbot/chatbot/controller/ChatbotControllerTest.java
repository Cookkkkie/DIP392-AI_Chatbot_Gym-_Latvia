package chatbot.chatbot.controller;

import java.util.Map;
import java.util.UUID;

import chatbot.chatbot.config.AppConstants;
import chatbot.chatbot.dto.ChatbotRequest;
import chatbot.chatbot.services.ChatbotService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ChatbotControllerTest {

    @Mock
    private ChatbotService chatbotService;

    @InjectMocks
    private ChatbotController controller;

    @Test
    void sendMessage_whenCalled_delegatesToServiceAndReturnsMap() {
        String userId  = UUID.randomUUID().toString();
        String message = "user message";
        String mockReplyMessage = "bot reply";

        ChatbotRequest req = new ChatbotRequest(userId, message);
        when(chatbotService.handleMessageRequest(userId, message)).thenReturn(mockReplyMessage);

        Map<String, String> result = controller.sendMessage(req);

        assertNotNull(result, "Should never return null");
        assertEquals(mockReplyMessage, result.get("generation"));

        verify(chatbotService, times(1)).handleMessageRequest(userId, message);
    }

    @Test
    void refreshData_whenCalled_invokesServiceAndReturnsConfirmation() {
        String resp = controller.refreshData();

        assertEquals(
                AppConstants.KNOWLEDGE_BASE_UPDATE_SUCCESS_MESSAGE,
                resp,
                "Message returned does not match expected value."
        );

        verify(chatbotService, times(1)).refreshSiteContent();
    }
}
