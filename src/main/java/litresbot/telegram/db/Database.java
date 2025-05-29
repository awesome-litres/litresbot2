package litresbot.telegram.db;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Database {
    final static Logger logger = LogManager.getLogger(Database.class);

    public static DataSource db = DatabasePool.getDataSource();

    public static Database create() throws SQLException {
        final var connection = db.getConnection();

        {
            final var sql = "CREATE DATABASE IF NOT EXISTS telegram";
            final var stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        }
        {
            final var sql = "CREATE TABLE IF NOT EXISTS telegram.searches ("
                    + "chatId BIGINT PRIMARY KEY, "
                    + "query VARCHAR(255) NOT NULL )";

            final var stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        }
        {
            final var sql = "CREATE TABLE IF NOT EXISTS telegram.read_books ("
                    + "chatId BIGINT PRIMARY KEY, "
                    + "bookId BIGINT NOT NULL )";

            final var stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        }
        {
            final var sql = "CREATE TABLE IF NOT EXISTS telegram.book_files ("
                    + "bookId BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "bookFile VARCHAR(255) NOT NULL )";

            final var stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        }
        return new Database();
    }

    public String getSearchQuery(Long chatId) throws SQLException {
        final var connection = db.getConnection();
        final var sql = "SELECT query FROM telegram.searches WHERE chatId = ?";
        final var preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setLong(1, chatId);
        final var resultSet = preparedStatement.executeQuery();
        if (!resultSet.next()) {
            return "";
        }
        return resultSet.getString("query");
    }

    public void setSearchQuery(Long chatId, String query) throws SQLException {
        final var connection = db.getConnection();
        final var sql = "INSERT INTO telegram.searches (chatId, query) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE query = ?";
        final var preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setLong(1, chatId);
        preparedStatement.setString(2, query);
        preparedStatement.setString(3, query);
        preparedStatement.executeUpdate();
    }
}
