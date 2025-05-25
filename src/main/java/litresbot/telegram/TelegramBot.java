package litresbot.telegram;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import litresbot.AppProperties;
import litresbot.telegram.commands.TelegramBotCommands;

public class TelegramBot extends TelegramLongPollingBot {
    final static Logger logger = LogManager.getLogger(TelegramBot.class);
    protected TelegramBotCommands telegramBotCommands;

    public TelegramBot(DefaultBotOptions options, String botToken) {
        super(options, botToken);
    }

    public void registerCommands(TelegramBotCommands telegramBotCommands) {
        this.telegramBotCommands = telegramBotCommands;
    }

    public static Long getChatId(Update update) throws TelegramApiException {
        var chatMessage = update.getMessage();

        if (update.hasCallbackQuery()) {
            final var updateMessage = update.getCallbackQuery().getMessage();
            if (updateMessage instanceof Message) {
                chatMessage = (Message) updateMessage;
            }
        }

        if (chatMessage == null) {
            throw new TelegramApiException("Chat message is unavailable.");
        }
        return chatMessage.getChatId();
    }

    public void sendReply(Long chatId, String res) throws TelegramApiException {
        final var message = new SendMessage();
        message.setText(res);
        message.enableHtml(true);
        sendReply(chatId, message);
    }

    public void sendReply(Long chatId, SendMessage res) throws TelegramApiException {
        res.setChatId(chatId);
        execute(res);
    }

    public void sendReply(Long chatId, SendMessageList res) throws TelegramApiException {
        for (final var sm : res.getMessages()) {
            if (sm.getText() != null && sm.getText().length() > 0) {
                sm.setChatId(chatId);
                execute(sm);
            }
        }
    }

    public void sendFile(Long chatId, SendDocument res) throws TelegramApiException {
        res.setChatId(chatId);
        execute(res);
    }

    public void sendBusy(Long chatId) throws TelegramApiException {
        final var sca = new SendChatAction();
        sca.setChatId(chatId);
        sca.setAction(ActionType.TYPING);
        execute(sca);
    }

    @Override
    public String getBotUsername() {
        final var name = AppProperties.getStringProperty("botName");
        return name == null ? "litresbot" : name;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // TODO: create new thread when processing the update
        try {
            telegramBotCommands.commandReceived(update);
        } catch (TelegramApiException e) {
            logger.error("Error processing telegram command: " + e.getMessage(), e);
        }
    }
}
