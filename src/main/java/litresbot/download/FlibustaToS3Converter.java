package litresbot.download;

import java.nio.file.Paths;
import java.sql.SQLException;

import javax.sql.DataSource;

import litresbot.books.BookFileInfo;
import litresbot.database.DatabasePool;

public class FlibustaToS3Converter {
    protected S3BookClient client;

    public static DataSource db = DatabasePool.getDataSource();

    public FlibustaToS3Converter(S3BookClient client) {
        this.client = client;
    }

    public String convertToPath(BookFileInfo bookFileInfo) throws SQLException {
        final var connection = db.getConnection();

        final var sql = "SELECT * FROM library.libfilename WHERE BookId = ?";
        final var preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setLong(1, bookFileInfo.id);
        final var resultSet = preparedStatement.executeQuery();
        var fileName = Long.toString(bookFileInfo.id) + "." + bookFileInfo.fileType;

        boolean user = !bookFileInfo.fileType.equals("fb2");

        if (resultSet.next()) {
            final var dbFileName = resultSet.getString("FileName");
            if (dbFileName != null && !dbFileName.isEmpty()) {
                fileName = dbFileName;
                user = true;
            }
        }

        var folder_prefix = "f.fb2";
        if (bookFileInfo.id <= 172702) {
            folder_prefix = "fb2";
        }
        if (user) {
            folder_prefix = "f.usr";
            if (bookFileInfo.id <= 172702) {
                folder_prefix = "usr";
            }
        }

        // now we should look for the folder 'folder_prefix'-'id_from'-'id_to'_zip
        // and check for the file 'fileName' or 'fileName.zip' inside it
        final var s3Files = client.listFiles("");

        for (final var s3File : s3Files) {
            // search for corresponding folders
            if (!s3File.startsWith(folder_prefix)) {
                continue;
            }
            if (!s3File.endsWith("_zip/")) {
                continue;
            }
            // remove 'prefix-' and '_zip/' 
            final var folderName = s3File.substring(folder_prefix.length() + 1, s3File.length() - "_zip/".length());
            // split by '-'
            final var folderParts = folderName.split("-");
            if (folderParts.length != 2) {
                continue;
            }
            final var idFrom = Long.parseLong(folderParts[0]);
            final var idTo = Long.parseLong(folderParts[1]);
            // check if the book id is in the range
            if (bookFileInfo.id < idFrom || bookFileInfo.id > idTo) {
                continue;
            }
            // check if the file exists in the folder
            final var s3BookFiles = client.listFiles(s3File);
            for (final var s3BookFile : s3BookFiles) {
                if (!s3BookFile.equals(fileName) && !s3BookFile.equals(fileName + ".zip")) {
                    continue;
                }
                return Paths.get(s3File, s3BookFile).toString().replace("\\", "/");
            }
        }
        return "";
    }
}
