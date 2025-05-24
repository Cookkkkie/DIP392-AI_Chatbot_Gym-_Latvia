import { CHATBOT } from "./constants.js";
// handle user id - unique key that is used for keeping the conversation of the history
// Function to get a cookie by name
function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop().split(";").shift();
}

// Function to set a cookie with a specified name, value, and expiration time in minutes
function setCookie(name, value, minutes) {
  const date = new Date();
  date.setTime(date.getTime() + minutes * 60 * 1000); // 30 minutes
  const expires = "; expires=" + date.toUTCString();
  document.cookie = name + "=" + (value || "") + expires + "; path=/";
}

// Function to get or create a unique user ID
function getOrCreateUserId() {
  let userId = getCookie(CHATBOT.COOKIE_USER_ID);
  if (!userId) {
    userId = crypto.randomUUID();
    setCookie(CHATBOT.COOKIE_USER_ID, userId, CHATBOT.COOKIE_USER_ID_EXPIRY_MINUTES);
  }
  return userId;
}

const userId = getOrCreateUserId();

window.addEventListener("DOMContentLoaded", () => {
  const welcomeMessage = [
    CHATBOT.MESSAGES.LV?.WELCOME,
    CHATBOT.MESSAGES.RU?.WELCOME,
    CHATBOT.MESSAGES.EN?.WELCOME
  ].filter(Boolean).join("..");

  // Set initial message
  addMessage(welcomeMessage, CHATBOT.RESPONDER.AI);

  // Get history of conversation
  fetch(`${CHATBOT.CHATBOT_API_BASE_URL}/conversation?userId=${userId}`)
    .then((response) => response.text())
    .then((history) => {
      const parts = history.split(/(User:|AI:)/).filter(Boolean);
      for (let i = 1; i < parts.length; i += 2) {
        let sender = "";
        let senderRaw = parts[i].trim().toLowerCase();
        if (senderRaw === "user:" || senderRaw === "user") sender = CHATBOT.RESPONDER.USER;
        else if (senderRaw === "ai:" || senderRaw === "ai") sender = CHATBOT.RESPONDER.AI;
        const message = parts[i + 1]?.trim();
        if (sender && message) {
          addMessage(message, sender === CHATBOT.RESPONDER.USER ? CHATBOT.RESPONDER.USER : CHATBOT.RESPONDER.AI);
        }
      }
    })
    .catch(() => {
      addMessage(CHATBOT.ERRORS.LOAD_HISTORY_ERROR, CHATBOT.RESPONDER.AI);
    });
});

// Handle sending messages and displaying responses
const sendButton = document.querySelector(".send-button");
const chatInput = document.querySelector(".chat-input");
const messages = document.querySelector("#chat-messages");

// Function to add a message to the chat UI
function addMessage(text, sender = CHATBOT.RESPONDER.USER) {
  const msgDiv = document.createElement("div");
  msgDiv.classList.add("message", sender);
  msgDiv.innerHTML = `<p>${text}</p>`;
  messages.appendChild(msgDiv);
  messages.scrollTop = messages.scrollHeight;
}

// Handle input and button states. Prevent user from sending multiple messages when chatbot is typing
function setInputEnabled(enabled) {
  chatInput.disabled = !enabled;
  sendButton.disabled = !enabled;
  sendButton.style.opacity = enabled ? "1" : "0.5";
  sendButton.style.cursor = enabled ? "pointer" : "not-allowed";
}

sendButton.addEventListener("click", function () {
  const userInput = chatInput.value.trim();
  if (userInput === "") return;

  setInputEnabled(false);

  addMessage(userInput, CHATBOT.RESPONDER.USER);
  chatInput.value = "";

  const typingDiv = document.createElement("div");
  typingDiv.classList.add("message", CHATBOT.RESPONDER.AI);
  typingDiv.id = "typing-indicator";
  typingDiv.innerHTML = `
          <div class="typing-indicator">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>`;
  messages.appendChild(typingDiv);
  messages.scrollTop = messages.scrollHeight;

  fetch(CHATBOT.CHATBOT_API_BASE_URL + "/chatbot", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      userId: userId,
      message: userInput,
    }),
  })
    .then((response) => {
      if (!response.ok) throw new Error(CHATBOT.ERRORS.NETWORK_ERROR);
      return response.json();
    })
    .then((data) => {
      const typing = document.getElementById("typing-indicator");
      if (typing) typing.remove();
      addMessage(data.generation || data.reply || CHATBOT.ERRORS.NO_RESPONSE, CHATBOT.RESPONDER.AI);
    })
    .catch(() => {
      const typing = document.getElementById("typing-indicator");
      if (typing) typing.remove();
      addMessage(CHATBOT.ERRORS.GENERIC_USER_FRIENDLY, CHATBOT.RESPONDER.AI);
    })
    .finally(() => {
      setInputEnabled(true);
    });
});

chatInput.addEventListener("keypress", function (e) {
  if (e.key === "Enter") sendButton.click();
});