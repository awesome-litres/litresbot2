package litresbot.telegram.commands;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import litresbot.telegram.TelegramBot;
import litresbot.telegram.view.TelegramView;

public class StartCommand implements TelegramCommandInterface {
    public static final String command = "/start";
    protected final TelegramBot bot;

    StartCommand(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public Boolean isCommand(String cmd) {
        return cmd.equalsIgnoreCase(command);
    }

    @Override
    public void execute(Long chatId, String _message) throws TelegramApiException {
        bot.sendReply(chatId, TelegramView.welcomeScreen());
    }
}
