package litresbot.books;

import java.util.ArrayList;
import java.util.List;

import litresbot.telegram.TelegramFilterHtml;

// this is an entry of a single book filtered for display purposes

public class BookInfoFiltered {
    public List<String> titles;
    public List<String> authors;
    public String annotation;
    public List<BookFileInfo> files;

    public BookInfoFiltered(BookInfo another) {
        annotation = TelegramFilterHtml.filterText(another.annotation);
        authors = new ArrayList<>();
        for (final var a: another.authors) {
            authors.add(TelegramFilterHtml.filterText(a));
        }
        titles = new ArrayList<>();
        for (final var a: another.titles) {
            titles.add(TelegramFilterHtml.filterText(a));
        }
        files = new ArrayList<>(another.files);
    }
}
