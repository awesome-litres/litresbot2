package litresbot.books.convert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import litresbot.books.FictionBook;
import litresbot.books.convert.TagPosition.TagType;

public class Fb2Converter {
    public final static String PARAGRAPH_INDENT = "    ";

    public static List<String> convertToTelegram(FictionBook book, int pageSize) throws IOException {
        final var pages = new ArrayList<String>();
        final var fb2BodyList = book.asDocument().getElementsByTagName("body");
        if (fb2BodyList.getLength() == 0) {
            return pages;
        }

        final var fb2Body = fb2BodyList.item(0);
        if (fb2Body.getNodeType() != Node.ELEMENT_NODE) {
            return pages;
        }

        final var printer = new TelegramParagraphPrinter();
        printer.pageSize = pageSize;
        printer.indent = PARAGRAPH_INDENT;
        printBody((Element) fb2Body, printer, pages);
        return pages;
    }

    private static void printBody(Element body, TextParagraphPrinter printer, List<String> pages) throws IOException {
        final var sections = body.getElementsByTagName("section");

        // add first title of the body and finish search
        final var titlesIterator = new NodeListIterator(body);
        for (final var n : titlesIterator.getIterable()) {
            if (n.getNodeName() != "title") {
                continue;
            }
            printSection(n, printer, pages);
            break;
        }

        // add all sections of the body (including childrens' children) to the
        // sectionNodes
        final var sectionsIterator = new NodeListIterator(sections);
        for (final var n : sectionsIterator.getIterable()) {
            printSection(n, printer, pages);
        }

        printer.flush(pages);
    }

    private static void printSection(Node section, TextParagraphPrinter printer, List<String> pages)
            throws IOException {
        final var sectionChildrenIterator = new NodeListIterator(section);

        for (final var c : sectionChildrenIterator.getIterable()) {
            if (c.getNodeName() == "title") {
                // process title paragraphs
                final var titleChildrenIterator = new NodeListIterator(c);
                for (final var t : titleChildrenIterator.getIterable()) {
                    if (t.getNodeName() != "p") {
                        continue;
                    }
                    printParagraphTree(t, printer, pages, true);
                }
                continue;
            }
            if (c.getNodeName() == "p") {
                printParagraphTree(c, printer, pages, false);
            }
        }
    }

    private static void printParagraphTree(Node node, TextParagraphPrinter printer, List<String> pages,
            boolean fromTitle) throws IOException {
        var stk = new Stack<ParagraphNode>();
        final var topParagraph = new ParagraphNode();
        topParagraph.node = node;
        topParagraph.text = node.getTextContent();
        stk.push(topParagraph);

        while (!stk.isEmpty()) {
            final var top = stk.pop();

            if (top.node == null) {
                printer.printParagraph(top, pages, fromTitle);
                continue;
            }

            final var paragraphsIterator = new NodeListIterator(top.node);
            final var children = new ArrayList<ParagraphNode>();
            ParagraphNode currentParagraph = null;

            for (final var p : paragraphsIterator.getIterable()) {
                if (p.getNodeName() == "p") {
                    if (currentParagraph != null) {
                        children.add(currentParagraph);
                        currentParagraph = null;
                    }

                    final var childParagraph = new ParagraphNode();
                    childParagraph.node = p;
                    childParagraph.text = p.getTextContent();
                    children.add(childParagraph);
                    continue;
                }

                if (currentParagraph == null) {
                    currentParagraph = new ParagraphNode();
                    currentParagraph.text = "";
                }
                if (p.getNodeName() == "strong") {
                    final var tag = new TagPosition();
                    tag.from = currentParagraph.text.length();
                    tag.to = currentParagraph.text.length() + p.getTextContent().length();
                    tag.type = TagType.BOLD;
                    currentParagraph.tags.add(tag);
                    currentParagraph.text += p.getTextContent();
                    continue;
                }
                if (p.getNodeName() == "emphasis") {
                    final var tag = new TagPosition();
                    tag.from = currentParagraph.text.length();
                    tag.to = currentParagraph.text.length() + p.getTextContent().length();
                    tag.type = TagType.ITALIC;
                    currentParagraph.tags.add(tag);
                    currentParagraph.text += p.getTextContent();
                    continue;
                }
                if (p.getNodeName() == "strikethrough") {
                    final var tag = new TagPosition();
                    tag.from = currentParagraph.text.length();
                    tag.to = currentParagraph.text.length() + p.getTextContent().length();
                    tag.type = TagType.STRIKE;
                    currentParagraph.tags.add(tag);
                    currentParagraph.text += p.getTextContent();
                    continue;
                }
                if (p.getNodeName() == "subtitle") {
                    currentParagraph.text += p.getTextContent();
                    continue;
                }
                currentParagraph.text += p.getTextContent();
            }

            if (currentParagraph != null) {
                children.add(currentParagraph);
            }

            final var iterator = children.listIterator(children.size());
            while (iterator.hasPrevious()) {
                final var p = iterator.previous();
                stk.push(p);
            }
        }
    }
}
