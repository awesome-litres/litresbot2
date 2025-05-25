package litresbot.search_db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.sql.DataSource;

import org.apache.logging.log4j.Logger;

import litresbot.books.BookFileInfo;
import litresbot.books.BookInfo;
import litresbot.utils.StringUtils;

import org.apache.logging.log4j.LogManager;

public class SearchFlibustaDatabaseBook {
    final static Logger logger = LogManager.getLogger(SearchFlibustaDatabaseBook.class);

    public static DataSource db = DatabasePool.getDataSource();

    protected static List<BookInfo> processBooksFromResultSet(ResultSet results) throws SQLException {
        var bookIds = new ArrayList<Long>();
        var bookDict = new HashMap<Long, SearchBookInfo>();
        while (results.next()) {
            final var bookId = results.getLong("BookId");

            var authorNames = new ArrayList<String>();
            authorNames.add(results.getString("LastName"));
            authorNames.add(results.getString("FirstName"));
            authorNames.add(results.getString("MiddleName"));
            final var fullName = StringUtils.joinStrings(" ", authorNames);

            if (bookDict.containsKey(bookId)) {
                bookDict.get(bookId).authors.add(fullName);
                continue;
            }

            var book = new SearchBookInfo();
            book.id = bookId;
            book.titles = new ArrayList<>();
            final var title1 = results.getString("title");
            if (title1 != null && !title1.isEmpty()) {
                book.titles.add(title1);
            }
            final var title2 = results.getString("title1");
            if (title2 != null && !title2.isEmpty()) {
                book.titles.add(title2);
            }
            book.authors = new ArrayList<>();
            book.authors.add(fullName);

            final var language = results.getString("Lang");
            if (language != null && !language.isEmpty()) {
                book.language = language;
            } else {
                book.language = "ru";
            }

            final var fileType = results.getString("FileType");
            if (fileType != null && !fileType.isEmpty()) {
                book.fileType = fileType;
            } else {
                book.fileType = "txt";
            }

            final var annotation = results.getString("annotation");
            if (annotation != null && !annotation.isEmpty()) {
                book.annotation = annotation;
            } else {
                book.annotation = "";
            }

            bookIds.add(bookId);
            bookDict.put(bookId, book);
        }
        var bookList = new ArrayList<SearchBookInfo>();
        for (final var bookId : bookIds) {
            bookList.add(bookDict.get(bookId));
        }
        return SearchBookInfoAggregator.aggregateBooks(bookList);
    }

    public static List<BookInfo> searchByTitle(String title) throws SQLException {
        final var connection = db.getConnection();

        final var sql = "WITH annotations AS (" +
	          " SELECT BookId, MAX(nid) AS annotationId" +
	          " FROM library.libbannotations" +
            " GROUP BY BookId)" +
            " SELECT library.libbook.*, library.libavtorname.*, library.libbannotations.Body AS annotation FROM library.libbook" +
            " LEFT JOIN library.libavtor ON library.libbook.BookId = library.libavtor.BookId" +
            " LEFT JOIN library.libavtorname ON library.libavtor.AvtorId = library.libavtorname.AvtorId" +
            " LEFT JOIN annotations ON library.libbook.BookId = annotations.BookId" +
            " LEFT JOIN library.libbannotations ON annotations.annotationId = library.libbannotations.nid AND annotations.BookId = library.libbannotations.BookId" +
            " WHERE (library.libbook.title LIKE ? OR title1 LIKE ? OR keywords LIKE ?) AND Deleted <> 1" +
            " ORDER BY library.libbook.BookId DESC";
        final var preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, "%" + title + "%");
        preparedStatement.setString(2, "%" + title + "%");
        preparedStatement.setString(3, "%" + title + "%");
        final var resultSet = preparedStatement.executeQuery();
        return processBooksFromResultSet(resultSet);
    }

    public static List<BookInfo> searchByAuthor(String author) throws SQLException {
        final var connection = db.getConnection();
        final var sql = "WITH annotations AS (" +
	          " SELECT BookId, MAX(nid) AS annotationId" +
	          " FROM library.libbannotations" +
            " GROUP BY BookId)" +
            " SELECT library.libbook.*, library.libavtorname.*, library.libbannotations.Body AS annotation FROM library.libavtorname" +
            " JOIN library.libavtor ON library.libavtor.AvtorId = library.libavtorname.AvtorId" +
            " JOIN library.libbook ON library.libbook.BookId = library.libavtor.BookId" +
            " LEFT JOIN annotations ON library.libbook.BookId = annotations.BookId" +
            " LEFT JOIN library.libbannotations ON annotations.annotationId = library.libbannotations.nid AND annotations.BookId = library.libbannotations.BookId" +
            " WHERE (FirstName LIKE ? OR MiddleName LIKE ? OR LastName LIKE ? OR NickName LIKE ?) AND Deleted <> 1" +
            " ORDER BY library.libavtor.AvtorId DESC";
        final var preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, "%" + author + "%");
        preparedStatement.setString(2, "%" + author + "%");
        preparedStatement.setString(3, "%" + author + "%");
        preparedStatement.setString(4, "%" + author + "%");
        final var resultSet = preparedStatement.executeQuery();
        return processBooksFromResultSet(resultSet);
    }

    // check for duplicate files in the books
    protected static List<BookFileInfo> processBook(BookInfo book, HashSet<Long> bookIds) {
        var bookFiles = new ArrayList<BookFileInfo>();
        for (var file : book.files) {
            if (bookIds.contains(file.id)) {
                continue;
            }
            bookIds.add(file.id);
            bookFiles.add(file);
        }
        return bookFiles;
    }

    // perform title search first
    // perform author search next
    // author search results are combined by author
    // duplicate books are removed
    public static List<BookInfo> searchCombined(String query) throws SQLException {
        var bookIds = new HashSet<Long>();
        final var titleResults = searchByTitle(query);
        final var authorResults = searchByAuthor(query);
        final var combinedResults = new ArrayList<BookInfo>();
        combinedResults.addAll(titleResults);
        combinedResults.addAll(authorResults);
        var bookList = new ArrayList<BookInfo>();
        for (var book : combinedResults) {
            final var bookFiles = processBook(book, bookIds);
            if (bookFiles.isEmpty()) {
                continue;
            }
            book.files = bookFiles;
            bookList.add(book);
        }
        return bookList;
    }
}
