package litresbot.telegram.commands;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface TelegramCommandInterface {
    // returns true if the command is recognized, i.e. HelpCommand returns true for "/help"
    Boolean isCommand(String cmd);

    // executes the command
    void execute(long chatId, String command) throws TelegramApiException;
}
