package litresbot;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import litresbot.books.BookDownloader;
import litresbot.download.FlibustaS3BookDownloader;
import litresbot.download.S3BookClient;
import litresbot.localisation.UserMessages;
import litresbot.localisation.UserMessagesRu;
import litresbot.telegram.TelegramBot;
import litresbot.telegram.TelegramBotState;
import litresbot.telegram.commands.TelegramBotCommands;

public class Application {
    public static final String packageName = Application.class.getPackage().getName();

    public static Boolean terminated = false;

    // set the locale depending on the bot type
    public static UserMessages userMessages = new UserMessagesRu();

    final static Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        final var version = AppProperties.versionProperties.getProperty("version");
        logger.info(packageName + " " + ((version == null) ? "(no version)" : version));

        terminated = false;

        final var botOptions = new DefaultBotOptions();
        final var botToken = AppProperties.getStringProperty("botToken");
        if (botToken == null) {
            logger.error("botToken is not defined. Unable to register bot.");
            return;
        }

        var useProxy = AppProperties.getBooleanProperty("useProxy");
        if (useProxy == null) {
            useProxy = false;
        }

        if (useProxy) {
            final var host = AppProperties.getStringProperty("proxyHost");
            final var port = AppProperties.getIntProperty("proxyPort");
            final var proxyType = AppProperties.getStringProperty("proxyType");

            if (host == null) {
                logger.error("proxyHost is not defined. Define it to proxy host or switch off the proxy.");
                return;
            }

            if (port == null) {
                logger.error("proxyPort is not defined. Define it to proxy port or switch off the proxy.");
                return;
            }

            if (proxyType == null) {
                logger.error("proxyType is not defined. Define it to proxy type (eg SOCKS5) or switch off the proxy.");
                return;
            }

            botOptions.setProxyHost(host);
            botOptions.setProxyPort(port);
            // default SOCKS5
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);

            if (proxyType.compareToIgnoreCase("http") == 0) {
                botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);
            }

            if (proxyType.compareToIgnoreCase("socks4") == 0) {
                botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS4);
            }
        }

        var flibustaDownloadPath = AppProperties.getStringProperty("flibustaDownloadPath");
        if (flibustaDownloadPath == null) {
            flibustaDownloadPath = "./tmp";
        }

        try {
            litresbot.search.db.Database.create();
        } catch (SQLException e) {
            logger.error("Could not connect to books database", e);
            return;
        }

        final TelegramBotState botState;
        try {
            botState = new TelegramBotState();
        } catch (SQLException e) {
            logger.error("Could not restore Telegram state", e);
            return;
        }

        final var s3Client = new S3BookClient("https://play.min.io", "Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG", "", "test", "upload");
        final var s3Downloader = new FlibustaS3BookDownloader(s3Client, flibustaDownloadPath);
        final var bookDownloader = new BookDownloader(s3Downloader);

        try {
            final var telegram = new TelegramBotsApi(DefaultBotSession.class);
            final var bot = new TelegramBot(botOptions, botToken);
            bot.registerCommands(new TelegramBotCommands(bot, botState, bookDownloader));
            telegram.registerBot(bot);
            logger.info("Bot successfully registered");
        } catch (TelegramApiException e) {
            logger.error("Could not register a bot", e);
            return;
        }

        try {
            while (true) {
                if (terminated) {
                    break;
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            logger.warn(packageName + " terminated");
            terminated = true;
        }

        logger.warn(packageName + " stopped");
    }
}
