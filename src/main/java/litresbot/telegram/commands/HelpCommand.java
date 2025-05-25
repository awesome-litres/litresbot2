package litresbot.telegram.commands;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import litresbot.telegram.TelegramBot;
import litresbot.telegram.view.TelegramView;

public class HelpCommand implements TelegramCommandInterface {
    public static final String command = "/help";
    protected final TelegramBot bot;

    HelpCommand(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public Boolean isCommand(String cmd) {
        return cmd.equalsIgnoreCase(command);
    }

    @Override
    public void execute(Long chatId, Message message) throws TelegramApiException {
        bot.sendReply(chatId, TelegramView.helpScreen());
    }
}
