package litresbot.telegram.view;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import litresbot.books.BookInfoFiltered;
import litresbot.telegram.SendMessageList;

public class ChooseBookFormat {
    public static SendMessageList show(BookInfoFiltered book, Long bookId) {
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
        result.endTextPage();

        // generate keyboard with download formats
        var buttonsRow = new ArrayList<InlineKeyboardButton>();
        for (final var f : book.files) {
            var btn1 = new InlineKeyboardButton();
            btn1.setText(f.fileType.toUpperCase());
            btn1.setCallbackData("/download " + f.fileType.toLowerCase() + " " + bookId);
            buttonsRow.add(btn1);
        }

        var buttons = new ArrayList<List<InlineKeyboardButton>>();
        buttons.add(buttonsRow);
        result.appendButtons(buttons);
        return result;
    }
}
