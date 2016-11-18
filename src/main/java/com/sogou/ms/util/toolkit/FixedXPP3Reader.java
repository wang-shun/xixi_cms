package com.sogou.ms.util.toolkit;

import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.XPP3Reader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * dom4j提供的XPP3Reader有bug，跳过了所有的XmlPullParser.ENTITY_REF（CDATA），本类修正之
 * @see XPP3Reader
 */
public final class FixedXPP3Reader extends XPP3Reader {

	@Override
	protected Document parseDocument() throws DocumentException, IOException, XmlPullParserException {
		DocumentFactory df = getDocumentFactory();
		Document document = df.createDocument();
		Element parent = null;
		XmlPullParser pp = getXPPParser();
		pp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

		while (true) {
			int type = pp.nextToken();

			switch (type) {
				case XmlPullParser.PROCESSING_INSTRUCTION: {
					String text = pp.getText();
					int loc = text.indexOf(' ');

					if (loc >= 0) {
						String target = text.substring(0, loc);
						String txt = text.substring(loc + 1);
						document.addProcessingInstruction(target, txt);
					} else {
						document.addProcessingInstruction(text, "");
					}

					break;
				}

				case XmlPullParser.COMMENT: {
					if (parent != null) {
						parent.addComment(pp.getText());
					} else {
						document.addComment(pp.getText());
					}

					break;
				}

				case XmlPullParser.CDSECT: {
					if (parent != null) {
						parent.addCDATA(pp.getText());
					} else {
						String msg = "Cannot have text content outside of the " + "root document";
						throw new DocumentException(msg);
					}

					break;
				}

				case XmlPullParser.END_DOCUMENT:
					return document;

				case XmlPullParser.START_TAG: {
					QName qname = (pp.getPrefix() == null) ? df.createQName(pp.getName(), pp.getNamespace()) : df
							.createQName(pp.getName(), pp.getPrefix(), pp.getNamespace());
					Element newElement = df.createElement(qname);
					int nsStart = pp.getNamespaceCount(pp.getDepth() - 1);
					int nsEnd = pp.getNamespaceCount(pp.getDepth());

					for (int i = nsStart; i < nsEnd; i++) {
						if (pp.getNamespacePrefix(i) != null) {
							newElement.addNamespace(pp.getNamespacePrefix(i), pp.getNamespaceUri(i));
						}
					}

					for (int i = 0; i < pp.getAttributeCount(); i++) {
						QName qa = (pp.getAttributePrefix(i) == null) ? df.createQName(pp.getAttributeName(i)) : df
								.createQName(pp.getAttributeName(i), pp.getAttributePrefix(i), pp.getAttributeNamespace(i));
						newElement.addAttribute(qa, pp.getAttributeValue(i));
					}

					if (parent != null) {
						parent.add(newElement);
					} else {
						document.add(newElement);
					}

					parent = newElement;

					break;
				}

				case XmlPullParser.END_TAG: {
					if (parent != null) {
						parent = parent.getParent();
					}

					break;
				}

				case XmlPullParser.ENTITY_REF:
				case XmlPullParser.TEXT: {
					String text = pp.getText();

					if (parent != null) {
						parent.addText(text);
					} else {
						String msg = "Cannot have text content outside of the " + "root document";
						throw new DocumentException(msg);
					}

					break;
				}

				default:
					break;
			}
		}
	}

}
