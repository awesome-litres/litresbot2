package litresbot.books;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import litresbot.download.FlibustaS3BookDownloader;

public class BookDownloader {
    final static Logger logger = LogManager.getLogger(BookDownloader.class);
    public final FlibustaS3BookDownloader downloader;

    public BookDownloader(FlibustaS3BookDownloader downloader) {
        this.downloader = downloader;
    }

    public String download(BookFileInfo book) throws IOException, SQLException {
        {
            final var destFilePath = downloader.getSaveFileName(book);
            final var destFile = new File(destFilePath);
            if (!destFilePath.isEmpty() && destFile.exists()) {
                logger.info("Book {} already exists in files", book.id);
                return destFilePath;
            }
            final var zipDestFilePath = destFilePath + ".zip";
            final var zipDestFile = new File(zipDestFilePath);
            if (!zipDestFilePath.isEmpty() && zipDestFile.exists()) {
                logger.info("Book {} already exists in files", book.id);
                return zipDestFilePath;
            }
        }

        final var destFilePath = downloader.downloadBook(book);
        if (destFilePath.isEmpty()) {
            logger.error("Failed to download book {} from S3", book.id);
            throw new IOException("Failed to download book");
        }
        return destFilePath;
    }

    public byte[] read(BookFileInfo book) throws IOException, SQLException {
        {
            final var destFilePath = downloader.getSaveFileName(book);
            final var destFile = new File(destFilePath);
            if (!destFilePath.isEmpty() && destFile.exists()) {
                return Files.readAllBytes(destFile.toPath());
            }
        }

        final var destFilePath = downloader.downloadBook(book);
        if (destFilePath.isEmpty()) {
            logger.error("Failed to download book {} from S3", book.id);
            throw new IOException("Failed to download book");
        }
        return Files.readAllBytes(new File(destFilePath).toPath());
    }
}
