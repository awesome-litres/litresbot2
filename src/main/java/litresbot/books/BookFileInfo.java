package litresbot.books;

// this is an entry of a single file with book content

public class BookFileInfo {
    public long id;
    public String language;
    public String fileType;

    public BookFileInfo(BookFileInfo another) {
        id = another.id;
        language = another.language;
        fileType = another.fileType;
    }

    public BookFileInfo() {}
}
