package litresbot.telegram.commands;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import litresbot.Application;
import litresbot.books.BookDownloader;
import litresbot.books.BookFileInfo;
import litresbot.localisation.UserMessagesEn;
import litresbot.telegram.TelegramBot;
import litresbot.telegram.TelegramBotState;
import litresbot.telegram.view.ProgressMessages;

public class BookDownloadCommand implements TelegramCommandInterface {
    final static Logger logger = LogManager.getLogger(BookDownloadCommand.class);
    public static final String command = "/download";
    protected final TelegramBot bot;
    protected final TelegramBotState botState;
    protected final BookDownloader downloader;

    BookDownloadCommand(TelegramBot bot, TelegramBotState botState, BookDownloader downloader) {
        this.bot = bot;
        this.botState = botState;
        this.downloader = downloader;
    }

    @Override
    public Boolean isCommand(String cmd) {
        return cmd.startsWith(command);
    }

    @Override
    public void execute(long chatId, String message) throws TelegramApiException {
        final var argument = message.substring(command.length() + 1);
        final var arguments = argument.split(" ");
        if (arguments.length != 2) {
            logger.error("Invalid command format: " + message);
            bot.sendReply(chatId, Application.userMessages.get(UserMessagesEn.errorBadCommand));
            return;
        }
        final var format = arguments[0];
        final var bookId = Long.parseLong(arguments[1]);
        bookDownload(chatId, format, bookId);
    }

    protected void bookDownload(long chatId, String format, long bookId) throws TelegramApiException {
        final var maybeBookInfo = botState.getBookInfo(chatId, bookId);
        if (maybeBookInfo.isEmpty()) {
            final var reply = ProgressMessages.retrySearch();
            bot.sendReply(chatId, reply);
            return;
        }
        final var bookInfo = maybeBookInfo.get();
        BookFileInfo bookFileInfo = null;
        for (final var f: bookInfo.files) {
            if (f.fileType.equals(format)) {
                bookFileInfo = f;
                break;
            }
        }
        if (bookFileInfo == null) {
            logger.error("Book format not found: {} for book {}", format, bookId);
            bot.sendReply(chatId, Application.userMessages.get(UserMessagesEn.errorBadCommand));
            return;
        }
        bot.sendReply(chatId, ProgressMessages.downloadInProgress());
        String bookFile = "";
        try {
            bookFile = downloader.download(bookFileInfo);
        } catch (SQLException e) {
            logger.error("Error downloading book: " + e.getMessage(), e);
            bot.sendReply(chatId, Application.userMessages.get(UserMessagesEn.errorUnknown));
            return;
        } catch (IOException e) {
            logger.error("Error downloading book: " + e.getMessage(), e);
            bot.sendReply(chatId, ProgressMessages.bookCouldNotDownload());
            return;
        }
        bot.sendReply(chatId, ProgressMessages.downloadFinished());

        final var fileMedia = new InputFile(new File(bookFile));
        var doc = new SendDocument();
        doc.setDocument(fileMedia);
        bot.sendFile(chatId, doc);
    }
}
