package litresbot.telegram.view;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import litresbot.books.BookInfoFiltered;
import litresbot.books.plurals.PluralsTextEn;
import litresbot.books.plurals.PluralsTextRu;
import litresbot.localisation.UserMessagesEn;
import litresbot.telegram.SendMessageList;

public class BookSearchResult {
    public static SendMessageList show(List<BookInfoFiltered> books, int from, int to, String next) {
        var result = new SendMessageList();

        // generate the search result header - how much books found
        if (from == 0) {
            final var bookText = litresbot.Application.userMessages.get(UserMessagesEn.bookText);
            var booksText = PluralsTextEn.convert(bookText, books.size());

            if (litresbot.Application.userMessages.language().contentEquals("ru")) {
                booksText = PluralsTextRu.convert(bookText, books.size());
            }

            result.appendTextPage(
                    litresbot.Application.userMessages.get(UserMessagesEn.searchFoundTotal) +
                            books.size() + " " + booksText + "\n\n");
            result.endTextPage();
        }

        // generate the search result body
        for (var bookNumber = from; bookNumber < to; bookNumber++) {
            final var book = books.get(bookNumber);

            if (book.titles.size() == 0) {
                continue;
            }

            // only first title appears in the book search result
            result.appendTextPage("<b>");
            result.appendTextPage("" + (bookNumber + 1) + ". " + book.titles.get(0));
            result.appendTextPage("</b>\n");

            // only first author appears in the book search result
            if (book.authors.size() > 0) {
                result.appendTextPage(" (");
                result.appendTextPage(book.authors.get(0));
                result.appendTextPage(")\n");
            }

            result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.searchGoto));
            result.appendTextPage("/b_" + (bookNumber + 1));

            result.appendTextPage("\n\n");
            result.endTextPage();
        }

        // if there are no search pages, don't show navigation buttons
        if (from == 0 && (next.isEmpty() || next.equals("/"))) {
            return result;
        }

        // generate the book search next buttons
        final var btn = new InlineKeyboardButton();
        btn.setText(litresbot.Application.userMessages.get(UserMessagesEn.endText));
        btn.setCallbackData("/");
        if (next != null && !next.isEmpty() && !next.equals("/")) {
            btn.setText(litresbot.Application.userMessages.get(UserMessagesEn.nextText));
            btn.setCallbackData(next);
        }
        final var buttonsRow = new ArrayList<InlineKeyboardButton>();
        buttonsRow.add(btn);
        final var buttons = new ArrayList<List<InlineKeyboardButton>>();
        buttons.add(buttonsRow);
        result.appendButtons(buttons);
        return result;
    }
}
