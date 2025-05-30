package litresbot.books;

import java.util.ArrayList;
import java.util.List;

// this is an entry of a single book

public class BookInfo {
    public List<String> titles;
    public List<String> authors;
    public String annotation;
    public List<BookFileInfo> files;

    public BookInfo(BookInfo another) {
        annotation = another.annotation;
        titles = new ArrayList<>(another.titles);
        authors = new ArrayList<>(another.authors);
        files = new ArrayList<>(another.files);
    }

    public BookInfo() {}
}
