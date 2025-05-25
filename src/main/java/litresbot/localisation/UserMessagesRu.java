package litresbot.localisation;

import java.util.HashMap;
import java.util.Map;

public class UserMessagesRu implements UserMessages {
    public final static Map<String, String> translation = new HashMap<String, String>();
    {
        translation.put(UserMessagesEn.welcomeScreen, "Введите название книги для поиска.");
        translation.put(UserMessagesEn.helpCommands, "Список доступных команд:");
        translation.put(UserMessagesEn.helpStart, "начало работы с ботом");
        translation.put(UserMessagesEn.helpHelp, "справка по работе с ботом");
        translation.put(UserMessagesEn.helpBook, "поиск по названию книги");
        translation.put(UserMessagesEn.errorUnknown, "Неизвестная ошибка");
        translation.put(UserMessagesEn.errorWrongBookId, "Неверный ID книги");
        translation.put(UserMessagesEn.errorCouldNotDownloadFile, "Не получилось загрузить файл");
        translation.put(UserMessagesEn.errorBadCommand, "Неверная команда");
        translation.put(UserMessagesEn.errorSearchNotFound, "Ничего не найдено");
        translation.put(UserMessagesEn.searchInProgress, "Поиск книги...");
        translation.put(UserMessagesEn.searchFoundTotal, "Найдено: ");
        translation.put(UserMessagesEn.bookText, "книга");
        translation.put(UserMessagesEn.searchGoto, "Загрузить: ");
        translation.put(UserMessagesEn.searchDownload, "Загрузить");
        translation.put(UserMessagesEn.downloadInProgress, "Загружаю книгу...");
        translation.put(UserMessagesEn.downloadFinished, "Книга загружена");
        translation.put(UserMessagesEn.searchRead, "Читать");
        translation.put(UserMessagesEn.previousText, "Назад");
        translation.put(UserMessagesEn.nextText, "Дальше");
        translation.put(UserMessagesEn.beginText, "Начало");
        translation.put(UserMessagesEn.endText, "Конец");
        translation.put(UserMessagesEn.annotationEmpty, "Нет аннотации");
        translation.put(UserMessagesEn.pageNumberText, "Страница: ");
    }

    public String language() {
        return "ru";
    }

    public String get(String in) {
        return translation.get(in);
    }
}
