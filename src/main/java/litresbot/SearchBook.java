package litresbot;

import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.Logger;

import litresbot.books.BookInfo;
import litresbot.search_db.SearchFlibustaDatabaseBook;

import org.apache.logging.log4j.LogManager;

public class SearchBook {
    final static Logger logger = LogManager.getLogger(SearchBook.class);

    public static List<BookInfo> search(String query) throws SQLException {
        return SearchFlibustaDatabaseBook.searchCombined(query);
    }
}
