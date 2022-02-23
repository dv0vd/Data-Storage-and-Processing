package ru.bstu.it32.davydov.lab5;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.ArrayList;

class XMLSearchEdditingPupil extends DefaultHandler {

    private String lastElementName;
    private long id;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        lastElementName = qName;
        if (lastElementName.equals("pupil")) {
            id = Long.parseLong(attributes.getValue("id"));
            if (id == Program.edditingid) {
                Program.foundEdit = true;
            }
        }
    }
}
