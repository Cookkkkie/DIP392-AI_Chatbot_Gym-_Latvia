package chatbot.chatbot;

public class AppConstants {
    public static final String KNOWLEDGE_BASE_UPDATE_SUCCESS_MESSAGE = "Knowledge base has been refreshed successfully.";
    public static final String KNOWLEDGE_BASE_UPDATE_FAILURE_MESSAGE = "The knowledge base is not available at the moment. Suggest the customer contacts support at info@gymlatija.lv.";
    public static final String NO_FAQ_ERROR_MESSAGE = "Could not initialize FAQ";
    public static final String CHATBOT_DEFINITION = "You are a GymLatvija chatbot. Follow these rules:\\n";
    public static final Integer CONVERSATION_HISTORY_EXPIRATION_TIME = 30; // in minutes
    public static final String[] CORS_ALLOWED_ORIGINS = {
        "http://localhost:4200",
        "http://127.0.0.1:5500", // Live server of HTML page
        "https://www.gymlatvija.lv"
    };
}
