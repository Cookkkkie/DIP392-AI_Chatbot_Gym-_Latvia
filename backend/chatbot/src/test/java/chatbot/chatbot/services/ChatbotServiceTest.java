package chatbot.chatbot.services;

import chatbot.chatbot.AppConstants;
import chatbot.chatbot.config.InitialPrompts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.ollama.OllamaChatClient;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private OllamaChatClient mockChatModel;

    @Mock
    private RedisService mockRedisService;

    @Mock
    private HtmlReadService mockParser;

    @InjectMocks
    private ChatbotService chatbotService;

    private String originalFaq;
    private String originalSystemMsg;
    private final String userId = UUID.randomUUID().toString();
    private final String userMsg = "some question";
    private final String botResponse = "response";
    private final String expectedAppend = "\nUser:\n" + userMsg + "\nAI:\n" + botResponse;

    @BeforeEach
    void setUp() {
        originalFaq = InitialPrompts.FAQ;
        originalSystemMsg = InitialPrompts.chatBot;
    }

    @AfterEach
    void tearDown() {
        InitialPrompts.FAQ = originalFaq;
        InitialPrompts.chatBot = originalSystemMsg;
    }

    @Test
    void handleMessageRequest_withHistoryAndFaq_buildsPromptAndUpdatesHistory() {

        String prevHistory = "User: Hi\nAI: Hello!";
        String faqData = "Q: question? A: answer.";
        String sysMsg = "Sys";

        InitialPrompts.FAQ = faqData;
        InitialPrompts.chatBot = sysMsg;

        when(mockRedisService.getHistory(userId))
                .thenReturn(prevHistory)
                .thenReturn(prevHistory + expectedAppend);
        when(mockChatModel.call(anyString())).thenReturn(botResponse);

        String actualResponse = chatbotService.handleMessageRequest(userId, userMsg);

        assertEquals(botResponse, actualResponse);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockChatModel).call(promptCaptor.capture());
        String prompt = promptCaptor.getValue();

        assertTrue(prompt.startsWith(AppConstants.CHATBOT_DEFINITION), "Missing header");
        assertTrue(prompt.contains(sysMsg), "Missing system message");
        assertTrue(prompt.contains("Reference FAQ:\n" + faqData), "Missing FAQ block");
        assertTrue(prompt.contains("Conversation history:\n" + prevHistory), "Missing conversation history");
        assertTrue(prompt.trim().endsWith(userMsg), "Must end with the user message");

        verify(mockRedisService).saveHistory(userId, expectedAppend);
    }

    @Test
    void handleMessageRequest_noHistoryAndEmptyFaq_usesSupportSuggestion() {
        InitialPrompts.FAQ = "";
        InitialPrompts.chatBot = "Sys";

        when(mockRedisService.getHistory(userId))
                .thenReturn(null)
                .thenReturn(expectedAppend);

        when(mockChatModel.call(anyString())).thenReturn(botResponse);

        String resp = chatbotService.handleMessageRequest(userId, userMsg);
        assertEquals(botResponse, resp);

        ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
        verify(mockChatModel).call(cap.capture());
        String prompt = cap.getValue();
        assertTrue(prompt.contains(
                AppConstants.KNOWLEDGE_BASE_UPDATE_FAILURE_MESSAGE),
                "Should include support suggestion");
        assertFalse(prompt.contains("Conversation history:"), "Should not mention conversation history when it's null");

        verify(mockRedisService).saveHistory(userId, expectedAppend);
    }

    @Test
    void refreshSiteContent_success_updatesFaq() throws IOException {
        String newFaq = "Loaded FAQ content";
        when(mockParser.getFaqEntries()).thenReturn(newFaq);

        chatbotService.refreshSiteContent();
        assertEquals(newFaq, InitialPrompts.FAQ, "InitialPrompts.FAQ should be updated from parser");
        verify(mockParser, times(1)).getFaqEntries();
    }

    @Test
    void refreshSiteContent_failure_throwsRuntimeException() throws IOException {
        when(mockParser.getFaqEntries()).thenThrow(new IOException("I/O error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> chatbotService.refreshSiteContent());
        assertTrue(ex.getMessage().contains(AppConstants.NO_FAQ_ERROR_MESSAGE));
        verify(mockParser).getFaqEntries();
    }
}
