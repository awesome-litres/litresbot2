package litresbot.books;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BookDownloader {
    final static Logger logger = LogManager.getLogger(BookDownloader.class);

    public static String folder = "./tmp";

    public static void setDownloadPath(String path) {
        File dir = new File(path);
        logger.info("Setting book downloader folder to: " + dir.getAbsolutePath());
        if (!dir.exists()) {
            logger.info("Book downloader folder does not exist. Creating new folder.");
            if (!dir.mkdirs()) {
                logger.warn("Book downloader folder creating failed.");
            }
        }
        BookDownloader.folder = path;
    }

    public static byte[] download(String fileName) throws IOException {
        // TODO: use S3 downloader
        byte[] book = getBookContent(fileName);
        if (book != null) {
            logger.info("book " + fileName + " found in files");
            return book;
        }
        return new byte[100];
    }

    public static byte[] downloadAndUnzip(String fileName) throws IOException {
        byte[] bookContent = getUnzippedBook(fileName);
        if (bookContent != null) {
            logger.info("book " + fileName + " found in unzipped files");
            return bookContent;
        }

        // try to download if not found in unzipped files
        byte[] zipContent = download(fileName);
        return unzipBook(zipContent, fileName);
    }

    private static byte[] getBookContent(String filename) throws IOException {
        File file = new File(folder + "/" + filename);

        if (!file.exists())
            return null;
        if (!file.canRead())
            return null;

        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);

        byte data[] = new byte[1024];
        int read;

        byte[] book = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            while ((read = bufferedInputStream.read(data, 0, 1024)) >= 0) {
                baos.write(data, 0, read);
            }
        } finally {
            fis.close();
        }

        book = baos.toByteArray();
        return book;
    }

    private static byte[] getUnzippedBook(String filename) throws IOException {
        if (!filename.endsWith(".zip")) {
            throw new IOException("Not a zip archive. File: " + filename);
        }

        String filenameStripped = filename.replace(".zip", "");
        return getBookContent(filenameStripped);
    }

    private static byte[] unzipBook(byte[] content, String filename) throws IOException {
        if (!filename.endsWith(".zip")) {
            throw new IOException("Not a zip archive. File: " + filename);
        }

        String filenameStripped = filename.replace(".zip", "");

        ByteArrayInputStream fileStream = new ByteArrayInputStream(content);
        ZipInputStream zis = new ZipInputStream(fileStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ZipEntry zipEntry = null;

            while (true) {
                zipEntry = zis.getNextEntry();
                if (zipEntry == null)
                    break;

                String zipEntryName = zipEntry.getName();

                if (zipEntryName.toLowerCase().equals(filenameStripped.toLowerCase()))
                    break;
            }

            if (zipEntry == null) {
                throw new IOException("Book not found in zip archive. File: " + filename);
            }

            byte[] buffer = new byte[1024];

            File newFile = new File(BookDownloader.folder + "/" + filenameStripped);
            FileOutputStream fos = new FileOutputStream(newFile);

            try {
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                    baos.write(buffer, 0, len);
                }
            } finally {
                fos.close();
            }
        } finally {
            zis.closeEntry();
            zis.close();
        }

        byte[] book = baos.toByteArray();
        return book;
    }
}
