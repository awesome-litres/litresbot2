package litresbot.telegram.commands;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface TelegramCommandInterface {
    // returns true if the command is recognized, i.e. HelpCommand returns true for "/help"
    Boolean isCommand(String cmd);

    // executes the command from the Telegram update message
    void execute(Long chatId, Message message) throws TelegramApiException;
}
