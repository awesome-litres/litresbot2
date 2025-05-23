package litresbot.books.convert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import litresbot.books.FictionBook;
import litresbot.books.convert.TagPosition.TagType;

public class Fb2Converter {
    public final static String PARAGRAPH_INDENT = "    ";

    public static List<String> convertToText(FictionBook book, int pageSize) throws IOException {
        List<String> pages = new ArrayList<String>();
        NodeList fb2BodyList = book.xmlDocument.getElementsByTagName("body");
        if (fb2BodyList.getLength() == 0)
            return pages;

        Node fb2Body = fb2BodyList.item(0);
        if (fb2Body.getNodeType() != Node.ELEMENT_NODE)
            return pages;

        TextParagraphPrinter printer = new TextParagraphPrinter();
        printer.pageSize = pageSize;
        printer.indent = PARAGRAPH_INDENT;
        printBody((Element) fb2Body, printer, pages);
        return pages;
    }

    public static List<String> convertToTelegram(FictionBook book, int pageSize) throws IOException {
        List<String> pages = new ArrayList<String>();
        NodeList fb2BodyList = book.xmlDocument.getElementsByTagName("body");
        if (fb2BodyList.getLength() == 0)
            return pages;

        Node fb2Body = fb2BodyList.item(0);
        if (fb2Body.getNodeType() != Node.ELEMENT_NODE)
            return pages;

        TextParagraphPrinter printer = new TelegramParagraphPrinter();
        printer.pageSize = pageSize;
        printer.indent = PARAGRAPH_INDENT;
        printBody((Element) fb2Body, printer, pages);
        return pages;
    }

    private static void printBody(Element body, TextParagraphPrinter printer, List<String> pages) throws IOException {
        NodeList sections = body.getElementsByTagName("section");

        // add first title of the body and finish search
        NodeListIterator titlesIterator = new NodeListIterator(body);
        for (Node n : titlesIterator.getIterable()) {
            if (n.getNodeName() != "title")
                continue;
            printSection(n, printer, pages);
            break;
        }

        // add all sections of the body (including childrens' children) to the
        // sectionNodes
        NodeListIterator sectionsIterator = new NodeListIterator(sections);
        for (Node n : sectionsIterator.getIterable()) {
            printSection(n, printer, pages);
        }

        printer.flush(pages);
    }

    private static void printSection(Node section, TextParagraphPrinter printer, List<String> pages)
            throws IOException {
        NodeListIterator sectionChildrenIterator = new NodeListIterator(section);

        for (Node c : sectionChildrenIterator.getIterable()) {
            if (c.getNodeName() == "title") {
                // process title paragraphs
                NodeListIterator titleChildrenIterator = new NodeListIterator(c);
                for (Node t : titleChildrenIterator.getIterable()) {
                    if (t.getNodeName() != "p")
                        continue;
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
        Stack<ParagraphNode> stk = new Stack<ParagraphNode>();
        ParagraphNode topParagraph = new ParagraphNode();
        topParagraph.node = node;
        topParagraph.text = node.getTextContent();
        stk.push(topParagraph);

        while (!stk.isEmpty()) {
            ParagraphNode top = stk.pop();

            if (top.node == null) {
                printer.printParagraph(top, pages, fromTitle);
                continue;
            }

            NodeListIterator paragraphsIterator = new NodeListIterator(top.node);
            List<ParagraphNode> children = new ArrayList<ParagraphNode>();
            ParagraphNode currentParagraph = null;

            for (Node p : paragraphsIterator.getIterable()) {
                if (p.getNodeName() == "p") {
                    if (currentParagraph != null) {
                        children.add(currentParagraph);
                        currentParagraph = null;
                    }

                    ParagraphNode childParagraph = new ParagraphNode();
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
                    TagPosition tag = new TagPosition();
                    tag.from = currentParagraph.text.length();
                    tag.to = currentParagraph.text.length() + p.getTextContent().length();
                    tag.type = TagType.BOLD;
                    currentParagraph.tags.add(tag);
                    currentParagraph.text += p.getTextContent();
                    continue;
                }
                if (p.getNodeName() == "emphasis") {
                    TagPosition tag = new TagPosition();
                    tag.from = currentParagraph.text.length();
                    tag.to = currentParagraph.text.length() + p.getTextContent().length();
                    tag.type = TagType.ITALIC;
                    currentParagraph.tags.add(tag);
                    currentParagraph.text += p.getTextContent();
                    continue;
                }
                if (p.getNodeName() == "strikethrough") {
                    TagPosition tag = new TagPosition();
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

            ListIterator<ParagraphNode> iterator = children.listIterator(children.size());
            while (iterator.hasPrevious()) {
                ParagraphNode p = iterator.previous();
                stk.push(p);
            }
        }
    }
}
