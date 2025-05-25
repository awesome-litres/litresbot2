package litresbot.telegram;

public class TelegramEscape {
    public static String escapeText(String text) {
        if (text == null)
            return null;

        final var escapedText = text.replace("<", "&lt;").replace("&", "&amp;").replace(">", "&gt;").replace("\"",
                "&quot;");
        return escapedText;
    }
}
