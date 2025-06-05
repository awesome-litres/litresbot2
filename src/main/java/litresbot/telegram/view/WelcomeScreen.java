package litresbot.telegram.view;

import litresbot.Application;
import litresbot.localisation.UserMessagesEn;

public class WelcomeScreen {
    public static String show() {
        return Application.userMessages.get(UserMessagesEn.welcomeScreen);
    }
}
