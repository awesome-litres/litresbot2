package litresbot.search.db;

import java.util.List;

// database entry for a book search result

public class SearchBookInfo {
    public long id;
    public List<String> titles;
    public List<String> authors;
    public String annotation;
    public String language;
    public String fileType;
}
