package com.sogou.ms.util.toolkit;

import com.sogou.ms.util._;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static com.sogou.ms.util._.asList;

public final class Xml {

	/* ------------------------- parse ------------------------- */

    public static Document parse(Reader reader) throws DocumentException, XmlPullParserException, IOException {
        return new FixedXPP3Reader().read(reader);
    }

    public static Document parse(String xml) throws DocumentException, XmlPullParserException, IOException {
        return parse(new StringReader(xml));
    }

    /* ------------------------- util (import from tc-entity) ------------------------- */
    public static String textNotNull(Element elem, String xpath) {
        return _.trimToEmpty(text(elem, xpath));
    }

    public static String text(Element elem, String xpath) {
        String[] paths = xpath.split("/");
        for (int i = 0; i < paths.length - 1; i++) {
            if (!paths[i].isEmpty() && elem != null)
                elem = elem.element(paths[i]);
        }
        if (elem == null)
            return null;
        String lastPath = paths[paths.length - 1];
        if (lastPath.isEmpty())
            return elem.getTextTrim();
        if (lastPath.startsWith("@"))
            return elem.attributeValue(lastPath.substring(1));
        return elem.elementText(lastPath);
    }

    public static Element elem(Element root, String xpath) {
        Element elem = root;
        for (String path : xpath.split("/")) {
            if (elem != null)
                elem = elem.element(path);
        }
        return elem;
    }

    public static List<Element> elems(Element root, String xpath) {
        List<Element> src = new ArrayList<>(asList(root));
        List<Element> trg = new ArrayList<>();
        for (String path : xpath.split("/")) {
            for (Element elem : src)
                trg.addAll(elem.elements(path));
            List<Element> swap = src;
            src = trg;
            trg = swap;
            trg.clear();
        }
        return src;
    }

	/* ------------------------- prettyPrint ------------------------- */

    public static String prettyPrint(String xml) {
        if (xml == null)
            return "!!! Empty Xml !!!";

        final Document doc;
        try {
            doc = new FixedXPP3Reader().read(new StringReader(xml));
        } catch (Exception e) {
            return "!!! Parse Xml Error !!!\n" + e + "\n\n\n" + xml;
        }

        try {
            return _prettyPrint(doc);
        } catch (Exception e) {
            return "!!! Format Xml Error !!!\n" + e + "\n\n\n" + xml;
        }
    }

    public static String prettyPrint(Document doc) {
        if (doc == null)
            return "!!! Empty Doc !!!";
        try {
            return _prettyPrint(doc);
        } catch (Exception e) {
            return "!!! Format Doc Error !!!\n" + e;
        }
    }

    static String _prettyPrint(Document doc) throws Exception {
        OutputFormat format = OutputFormat.createPrettyPrint();
        StringWriter out = new StringWriter();
        new XMLWriter(out, format).write(doc);
        return out.toString();
    }

    public static String prettyPrint(Element elem) {
        if (elem == null)
            return "!!! Empty Elem !!!";
        try {
            return _prettyPrint(elem);
        } catch (Exception e) {
            return "!!! Format Elem Error !!!\n" + e;
        }
    }

    private static String _prettyPrint(Element elem) throws Exception {
        OutputFormat format = OutputFormat.createPrettyPrint();
        StringWriter out = new StringWriter();
        new XMLWriter(out, format).write(elem);
        return out.toString();
    }

}
