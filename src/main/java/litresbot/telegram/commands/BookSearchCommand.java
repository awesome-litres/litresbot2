package litresbot.telegram.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import litresbot.Application;
import litresbot.SearchBook;
import litresbot.books.BookInfo;
import litresbot.localisation.UserMessagesEn;
import litresbot.telegram.TelegramBot;
import litresbot.telegram.TelegramBotState;
import litresbot.telegram.view.TelegramView;

public class BookSearchCommand implements TelegramCommandInterface {
    final static Logger logger = LogManager.getLogger(BookSearchCommand.class);
    public static final String command = "/book";
    protected final TelegramBot bot;
    protected final TelegramBotState botState;

    public static int searchPageSize = 10;

    BookSearchCommand(TelegramBot bot, TelegramBotState botState) {
        this.bot = bot;
        this.botState = botState;
    }

    @Override
    public Boolean isCommand(String cmd) {
        return cmd.startsWith(command);
    }

    @Override
    public void execute(Long chatId, String message) throws TelegramApiException {
        // take the rest of the command as an argument since it may contain spaces
        String argument = message;
        if (message.startsWith(command)) {
            // remove the command and trailing space from the string
            argument = message.substring(command.length() + 1);
        }
        bookSearch(chatId, argument);
    }

    protected void bookSearch(Long chatId, String searchQuery) throws TelegramApiException {
        bot.sendReply(chatId, TelegramView.searchInProgress());
        List<BookInfo> books = new ArrayList<>();
        try {
            books = SearchBook.search(searchQuery);
        } catch (SQLException e) {
            logger.error("Error searching for book: " + e.getMessage(), e);
            bot.sendReply(chatId, Application.userMessages.get(UserMessagesEn.errorUnknown));
            return;
        }
        botState.newSearch(chatId, searchQuery, books);
        var nextPage = "/";
        var booksSearchTo = books.size();
        if (booksSearchTo > searchPageSize) {
            booksSearchTo = searchPageSize;
            nextPage = "/next " + booksSearchTo;
        }
        
        final var reply = TelegramView.bookSearchResult(books, 0, booksSearchTo, nextPage);
        bot.sendReply(chatId, reply);
    }
}
