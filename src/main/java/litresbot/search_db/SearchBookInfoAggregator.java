package litresbot.search_db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.Logger;

import litresbot.books.BookFileInfo;
import litresbot.books.BookInfo;
import litresbot.utils.StringUtils;

import org.apache.logging.log4j.LogManager;

public class SearchBookInfoAggregator {
    final static Logger logger = LogManager.getLogger(SearchBookInfoAggregator.class);

    // Tries to aggregate books by their title and author
    // It would be nice to also aggragate by language, i.e. books in different languages are considered the same book
    // but it requires some way to identify different variants of the same book
    public static List<BookInfo> aggregateBooks(List<SearchBookInfo> searchBooks) {
        var books = new ArrayList<BookInfo>();
        var bookIndexes = new HashMap<String, Integer>();

        for (int bookCurrentIndex = 0; bookCurrentIndex < searchBooks.size(); bookCurrentIndex++) {
            final var searchBook = searchBooks.get(bookCurrentIndex);
            final var bookKey = StringUtils.joinStrings(" ", searchBook.titles) + "_" + StringUtils.joinStrings(" ", searchBook.authors);
            final var bookIndex = bookIndexes.get(bookKey);

            BookInfo book;
            if (bookIndex == null) {
                book = new BookInfo();
                book.files = new ArrayList<>();
                book.titles = searchBook.titles;
                book.authors = searchBook.authors;
                book.annotation = searchBook.annotation;
                bookIndexes.put(bookKey, books.size());
            } else {
                book = books.get(bookIndex);
            }

            var fileInfo = new BookFileInfo();
            fileInfo.id = searchBook.id;
            fileInfo.language = searchBook.language;
            fileInfo.fileType = searchBook.fileType;
            book.files.add(fileInfo);

            if (bookIndex == null) {
                books.add(book);
            }
        }

        return books;
    }
}
