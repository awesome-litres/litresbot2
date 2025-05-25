package litresbot.telegram;

import java.util.ArrayList;

import org.jsoup.Jsoup;

public class TelegramFilterHtml {
    // Telegram supported tags
    // <b>bold</b>, <strong>bold</strong>
    // <i>italic</i>, <em>italic</em>
    // <u>underline</u>, <ins>underline</ins>
    // <s>strikethrough</s>, <strike>strikethrough</strike>,
    // <del>strikethrough</del>
    public static String filterText(String text) {
        final var doc = Jsoup.parseBodyFragment(text);
        final var divElements = doc.select("a, b, strong, i, em, u, ins, s, strike, del, br, :matchText");
        String[] formatElements = { "a", "b", "strong", "i", "em", "u", "ins", "s", "strike", "del" };
        final var texts = new ArrayList<String>(divElements.size());
        for (final var el : divElements) {
            // add newline in the end of <p>
            if (el.tagName().equalsIgnoreCase("p")) {
                var pText = el.wholeText();
                if (el.parent() != null && (el.siblingIndex() + 1 >= el.parent().childNodeSize())) {
                    pText += "\n";
                }
                texts.add(pText);
                continue;
            }
            // add newline when we get <br>
            if (el.tagName().equalsIgnoreCase("br")) {
                texts.add("\n");
                continue;
            }
            var gotFormat = false;
            for (final var f : formatElements) {
                if (el.tagName().equalsIgnoreCase(f)) {
                    texts.add(el.outerHtml());
                    gotFormat = true;
                    break;
                }
            }
            if (gotFormat)
                continue;

            if (el.hasText()) {
                texts.add(el.wholeText());
            }
        }
        return String.join("", texts);
    }
}
