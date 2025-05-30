package litresbot.database;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabasePool {
    public static final String DB_URL = "jdbc:mysql://localhost";
    public static final int POOL_SIZE = 20;
    // minimum number of connections in the pool when idle
    public static final int POOL_SIZE_MIN = 2;

    public static DataSource getDataSource() {
        var config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setUsername("root");
        config.setPassword("root");
        config.setConnectionTestQuery("SELECT 1");
        config.setMaximumPoolSize(POOL_SIZE);
        config.setMinimumIdle(POOL_SIZE_MIN);
        return new HikariDataSource(config);
    }
}
