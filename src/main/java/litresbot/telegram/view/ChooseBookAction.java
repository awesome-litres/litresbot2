package litresbot.telegram.view;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import litresbot.books.BookInfoFiltered;
import litresbot.localisation.UserMessagesEn;
import litresbot.telegram.SendMessageList;

public class ChooseBookAction {
    public static SendMessageList show(BookInfoFiltered book, Long bookId, boolean canRead) {
        var result = new SendMessageList();

        // generate the book info header
        result.appendTextPage("<b>");
        final var titles = String.join("\n", book.titles);
        result.appendTextPage(titles);
        result.appendTextPage("</b>\n");

        if (!book.authors.isEmpty()) {
            result.appendTextPage("(");
            final var authors = String.join(", ", book.authors);
            result.appendTextPage(authors);
            result.appendTextPage(")\n");
        }

        if (book.annotation != null && !book.annotation.isEmpty()) {
            result.appendTextPage("\n");
            result.appendTextPage(book.annotation);
            result.appendTextPage("\n");
        }

        result.endTextPage();

        // generate the book info download and read buttons

        var btn1 = new InlineKeyboardButton();
        btn1.setText(litresbot.Application.userMessages.get(UserMessagesEn.searchDownload));
        btn1.setCallbackData("/format " + bookId);
        var buttonsRow = new ArrayList<InlineKeyboardButton>();
        buttonsRow.add(btn1);

        if (canRead) {
            InlineKeyboardButton btn2 = new InlineKeyboardButton();
            btn2.setText(litresbot.Application.userMessages.get(UserMessagesEn.searchRead));
            btn2.setCallbackData("/read " + bookId);
            buttonsRow.add(btn2);
        }

        var buttons = new ArrayList<List<InlineKeyboardButton>>();
        buttons.add(buttonsRow);
        result.appendButtons(buttons);

        return result;
    }
}
