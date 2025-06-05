package litresbot.telegram.view;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import litresbot.localisation.UserMessagesEn;
import litresbot.telegram.SendMessageList;

public class TelegramView {
    public static SendMessageList readBookSection(SendMessageList output, String line, String next, int pageCount,
            int pageNumber) {
        output.appendTextPage(line + "\n\n");
        output.appendTextPage("------------------\n");
        output.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.pageNumberText) + pageNumber + " / "
                + pageCount + "\n");
        output.endTextPage();

        // generate the book next page

        final var buttonsRow = new ArrayList<InlineKeyboardButton>();

        final var btn = new InlineKeyboardButton();
        btn.setText(litresbot.Application.userMessages.get(UserMessagesEn.endText));
        btn.setCallbackData("/");
        if (next != null) {
            btn.setText(litresbot.Application.userMessages.get(UserMessagesEn.nextText));
            btn.setCallbackData(next);
        }
        buttonsRow.add(btn);

        final var buttons = new ArrayList<List<InlineKeyboardButton>>();
        buttons.add(buttonsRow);
        output.appendButtons(buttons);

        return output;
    }
}
