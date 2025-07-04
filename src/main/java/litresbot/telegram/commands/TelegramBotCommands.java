package litresbot.telegram.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import litresbot.Application;
import litresbot.books.BookDownloader;
import litresbot.localisation.UserMessagesEn;
import litresbot.telegram.TelegramBot;
import litresbot.telegram.TelegramBotState;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

public class TelegramBotCommands {
    final static Logger logger = LogManager.getLogger(TelegramBotCommands.class);

    protected final TelegramBot bot;
    protected final List<TelegramCommandInterface> commands = new ArrayList<>();
    protected final TelegramBotState botState;
    protected final BookDownloader downloader;

    public TelegramBotCommands(TelegramBot bot, TelegramBotState botState, BookDownloader downloader) {
        this.bot = bot;
        this.botState = botState;
        this.downloader = downloader;
        commands.add(new HelpCommand(bot));
        commands.add(new StartCommand(bot));
        commands.add(new BookSearchCommand(bot, botState));
        commands.add(new BookSearchNextCommand(bot, botState));
        commands.add(new BookInfoCommand(bot, botState));
        commands.add(new BookFormatCommand(bot, botState));
        commands.add(new BookDownloadCommand(bot, botState, downloader));
    }

    public void commandReceived(Update update) throws TelegramApiException {
        final var chatId = TelegramBot.getChatId(update);

        var cmd = "";
        var userName = "";
        var languageCode = "en";
        if (update.hasMessage()) {
            final var chatMessage = update.getMessage();
            cmd = chatMessage.getText();
            userName = chatMessage.getFrom().getUserName();
            languageCode = chatMessage.getFrom().getLanguageCode();
        } else if (update.hasCallbackQuery()) {
            final var updateMessage = update.getCallbackQuery().getMessage();
            if (updateMessage instanceof Message) {
                final var chatMessage = (Message) updateMessage;
                userName = chatMessage.getFrom().getUserName();
                languageCode = chatMessage.getFrom().getLanguageCode();
            }
            cmd = update.getCallbackQuery().getData();
        }

        if (cmd.isEmpty()) {
            bot.sendReply(chatId, Application.userMessages.get(UserMessagesEn.errorUnknown));
            return;
        }

        // NOP command
        if (cmd.equals("/")) {
            return;
        }

        // String escaping is not used because log4shell attack is prevented by
        // recent log4j versions and message lookups are disabled in configuration
        logger.info("Command received from {}: {}", userName, cmd);
        logger.info("User has language: {}", languageCode);
        cmd = cmd.toLowerCase();

        for (final var command : commands) {
            if (command.isCommand(cmd)) {
                bot.sendBusy(chatId);
                try {
                    command.execute(chatId, cmd);
                } catch (NumberFormatException _e) {
                    bot.sendReply(chatId, Application.userMessages.get(UserMessagesEn.errorBadCommand));
                } catch (IndexOutOfBoundsException _e) {
                    bot.sendReply(chatId, Application.userMessages.get(UserMessagesEn.errorBadCommand));
                } catch (NoSuchKeyException _e) {
                    bot.sendReply(chatId, Application.userMessages.get(UserMessagesEn.errorBadCommand));
                } catch (Exception e) {
                    logger.error("Error executing command: " + e.getMessage(), e);
                }
                return;
            }
        }

        // If no command was found, send an error message
        if (cmd.startsWith("/")) {
            bot.sendReply(chatId, Application.userMessages.get(UserMessagesEn.errorBadCommand));
            return;
        }

        final var defaultCommand = new BookSearchCommand(bot, botState);
        bot.sendBusy(chatId);
        defaultCommand.execute(chatId, cmd);
    }
}
