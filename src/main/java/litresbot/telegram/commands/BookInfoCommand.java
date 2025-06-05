package litresbot.telegram.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import litresbot.books.BookInfoFiltered;
import litresbot.telegram.TelegramBot;
import litresbot.telegram.TelegramBotState;
import litresbot.telegram.view.ChooseBookAction;
import litresbot.telegram.view.ProgressMessages;

public class BookInfoCommand implements TelegramCommandInterface {
    final static Logger logger = LogManager.getLogger(BookInfoCommand.class);
    public static final String command = "/bookinfo";
    public static final String shortCommand = "/b";
    protected final TelegramBot bot;
    protected final TelegramBotState botState;

    BookInfoCommand(TelegramBot bot, TelegramBotState botState) {
        this.bot = bot;
        this.botState = botState;
    }

    @Override
    public Boolean isCommand(String cmd) {
        return cmd.startsWith(command) || cmd.startsWith(shortCommand + "_");
    }

    @Override
    public void execute(long chatId, String message) throws TelegramApiException {
        var argument = "";
        if (message.startsWith(command)) {
            // remove the command and trailing space from the string
            argument = message.substring(command.length() + 1);
        } else {
            // remove the command and trailing underscore from the string
            argument = message.substring(shortCommand.length() + 1);
        }
        final var bookId = Long.parseLong(argument);
        bookInfo(chatId, bookId);
    }

    protected void bookInfo(long chatId, long bookId) throws TelegramApiException {
        final var maybeBookInfo = botState.getBookInfo(chatId, bookId);
        if (maybeBookInfo.isEmpty()) {
            final var reply = ProgressMessages.retrySearch();
            bot.sendReply(chatId, reply);
            return;
        }
        final var bookInfo = maybeBookInfo.get();

        var hasFb2Format = false;
        for(final var f: bookInfo.files) {
            if (f.fileType.startsWith("fb2")) {
                hasFb2Format = true;
                break;
            }
        }

        final var bookInfoFiltered = new BookInfoFiltered(bookInfo);
        final var reply = ChooseBookAction.show(bookInfoFiltered, bookId, hasFb2Format);
        bot.sendReply(chatId, reply);
    }
}
