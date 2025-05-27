package litresbot.telegram.view;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import litresbot.Application;
import litresbot.books.BookInfo;
import litresbot.books.plurals.PluralsTextEn;
import litresbot.books.plurals.PluralsTextRu;
import litresbot.localisation.UserMessagesEn;
import litresbot.telegram.SendMessageList;

public class TelegramView {
    public static String welcomeScreen() {
        return Application.userMessages.get(UserMessagesEn.welcomeScreen);
    }

    public static String helpScreen() {
        return Application.userMessages.get(UserMessagesEn.helpCommands) + "\n" +
                "/start - " + Application.userMessages.get(UserMessagesEn.helpStart) + "\n" +
                "/help - " + Application.userMessages.get(UserMessagesEn.helpHelp) + "\n" +
                "/book - " + Application.userMessages.get(UserMessagesEn.helpBook);
    }

    public static SendMessageList bookInfoNotFound() {
        return SendMessageList.fromText(Application.userMessages.get(UserMessagesEn.errorSearchNotFound));
    }

    public static SendMessageList bookCouldNotDownload() {
        return SendMessageList.fromText(Application.userMessages.get(UserMessagesEn.errorCouldNotDownloadFile));
    }

    public static SendMessageList searchInProgress() {
        return SendMessageList.fromText(Application.userMessages.get(UserMessagesEn.searchInProgress));
    }

    /*public static SendMessageList bookChooseFormat(BookInfo book) {
        SendMessageList result = new SendMessageList(4096);

        // generate the book info header

        result.appendTextPage("<b>");
        result.appendTextPage(book.title);
        result.appendTextPage("</b>\n");

        if (book.author != null) {
            result.appendTextPage(" (");
            result.appendTextPage(book.author);
            result.appendTextPage(")\n");
        }

        result.endTextPage();

        // generate keyboard with download formats

        List<InlineKeyboardButton> buttonsRow = new ArrayList<InlineKeyboardButton>();

        for (BookFileLink link : book.links) {
            InlineKeyboardButton btn1 = new InlineKeyboardButton();
            btn1.setText(link.format.toUpperCase());
            btn1.setCallbackData("/download " + link.format.toLowerCase() + " " + book.id);
            buttonsRow.add(btn1);
        }

        List<List<InlineKeyboardButton>> buttons = new ArrayList<List<InlineKeyboardButton>>();
        buttons.add(buttonsRow);
        result.appendButtons(buttons);

        return result;
    }*/

    /*public static SendMessageList bookChooseAction(BookInfo book, boolean canRead) {
        SendMessageList result = new SendMessageList(4096);

        // generate the book info header

        result.appendTextPage("<b>");
        result.appendTextPage(book.title);
        result.appendTextPage("</b>\n");

        if (book.author != null) {
            result.appendTextPage(" (");
            result.appendTextPage(book.author);
            result.appendTextPage(")\n");
        }

        if (book.annotation != null) {
            result.appendTextPage("\n");
            result.appendTextPage(book.annotation);
            result.appendTextPage("\n");
        }

        result.endTextPage();

        // generate the book info download and read buttons

        List<InlineKeyboardButton> buttonsRow = new ArrayList<InlineKeyboardButton>();
        InlineKeyboardButton btn1 = new InlineKeyboardButton();
        btn1.setText(litresbot.Application.userMessages.get(UserMessagesEn.searchDownload));
        btn1.setCallbackData("/format " + book.id);
        buttonsRow.add(btn1);

        if (canRead) {
            InlineKeyboardButton btn2 = new InlineKeyboardButton();
            btn2.setText(litresbot.Application.userMessages.get(UserMessagesEn.searchRead));
            btn2.setCallbackData("/read " + book.id);
            buttonsRow.add(btn2);
        }

        List<List<InlineKeyboardButton>> buttons = new ArrayList<List<InlineKeyboardButton>>();
        buttons.add(buttonsRow);
        result.appendButtons(buttons);

        return result;
    }*/

    public static SendMessageList downloadInProgress() {
        return SendMessageList.fromText(Application.userMessages.get(UserMessagesEn.downloadInProgress));
    }

    public static SendMessageList downloadFinished() {
        return SendMessageList.fromText(Application.userMessages.get(UserMessagesEn.downloadFinished));
    }

    public static SendMessageList bookSearchResult(List<BookInfo> books, int from, int to, String next) {
        var result = new SendMessageList(4096);

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

        // generate the book search next buttons

        final var buttonsRow = new ArrayList<InlineKeyboardButton>();

        final var btn = new InlineKeyboardButton();
        btn.setText(litresbot.Application.userMessages.get(UserMessagesEn.endText));
        btn.setCallbackData("/");
        if (next != null && !next.isEmpty() && !next.equals("/")) {
            btn.setText(litresbot.Application.userMessages.get(UserMessagesEn.nextText));
            btn.setCallbackData(next);
        }
        buttonsRow.add(btn);

        final var buttons = new ArrayList<List<InlineKeyboardButton>>();
        buttons.add(buttonsRow);
        result.appendButtons(buttons);
        return result;
    }

    public static SendMessageList readBookSection(SendMessageList output, String line, String next, int pageCount, int pageNumber) {
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
