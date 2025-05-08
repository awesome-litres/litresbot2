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

public class TelegramBot extends TelegramLongPollingBot {
    final static Logger logger = LogManager.getLogger(TelegramBot.class);

    public TelegramBot(DefaultBotOptions options, String botToken) {
        super(options, botToken);
    }

    protected static String getChatId(Update update) throws TelegramApiException {
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
        return String.valueOf(chatMessage.getChatId());
    }

    public void sendReply(Update update, String res) throws TelegramApiException {
        final var message = new SendMessage();
        message.setText(res);
        message.enableHtml(true);
        sendReply(update, message);
    }

    public void sendReply(Update update, SendMessage res) throws TelegramApiException {
        final var chatId = getChatId(update);
        res.setChatId(chatId);
        execute(res);
    }

    public void sendReply(Update update, SendMessageList res) throws TelegramApiException {
        final var chatId = getChatId(update);
        for (final var sm : res.getMessages()) {
            if (sm.getText() != null && sm.getText().length() > 0) {
                sm.setChatId(chatId);
                execute(sm);
            }
        }
    }

    public void sendFile(Update update, SendDocument res) throws TelegramApiException {
        final var chatId = getChatId(update);
        res.setChatId(chatId);
        execute(res);
    }

    public void sendBusy(Update update) throws TelegramApiException {
        final var chatId = getChatId(update);
        final var sca = new SendChatAction();
        sca.setChatId(chatId);
        sca.setAction(ActionType.UPLOADDOCUMENT);
        execute(sca);
    }

    @Override
    public String getBotUsername() {
        final var name = AppProperties.getStringProperty("botName");
        return name == null ? "litresbot" : name;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            TelegramBotCommands.commandReceived(this, update);
        } catch (TelegramApiException e) {
            logger.error("Error processing telegram command: " + e.getMessage(), e);
        }
    }
}
