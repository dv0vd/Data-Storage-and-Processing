package ru.bstu.it32.davydov.lab5;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import static ru.bstu.it32.davydov.lab5.Program.*;

class XMLAgeMinMax extends DefaultHandler {

    private byte age = 0;
    private String lastElementName;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        lastElementName = qName;

    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String information = new String(ch, start, length);
        information = information.replace("\n", "").trim();
        if (!information.isEmpty()) {
            if (lastElementName.equals("age")) {
                age = Byte.parseByte(information);
                if (age > maxAge)
                    maxAge = age;
                if (age < minAge)
                    minAge = age;
                return;
            }
        }
    }
}
