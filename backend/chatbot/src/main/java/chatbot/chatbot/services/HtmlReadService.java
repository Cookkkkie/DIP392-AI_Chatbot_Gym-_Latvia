package chatbot.chatbot.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import chatbot.chatbot.config.AppConstants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class HtmlReadService {

    private final String FAQ_URL = "https://www.gymlatvija.lv/faq";

    public String getFaqEntries() throws IOException {
        Connection.Response response = Jsoup.connect(FAQ_URL)
            .userAgent("Mozilla/5.0")
            .timeout(AppConstants.UPDATE_KNOWLEDGE_BASE_REQUEST_TIMEOUT)
            .ignoreContentType(true)
            .execute();
        Document doc = Jsoup.parse(
            new String(response.bodyAsBytes(), StandardCharsets.UTF_8),
            FAQ_URL
        );


        // Document doc = Jsoup.parse(
        //     new ByteArrayInputStream(response.bodyAsBytes()), 
        //     "UTF-8", 
        //     FAQ_URL
        // );

        List<String> entries = new ArrayList<>();
        Elements questions = doc.select("h3");

        for (Element q : questions) {
            String questionText = q.text().trim();
            // if (!isEnglish(questionText)) continue;
            List<String> answerLines = new ArrayList<>();

            Element sibling = q.nextElementSibling();
            while (sibling != null && !sibling.tagName().equals("h3")) {
                String answertxt = sibling.text().trim();
                // if (!isEnglish(answertxt)) break;
                if (!answertxt.isEmpty() && containsLettersOrDigits(answertxt)) {
                    answerLines.add(answertxt);
                }
                sibling = sibling.nextElementSibling();
            }
            if (!answerLines.isEmpty()) {
                entries.add(("Question: " + questionText + "\n" + "Answer: " + answerLines).toString());   
            }
        }
        // System.out.println(String.join("\n\n", entries));
        return String.join("\n\n", entries);
    }

    // private boolean isEnglish(String text) {
    //     // Reject text that contains any Cyrillic or Latvian-specific characters
    //     return !text.matches(".*[ĀāĒēĢģĶķĻļĪīŅņŠšŪūŽžČčА-яЁё].*");
    // }

    private boolean containsLettersOrDigits(String text) {
        return text.matches(".*[\\p{L}\\p{N}].*");
    }
}
