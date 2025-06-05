package litresbot.telegram.view;

import litresbot.Application;
import litresbot.localisation.UserMessagesEn;

public class HelpScreen {
    public static String show() {
        return Application.userMessages.get(UserMessagesEn.helpCommands) + "\n" +
                "/start - " + Application.userMessages.get(UserMessagesEn.helpStart) + "\n" +
                "/help - " + Application.userMessages.get(UserMessagesEn.helpHelp) + "\n" +
                "/book - " + Application.userMessages.get(UserMessagesEn.helpBook);
    }
}
