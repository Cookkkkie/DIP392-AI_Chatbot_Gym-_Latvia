package chatbot.chatbot.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class HtmlReadService {

    private final String faqUrl = "https://www.gymlatvija.lv/faq";

    public String getFaqEntries() throws IOException {
        Document doc = Jsoup.connect(faqUrl)
                .userAgent("Mozilla/5.0")
                .timeout(10_000)
                .get();

        List<String> entries = new ArrayList<>();
        Elements questions = doc.select("h3");

        for (Element q : questions) {
            String questionText = q.text().trim();
            List<String> answerLines = new ArrayList<>();

            Element sibling = q.nextElementSibling();
            while (sibling != null && !sibling.tagName().equals("h3")) {
                String txt = sibling.text().trim();
                if (!txt.isEmpty()) {
                    answerLines.add(txt);
                }
                sibling = sibling.nextElementSibling();
            }

            entries.add(("Question: " + questionText +"\n"+ "Answer: " + answerLines).toString());
        }
//        System.out.println(String.join("\n\n", entries));
        return String.join("\n\n", entries);
    }
}
