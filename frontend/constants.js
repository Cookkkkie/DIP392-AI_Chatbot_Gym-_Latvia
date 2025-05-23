export const CHATBOT = {
    CHATBOT_API_BASE_URL: 'http://localhost:8080',

    RESPONDER: {
        AI: "ai",	
        USER: "user",
    },

    ERRORS: {
        GENERIC_USER_FRIENDLY: "Something went wrong. Please try again.",
        LOAD_HISTORY_ERROR: "Failed to load history of conversation. Please try again.",
        NETWORK_ERROR: "Network response was not ok",
        NO_RESPONSE: "No response",
    },
    COOKIE_USER_ID: "userId",
    COOKIE_USER_ID_EXPIRY_MINUTES: 30,
    
    MESSAGES: {
        EN: {
            WELCOME: "Hello!👋 I am your assistant from a Gym Latvia! 💪",
        },
        LV: {
            WELCOME: "Sveiki!👋 Esmu jūsu palīgs no Gym Latvia!",
        },
        RU: {
            WELCOME: "Привет!👋 Я твой ассистент от Gym Latvia!💪",
        },
    }
}