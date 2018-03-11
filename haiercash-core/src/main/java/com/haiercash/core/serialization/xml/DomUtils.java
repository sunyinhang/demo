package com.haiercash.core.serialization.xml;

import com.haiercash.core.io.CharsetNames;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Encapsulating XML common operations.
 *
 * @author carver
 * @since 1.0, Jun 12, 2007
 */
final class DomUtils {
    /**
     * 过滤不可见字符
     */
    private static String stripNonValidXMLCharacters(String input) {
        if (input == null || "".equals(input))
            return "";
        StringBuilder out = new StringBuilder();
        char current;
        for (int i = 0; i < input.length(); i++) {
            current = input.charAt(i);
            if (current == 0x9 || current == 0xA || current == 0xD || current >= 0x20 && current <= 0xD7FF || current >= 0xE000 && current <= 0xFFFD)
                out.append(current);
        }
        return out.toString();
    }

    /**
     * Gets the encoding pattern from given XML file.
     *
     * @param text the context of the XML file
     * @return the encoding pattern string of given XML file
     */
    private static String getEncoding(String text) {
        String result = CharsetNames.UTF_8;//默认编码格式

        String xml = text.trim();

        if (xml.startsWith("<?xml")) {
            int end = xml.indexOf("?>");
            String sub = xml.substring(0, end);
            StringTokenizer tokens = new StringTokenizer(sub, " =\"\'");

            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();

                if ("encoding".equals(token)) {
                    if (tokens.hasMoreTokens()) {
                        result = tokens.nextToken();
                    }

                    break;
                }
            }
        }
        return result;
    }

    /**
     * Parses the content of the given stream as an XML document.
     *
     * @param in the XML file input stream
     * @return the document instance representing the entire XML document
     */
    public static Document getDocument(InputStream in) {
        try (InputStream stream = in) {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            return builder.parse(stream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the root element from the given XML payload.
     *
     * @param payload the XML payload representing the XML file.
     * @return the root element of parsed document
     */
    public static Element getRootElementFromString(String payload) {
        if (payload == null || payload.trim().length() < 1) {
            throw new RuntimeException("XML_PAYLOAD_EMPTY");
        }

        byte[] bytes;
        try {
            payload = stripNonValidXMLCharacters(payload);
            String encodeString = getEncoding(payload);
            bytes = payload.getBytes(encodeString);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("XML_ENCODE_ERROR", e);
        }

        InputStream in = new ByteArrayInputStream(bytes);
        return getDocument(in).getDocumentElement();
    }

    /**
     * Gets the descendant elements list from the parent element.
     *
     * @param parent  the parent element in the element tree
     * @param tagName the specified tag name
     * @return the NOT NULL descendant elements list
     */
    public static List<Element> getElements(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        List<Element> elements = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                elements.add((Element) node);
            }
        }

        return elements;
    }

    /**
     * Gets the immediately descendant element from the parent element.
     *
     * @param parent  the parent element in the element tree
     * @param tagName the specified tag name.
     * @return immediately descendant element of parent element, NULL otherwise.
     */
    public static Element getElement(Element parent, String tagName) {
        List<Element> children = getElements(parent, tagName);
        if (children.isEmpty()) {
            return null;
        } else {
            return children.get(0);
        }
    }

    /**
     * Gets the immediately child elements list from the parent element.
     *
     * @param parent  the parent element in the element tree
     * @param tagName the specified tag name
     * @return the NOT NULL immediately child elements list
     */
    public static List<Element> getChildElements(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        List<Element> elements = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element && node.getParentNode() == parent) {
                elements.add((Element) node);
            }
        }

        return elements;
    }

    /**
     * Gets the immediately child element from the parent element.
     *
     * @param parent  the parent element in the element tree
     * @param tagName the specified tag name
     * @return immediately child element of parent element, NULL otherwise
     */
    public static Element getChildElement(Element parent, String tagName) {
        List<Element> children = getChildElements(parent, tagName);

        if (children.isEmpty()) {
            return null;
        } else {
            return children.get(0);
        }
    }

    /**
     * Gets the value of the child element by tag name under the given parent
     * element. If there is more than one child element, return the value of the
     * first one.
     *
     * @param parent  the parent element
     * @param tagName the tag name of the child element
     * @return value of the first child element, NULL if tag not exists
     */
    public static String getElementValue(Element parent, String tagName) {
        Element element = getChildElement(parent, tagName);
        return getElementValue(element);
    }

    /**
     * Gets the text value of current element.
     *
     * @return text value of the element, NULL if element not exists
     */
    public static String getElementValue(Element element) {
        if (element != null) {
            NodeList nodes = element.getChildNodes();
            if (nodes != null && nodes.getLength() > 0) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    if (node instanceof Text) {
                        return ((Text) node).getData();
                    }
                }
            }
        }

        return null;
    }
}
