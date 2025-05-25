# DIP392 AI Chatbot for Gym! Latvia 🧠🏋️‍♀️
This repository contains the source code for a centralized AI chatbot system designed to assist Gym Latvia clients by answering frequently asked questions and providing helpful information. It utilizes OpenAI-compatible models (via Ollama LLM), Redis for chat history management, and Spring Boot for backend API handling. The chatbot dynamically fetches FAQ data from the Gym Latvia website to stay up-to-date.
---
Report file:
https://docs.google.com/document/d/1JwBFa9Y62uL0T_Wz_tGedC55M6PRoJ_qmPZWMozQNpQ/edit?usp=sharing

## Project overview:
The AI chatbot system supports:
- Real-time communication with users through a REST API.
- Automatic integration of FAQ data from the Gym Latvia website.
- Conversation history tracking via Redis.
- Prompt optimization and system prompts for accurate responses.
- A modular design to separate responsibilities (AI client, Redis service, FAQ reader, etc.).
---

## ⚙️ Setup & Run Instructions
### Prerequisites
- Java 21 or later
- Maven 3.6+
- Redis server running locally.
- Ollama or other OpenAI-compatible LLM backend.

### 1. Clone the Repository

```bash
git clone https://github.com/Cookkkkie/DIP392-AI_Chatbot_Gym-_Latvia.git
cd DIP392-AI_Chatbot_Gym-_Latvia
```
### 2. Configure the Application
```bash
Edit application.p[roperties file:
ollama.base-url=http://localhost:11434
faq.source-url=https://www.gym.lv/faq
spring.redis.host=localhost
spring.redis.port=6379
```
### 3. Run the Application using Maven
```bash
mvn clean install
mvn spring-boot:run
```
### 4. Test the API
```bash
  POST http://localhost:5050/api/chat
  Content-Type: application/json
  
  {
    "userId": "GUID",
    "message": "What are the working hours in Gym?"
  }
```
### Team Members & Roles
Mykyta Medvediev: Technical Lead and architect
Oleksii Pecheniuk: Backend Developer 1.
Ruslan Dzhubuev: Backend Developer 2.
Mariana Mechyk: Q/A testing
Ayomide Akintola: Functional developer. 
Agboola Peter: Functional developer. 

### Demo video link
https://drive.google.com/file/d/1JY0xtZNR56QcW5EbdWtqo72pxZZm0hrE/view?usp=sharing
### License
This project is developed for academic purposes as part of the DIP392 Applied System Software(English) 2024/2025 course and is not intended for commercial use for now.
