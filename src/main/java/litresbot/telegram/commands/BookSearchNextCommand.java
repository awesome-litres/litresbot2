package litresbot.telegram.commands;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import litresbot.Application;
import litresbot.SearchBook;
import litresbot.localisation.UserMessagesEn;
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
        // take the rest of the command as an argument since it may contain spaces
        final var argument = message.substring(command.length() + 1);
        final var fromBook = Integer.parseInt(argument);
        bookSearchNext(chatId, fromBook);
    }

    protected void bookSearchNext(Long chatId, int fromBook) throws TelegramApiException {
        var books = botState.getSearchResults(chatId);
        // restores the search query from the DB in case the bot was restarted
        if (books.isEmpty()) {
            var searchQuery = "";
            try {
                searchQuery = botState.getSearchQuery(chatId);
            } catch (SQLException e) {
                logger.warn("Could not restore search query from the bot state: " + e.getMessage(), e);
            }
            if (searchQuery.isEmpty()) {
                bot.sendReply(chatId, Application.userMessages.get(UserMessagesEn.errorUnknown));
                return;
            }
            try {
                books = SearchBook.search(searchQuery);
            } catch (SQLException e) {
                logger.error("Error searching for book: " + e.getMessage(), e);
                bot.sendReply(chatId, Application.userMessages.get(UserMessagesEn.errorUnknown));
                return;
            }
            try {
                botState.newSearch(chatId, searchQuery, books);
            } catch (SQLException e) {
                logger.warn("Could not save search query to the bot state: " + e.getMessage(), e);
            }
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

        final var reply = TelegramView.bookSearchResult(books, fromBook, booksSearchTo, nextPage);
        bot.sendReply(chatId, reply);
    }
}
