package litresbot.download;

import litresbot.books.BookFileInfo;

import java.nio.file.Paths;
import java.sql.SQLException;

public class FlibustaS3BookDownloader {
    protected S3BookClient client;
    protected String downloadPath;
    protected FlibustaToS3Converter pathConverter;

    public FlibustaS3BookDownloader(S3BookClient client, String downloadPath) {
        this.client = client;
        this.downloadPath = downloadPath;
        pathConverter = new FlibustaToS3Converter(client);
    }

    public String downloadBook(BookFileInfo bookFileInfo) throws SQLException {
        final var filePath = pathConverter.convertToPath(bookFileInfo);
        if (filePath.isEmpty()) {
                return "";
        }

        final var tryPaths = filePath.endsWith(".zip")
            ? new String[]{filePath}
            : new String[]{filePath + ".zip", filePath};

        for (final var path : tryPaths) {
            if (!client.fileExists(path)) {
                continue;
            }
            final var bookFile = Paths.get(path).getFileName().toString();
            final var savePath = Paths.get(downloadPath, bookFile);
            if (client.downloadFile(path, savePath.toString()) == 200) {
                return savePath.toString();
            }
        }
        return "";
    }

    public String getSaveFileName(BookFileInfo book) throws SQLException {
        final var bookFile = "" + book.id + "." + book.fileType;
        return Paths.get(downloadPath, bookFile).toString();
    }
}
