package litresbot.telegram.commands;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import litresbot.books.BookInfoFiltered;
import litresbot.telegram.TelegramBot;
import litresbot.telegram.TelegramBotState;
import litresbot.telegram.view.BookSearchResult;
import litresbot.telegram.view.ProgressMessages;

public class BookSearchNextCommand implements TelegramCommandInterface {
    public static final String command = "/next";
    protected final TelegramBot bot;
    protected final TelegramBotState botState;

    BookSearchNextCommand(TelegramBot bot, TelegramBotState botState) {
        this.bot = bot;
        this.botState = botState;
    }

    @Override
    public Boolean isCommand(String cmd) {
        return cmd.startsWith(command);
    }

    @Override
    public void execute(long chatId, String message) throws TelegramApiException {
        final var argument = message.substring(command.length() + 1);
        final var fromBook = Integer.parseInt(argument);
        bookSearchNext(chatId, fromBook);
    }

    protected void bookSearchNext(long chatId, int fromBook) throws TelegramApiException {
        var books = botState.getSearchResults(chatId);
        if (books.isEmpty()) {
            final var reply = ProgressMessages.retrySearch();
            bot.sendReply(chatId, reply);
            return;
        }
        var nextPage = "/";
        var booksSearchTo = books.size();
        if (booksSearchTo > fromBook + BookSearchCommand.searchPageSize) {
            booksSearchTo = fromBook + BookSearchCommand.searchPageSize;
            nextPage = "/next " + booksSearchTo;
        }
        if (booksSearchTo < fromBook) {
            booksSearchTo = fromBook;
        }
         // Filter books for XSS protection
        final var filteredBooks = books.stream().map(book -> {
            return new BookInfoFiltered(book);
        }).toList();

        final var reply = BookSearchResult.show(filteredBooks, fromBook, booksSearchTo, nextPage);
        bot.sendReply(chatId, reply);
    }
}
