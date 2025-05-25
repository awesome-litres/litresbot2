package litresbot.books;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class FictionBook {
    protected final Document xmlDocument;

    public FictionBook(InputStream is) throws SAXException, IOException, ParserConfigurationException {
        final var factory = DocumentBuilderFactory.newInstance();
        final var builder = factory.newDocumentBuilder();
        xmlDocument = builder.parse(is);
    }

    public Document asDocument() {
        return xmlDocument;
    }
}