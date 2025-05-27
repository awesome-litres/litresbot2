package litresbot.telegram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import litresbot.books.BookInfo;

public class TelegramBotState {
    // key - chatId
    // value - UserState
    protected HashMap<Long, UserState> state = new HashMap<>();
    // non persistent, search results
    protected HashMap<Long, List<BookInfo>> searchState = new HashMap<>();

    public void newSearch(long chatId, String searchQuery, List<BookInfo> searchResults) {
        searchState.put(chatId, searchResults);
        var userState = new UserState();
        if (state.containsKey(chatId)) {
            userState = state.get(chatId);
        }
        userState.searchQuery = searchQuery;
        // TODO: save to DB
        state.put(chatId, userState);
    }

    public String getSearch(long chatId) {
        if (state.containsKey(chatId)) {
            return state.get(chatId).searchQuery;
        }
        return "";
    }

    public List<BookInfo> getSearchResults(long chatId) {
        if (searchState.containsKey(chatId)) {
            return searchState.get(chatId);
        }
        return new ArrayList<>();
    }
}
