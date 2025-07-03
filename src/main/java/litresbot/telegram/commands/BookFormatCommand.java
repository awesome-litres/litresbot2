package litresbot.telegram.commands;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import litresbot.books.BookInfoFiltered;
import litresbot.telegram.TelegramBot;
import litresbot.telegram.TelegramBotState;
import litresbot.telegram.view.ChooseBookFormat;
import litresbot.telegram.view.ProgressMessages;

public class BookFormatCommand implements TelegramCommandInterface {
    public static final String command = "/format";
    protected final TelegramBot bot;
    protected final TelegramBotState botState;

    BookFormatCommand(TelegramBot bot, TelegramBotState botState) {
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
        final var bookId = Long.parseLong(argument);
        bookFormat(chatId, bookId);
    }

    protected void bookFormat(long chatId, long bookId) throws TelegramApiException {
        final var maybeBookInfo = botState.getBookInfo(chatId, bookId);
        if (maybeBookInfo.isEmpty()) {
            final var reply = ProgressMessages.retrySearch();
            bot.sendReply(chatId, reply);
            return;
        }
        final var bookInfo = maybeBookInfo.get();
        final var bookInfoFiltered = new BookInfoFiltered(bookInfo);
        final var reply = ChooseBookFormat.show(bookInfoFiltered, bookId);
        bot.sendReply(chatId, reply);
    }
}
