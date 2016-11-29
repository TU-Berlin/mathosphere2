package com.formulasearchengine.mathosphere.mathpd.pojos;

import com.formulasearchengine.mathmlquerygenerator.xmlhelper.NonWhitespaceNodeList;
import com.formulasearchengine.mathmlquerygenerator.xmlhelper.XMLHelper;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import org.apache.commons.lang3.StringUtils;
import org.apache.xpath.operations.Mult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;

public class ArxivDocument {

    public String title;
    public String text;

    public ArxivDocument() {
    }

    public ArxivDocument(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public Document getDoc() {
        return XMLHelper.String2Doc(text, true);
    }

    /**
     * Returns all mathematical expressions, i.e., all nodes in the XML that start with <math ...> (from any namespace)
     * This can include both content and presentation MathML.
     *
     * @return
     * @throws XPathExpressionException
     */
    public NonWhitespaceNodeList getMathTags() throws XPathExpressionException {
        return new NonWhitespaceNodeList(XMLHelper.getElementsB(getDoc(), "//*:math"));
    }

    /**
     * Returns all content MathML elements, i.e., ci (content identifiers), co (), cn (content numbers) of the whole document
     * @return
     * @throws XPathExpressionException
     */
    public Multiset<String> getCElements() throws XPathExpressionException {
        final Multiset<String> identifiersFromCmml = HashMultiset.create();
        for (Node n : getMathTags()) {
            identifiersFromCmml.addAll(getCElements(n));
        }
        return identifiersFromCmml;
    }

    /**
     * Returns all content MathML elements, i.e., ci (content identifiers), co (), cn (content numbers) for a given expression
     * @return
     * @throws XPathExpressionException
     */
    public Multiset<String> getCElements(Node expression) throws XPathExpressionException {
        return XMLHelper.getIdentifiersFromCmml(expression);
    }

    @Override
    public String toString() {
        return "[title=" + title + ", text=" + StringUtils.abbreviate(text, 100) + "]";
    }

}
