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
import litresbot.telegram.TelegramBotState;

public class TelegramBotCommands {
    final static Logger logger = LogManager.getLogger(TelegramBotCommands.class);

    protected final TelegramBot bot;
    protected final List<TelegramCommandInterface> commands = new ArrayList<>();
    protected final TelegramBotState botState = new TelegramBotState();

    public TelegramBotCommands(TelegramBot bot) {
        this.bot = bot;
        commands.add(new HelpCommand(bot));
        commands.add(new StartCommand(bot));
        commands.add(new BookSearchCommand(bot, botState));
        commands.add(new BookSearchNextCommand(bot, botState));
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

        logger.info("Command received from {}: {}", StringEscapeUtils.escapeJava(userName), StringEscapeUtils.escapeJava(cmd));
        logger.info("User has language: {}", StringEscapeUtils.escapeJava(languageCode));
        cmd = cmd.toLowerCase();

        for (final var command : commands) {
            if (command.isCommand(cmd)) {
                bot.sendBusy(chatId);
                command.execute(chatId, cmd);
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
