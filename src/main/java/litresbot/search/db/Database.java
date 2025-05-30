package litresbot.search.db;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import litresbot.database.DatabasePool;

public class Database {
    final static Logger logger = LogManager.getLogger(Database.class);

    public static DataSource db = DatabasePool.getDataSource();

    public static Database create() throws SQLException {
        final var connection = db.getConnection();

        {
            final var sql = "CREATE DATABASE IF NOT EXISTS library";
            final var stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        }
        {
            final var sql = "CREATE TABLE IF NOT EXISTS library.libbannotations ("
                    + "`BookId` int(10) unsigned NOT NULL,"
                    + "`nid` int(10) unsigned NOT NULL,"
                    + "`Title` varchar(255) CHARACTER SET utf8 NOT NULL,"
                    + "`Body` longtext CHARACTER SET utf8,"
                    + "KEY `idx_libbannotations_BookId` (`BookId`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

            final var stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        }
        {
            final var sql = "CREATE TABLE IF NOT EXISTS library.libavtor ("
                    + "`BookId` int(10) unsigned NOT NULL DEFAULT '0',"
                    + "`AvtorId` int(10) unsigned NOT NULL DEFAULT '0',"
                    + "`Pos` tinyint(4) unsigned NOT NULL DEFAULT '0',"
                    + "PRIMARY KEY (`BookId`,`AvtorId`),"
                    + "KEY `iav` (`AvtorId`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

            final var stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        }
        {
            final var sql = "CREATE TABLE IF NOT EXISTS library.libavtorname ("
                    + "`AvtorId` int(10) unsigned NOT NULL AUTO_INCREMENT,"
                    + "`FirstName` varchar(99) CHARACTER SET utf8 NOT NULL DEFAULT '',"
                    + "`MiddleName` varchar(99) CHARACTER SET utf8 NOT NULL DEFAULT '',"
                    + "`LastName` varchar(99) CHARACTER SET utf8 NOT NULL DEFAULT '',"
                    + "`NickName` varchar(33) CHARACTER SET utf8 NOT NULL DEFAULT '',"
                    + "`uid` int(11) NOT NULL DEFAULT '0',"
                    + "`Email` varchar(255) CHARACTER SET utf8 NOT NULL,"
                    + "`Homepage` varchar(255) CHARACTER SET utf8 NOT NULL,"
                    + "`Gender` char(1) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',"
                    + "`MasterId` int(10) NOT NULL DEFAULT '0',"
                    + "PRIMARY KEY (`AvtorId`),"
                    + "KEY `FirstName` (`FirstName`(20)),"
                    + "KEY `LastName` (`LastName`(20)),"
                    + "KEY `email` (`Email`),"
                    + "KEY `Homepage` (`Homepage`),"
                    + "KEY `uid` (`uid`),"
                    + "KEY `MasterId` (`MasterId`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

            final var stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        }
        {
            final var sql = "CREATE TABLE IF NOT EXISTS library.libbook ("
                    + "`BookId` int(10) unsigned NOT NULL AUTO_INCREMENT,"
                    + "`FileSize` int(10) unsigned NOT NULL DEFAULT '0',"
                    + "`Time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "`Title` varchar(254) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',"
                    + "`Title1` varchar(254) CHARACTER SET utf8 NOT NULL,"
                    + "`Lang` char(3) CHARACTER SET utf8 NOT NULL DEFAULT 'ru',"
                    + "`LangEx` smallint(6) unsigned NOT NULL DEFAULT '0',"
                    + "`SrcLang` char(3) CHARACTER SET utf8 NOT NULL DEFAULT '',"
                    + "`FileType` char(4) CHARACTER SET utf8 NOT NULL,"
                    + "`Encoding` varchar(32) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',"
                    + "`Year` smallint(6) NOT NULL DEFAULT '0',"
                    + "`Deleted` char(1) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',"
                    + "`Ver` varchar(8) CHARACTER SET utf8 NOT NULL DEFAULT '',"
                    + "`FileAuthor` varchar(64) CHARACTER SET utf8 NOT NULL,"
                    + "`N` int(10) unsigned NOT NULL DEFAULT '0',"
                    + "`keywords` varchar(255) CHARACTER SET utf8 NOT NULL,"
                    + "`md5` binary(32) NOT NULL,"
                    + "`Modified` timestamp NOT NULL DEFAULT '2009-11-29 05:00:00',"
                    + "`pmd5` char(32) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',"
                    + "`InfoCode` tinyint(3) unsigned NOT NULL DEFAULT '0',"
                    + "`Pages` int(10) unsigned NOT NULL DEFAULT '0',"
                    + "`Chars` int(10) unsigned NOT NULL DEFAULT '0',"
                    + "PRIMARY KEY (`BookId`),"
                    + "UNIQUE KEY `md5` (`md5`),"
                    + "UNIQUE KEY `BookDel` (`Deleted`,`BookId`),"
                    + "KEY `Title` (`Title`),"
                    + "KEY `Year` (`Year`),"
                    + "KEY `Deleted` (`Deleted`),"
                    + "KEY `FileType` (`FileType`),"
                    + "KEY `Lang` (`Lang`),"
                    + "KEY `FileSize` (`FileSize`),"
                    + "KEY `FileAuthor` (`FileAuthor`),"
                    + "KEY `N` (`N`),"
                    + "KEY `Title1` (`Title1`),"
                    + "KEY `FileTypeDel` (`Deleted`,`FileType`),"
                    + "KEY `LangDel` (`Deleted`,`Lang`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

            final var stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        }
        {
            final var sql = "CREATE TABLE IF NOT EXISTS library.libfilename ("
                    + "`BookId` int(11) NOT NULL,"
                    + "`FileName` varchar(255) CHARACTER SET utf8 NOT NULL,"
                    + "PRIMARY KEY (`BookId`),"
                    + "UNIQUE KEY `FileName` (`FileName`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

            final var stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        }
        return new Database();
    }
}
