package litresbot.telegram.commands;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import litresbot.Application;
import litresbot.localisation.UserMessagesEn;
import litresbot.telegram.TelegramBot;
import litresbot.telegram.TelegramBotState;
import litresbot.telegram.view.TelegramView;

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
    public void execute(Long chatId, String message) throws TelegramApiException {
        // take the rest of the command as an argument since it may contain spaces
        final var argument = message.substring(command.length() + 1);
        final var fromBook = Integer.parseInt(argument);
        bookSearchNext(chatId, fromBook);
    }

    protected void bookSearchNext(Long chatId, int fromBook) throws TelegramApiException {
        var books = botState.getSearchResults(chatId);
        if (books.isEmpty()) {
            final var searchQuery = botState.getSearch(chatId);
            if (searchQuery.isEmpty()) {
                bot.sendReply(chatId, Application.userMessages.get(UserMessagesEn.errorUnknown));
                return;
            }
            final var defaultCommand = new BookSearchCommand(bot, botState);
            bot.sendBusy(chatId);
            defaultCommand.execute(chatId, searchQuery);
            books = botState.getSearchResults(chatId);
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
