package litresbot.telegram.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import litresbot.Application;
import litresbot.localisation.UserMessagesEn;
import litresbot.telegram.TelegramBot;

public class TelegramBotCommands {
    final static Logger logger = LogManager.getLogger(TelegramBotCommands.class);

    protected final TelegramBot bot;
    protected final List<TelegramCommandInterface> commands = new ArrayList<>();

    public TelegramBotCommands(TelegramBot bot) {
        this.bot = bot;
        commands.add(new HelpCommand(bot));
        commands.add(new StartCommand(bot));
        commands.add(new BookSearchCommand(bot));
        commands.add(new BookSearchNextCommand(bot));
    }

    public void commandReceived(Update update) throws TelegramApiException {
        final var chatId = TelegramBot.getChatId(update);

        Message chatMessage = null;
        if (update.hasMessage()) {
            chatMessage = update.getMessage();
        } else if (update.hasCallbackQuery()) {
            final var updateMessage = update.getMessage();
            if (updateMessage instanceof Message) {
                chatMessage = (Message) updateMessage;
            }
        }

        if (chatMessage == null) {
            bot.sendReply(chatId, Application.userMessages.get(UserMessagesEn.errorUnknown));
            return;
        }

        if (!chatMessage.hasText()) {
            bot.sendReply(chatId, Application.userMessages.get(UserMessagesEn.errorUnknown));
            return;
        }

        final var cmd = chatMessage.getText();
        final var userName = chatMessage.getFrom().getUserName();
        final var languageCode = chatMessage.getFrom().getLanguageCode();

        if (cmd == null) {
            bot.sendReply(chatId, Application.userMessages.get(UserMessagesEn.errorUnknown));
            return;
        }

        logger.info("Command received from {}: {}", StringEscapeUtils.escapeJava(userName), StringEscapeUtils.escapeJava(cmd));
        logger.info("User has language: {}", StringEscapeUtils.escapeJava(languageCode));
        final var normalCmd = cmd.toLowerCase();

        for (final var command : commands) {
            if (command.isCommand(normalCmd)) {
                bot.sendBusy(chatId);
                command.execute(chatId, chatMessage);
                return;
            }
        }

        // If no command was found, send an error message
        if (normalCmd.startsWith("/")) {
            bot.sendReply(chatId, Application.userMessages.get(UserMessagesEn.errorBadCommand));
            return;
        }

        final var defaultCommand = new BookSearchCommand(bot);
        bot.sendBusy(chatId);
        defaultCommand.execute(chatId, chatMessage);
    }
}
