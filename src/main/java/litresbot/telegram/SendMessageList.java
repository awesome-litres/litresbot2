package litresbot.telegram;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class SendMessageList {
    private int max = 2048;

    private String scratchpadPage = "";
    private String currentTextChunk = "";

    private List<String> pages = new ArrayList<String>();
    private List<SendMessage> messages = new ArrayList<SendMessage>();

    public SendMessageList() {}

    public SendMessageList(int max) {
        this.max = max;
    }

    public static SendMessageList fromText(String text) {
        final var message = new SendMessageList();
        message.appendTextPage(text);
        message.endTextPage();
        return message;
    }

    public void endTextPage() {
        if ((currentTextChunk.length() + scratchpadPage.length()) > max) {
            pages.add(scratchpadPage);
            scratchpadPage = currentTextChunk;
            currentTextChunk = "";
            return;
        }

        scratchpadPage += currentTextChunk;
        currentTextChunk = "";
    }

    public void appendTextPage(String msg) {
        currentTextChunk += msg;
    }

    // appends buttons to the last message in list
    public void appendButtons(List<List<InlineKeyboardButton>> buttons) {
        prepareMessages();

        if (messages.size() == 0) {
            /// HACK: do not add buttons if no messages in list
            return;
        }

        final var lastMessage = messages.get(messages.size() - 1);
        messages.remove(messages.size() - 1);

        final var kb = new InlineKeyboardMarkup();
        kb.setKeyboard(buttons);
        final var sendMessage = new SendMessage();
        sendMessage.setText(lastMessage.getText());
        sendMessage.setReplyMarkup(kb);
        sendMessage.enableHtml(true);
        messages.add(sendMessage);
    }

    public List<SendMessage> getMessages() {
        prepareMessages();
        return messages;
    }

    protected void prepareMessages() {
        // flush current text to page
        if (currentTextChunk.length() > 0) {
            endTextPage();
        }

        // and flush current page to pages list
        if (scratchpadPage.length() > 0) {
            pages.add(scratchpadPage);
            scratchpadPage = "";
        }

        // and flush pages list to message list
        for (String page : pages) {
            final var sendMessage = new SendMessage();
            sendMessage.setText(page.toString());
            sendMessage.enableHtml(true);
            messages.add(sendMessage);
        }

        pages.clear();
    }
}
