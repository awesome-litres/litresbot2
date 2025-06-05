package litresbot.telegram;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import litresbot.books.BookInfo;
//import litresbot.telegram.db.Database;

public class TelegramBotState {
    //protected final Database db;
    // non persistent, search results
    protected HashMap<Long, List<BookInfo>> searchState = new HashMap<>();

    public TelegramBotState() throws SQLException {
        //db = Database.create();
    }

    public void newSearch(long chatId, String searchQuery, List<BookInfo> searchResults) throws SQLException {
        searchState.put(chatId, searchResults);
        //db.setSearchQuery(chatId, searchQuery);
    }

    /*public String getSearchQuery(long chatId) throws SQLException {
        return db.getSearchQuery(chatId);
    }*/

    public List<BookInfo> getSearchResults(long chatId) {
        if (searchState.containsKey(chatId)) {
            return searchState.get(chatId);
        }
        return new ArrayList<>();
    }

    public Optional<BookInfo> getBookInfo(long chatId, long bookId) {
        if (!searchState.containsKey(chatId)) {
            return Optional.empty();
        }

        final var books = searchState.get(chatId);
        if (bookId == 0 || bookId > books.size()) {
            return Optional.empty();
        }
        return Optional.of(books.get((int) bookId - 1)); // bookId is 1-based
    }
}
