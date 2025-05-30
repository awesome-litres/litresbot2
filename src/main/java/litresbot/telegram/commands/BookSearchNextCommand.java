package litresbot.telegram.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import litresbot.books.BookInfoFiltered;
import litresbot.telegram.TelegramBot;
import litresbot.telegram.TelegramBotState;
import litresbot.telegram.view.TelegramView;

public class BookSearchNextCommand implements TelegramCommandInterface {
    final static Logger logger = LogManager.getLogger(BookSearchNextCommand.class);
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
    public void execute(Long chatId, String message) throws TelegramApiException {
        final var argument = message.substring(command.length() + 1);
        final var fromBook = Integer.parseInt(argument);
        bookSearchNext(chatId, fromBook);
    }

    protected void bookSearchNext(Long chatId, int fromBook) throws TelegramApiException {
        var books = botState.getSearchResults(chatId);
        if (books.isEmpty()) {
            final var reply = TelegramView.retrySearch();
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
        final var filteredBooks = books.stream().map(book -> {
            // Filter book info to reduce size of the message
            return new BookInfoFiltered(book);
        }).toList();

        final var reply = TelegramView.bookSearchResult(filteredBooks, fromBook, booksSearchTo, nextPage);
        bot.sendReply(chatId, reply);
    }
}
