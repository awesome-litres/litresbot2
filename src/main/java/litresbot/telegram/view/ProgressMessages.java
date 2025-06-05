package litresbot.telegram.view;

import litresbot.Application;
import litresbot.localisation.UserMessagesEn;
import litresbot.telegram.SendMessageList;

public class ProgressMessages {
    public static SendMessageList bookInfoNotFound() {
        return SendMessageList.fromText(Application.userMessages.get(UserMessagesEn.errorSearchNotFound));
    }

    public static SendMessageList bookCouldNotDownload() {
        return SendMessageList.fromText(Application.userMessages.get(UserMessagesEn.errorCouldNotDownloadFile));
    }

    public static SendMessageList searchInProgress() {
        return SendMessageList.fromText(Application.userMessages.get(UserMessagesEn.searchInProgress));
    }

    public static SendMessageList retrySearch() {
        return SendMessageList.fromText(Application.userMessages.get(UserMessagesEn.errorUnknownRetry));
    }

    public static SendMessageList downloadInProgress() {
        return SendMessageList.fromText(Application.userMessages.get(UserMessagesEn.downloadInProgress));
    }

    public static SendMessageList downloadFinished() {
        return SendMessageList.fromText(Application.userMessages.get(UserMessagesEn.downloadFinished));
    }
}
