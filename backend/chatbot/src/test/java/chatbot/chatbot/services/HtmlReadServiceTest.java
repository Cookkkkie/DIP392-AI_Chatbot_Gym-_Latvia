package chatbot.chatbot.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class HtmlReadServiceTest {

    @Test
    void getFaqEntries_happyPath_returnsFormattedEntries() throws IOException {

        String html = "<html><body>"
                + "<h3>Q1?</h3>"
                + "<p>Answer line 1.</p>"
                + "<p>Answer line 2.</p>"
                + "<h3>Q2?</h3>"
                + "<div>Only one line.</div>"
                + "</body></html>";

        Document fakeDoc = Jsoup.parse(html);

        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            Connection conn = mock(Connection.class);
            jsoupMock.when(() -> Jsoup.connect("https://www.gymlatvija.lv/faq"))
                    .thenReturn(conn);
            when(conn.userAgent(anyString())).thenReturn(conn);
            when(conn.timeout(anyInt())).thenReturn(conn);
            when(conn.get()).thenReturn(fakeDoc);

            HtmlReadService svc = new HtmlReadService();
            String result = svc.getFaqEntries();


            String expected =
                    "Question: Q1?\n" +
                            "Answer: [Answer line 1., Answer line 2.]\n\n" +
                            "Question: Q2?\n" +
                            "Answer: [Only one line.]";

            assertEquals(expected, result);
        }
    }

    @Test
    void getFaqEntries_noQuestions_returnsEmptyString() throws IOException {
        Document emptyDoc = Jsoup.parse("<html><body><p>No headings here</p></body></html>");

        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            Connection conn = mock(Connection.class);
            jsoupMock.when(() -> Jsoup.connect(anyString())).thenReturn(conn);
            when(conn.userAgent(anyString())).thenReturn(conn);
            when(conn.timeout(anyInt())).thenReturn(conn);
            when(conn.get()).thenReturn(emptyDoc);

            HtmlReadService svc = new HtmlReadService();
            String result = svc.getFaqEntries();

            assertTrue(result.isEmpty(), "Expected no entries when there are no <h3> tags");
        }
    }

    @Test
    void getFaqEntries_ioException_bubblesUp() throws IOException {
        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            Connection conn = mock(Connection.class);
            jsoupMock.when(() -> Jsoup.connect(anyString())).thenReturn(conn);
            when(conn.userAgent(anyString())).thenReturn(conn);
            when(conn.timeout(anyInt())).thenReturn(conn);
            when(conn.get()).thenThrow(new IOException("Network down"));

            HtmlReadService svc = new HtmlReadService();
            IOException ex = assertThrows(IOException.class, svc::getFaqEntries);
            assertEquals("Network down", ex.getMessage());
        }
    }
}
