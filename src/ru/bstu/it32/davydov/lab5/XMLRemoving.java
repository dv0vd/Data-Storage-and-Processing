package ru.bstu.it32.davydov.lab5;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import static ru.bstu.it32.davydov.lab5.Program.*;

class XMLRemoving extends DefaultHandler {

    private long pid;
    private boolean removed = false;
    private long removingId;
    private String lastElementName = "";
    private String pname = null;
    private String psurname = null;
    private byte page = 0;
    private byte plevel = 0;
    private byte pschoolId = 0;
    private ArrayList<Byte> pmarks = new ArrayList<Byte>();
    private ArrayList<String> psubjects = new ArrayList<String>();
    Document doc;
    Element rootElement;

    protected XMLRemoving() throws RuntimeException, SAXException {
        while (true) {
            System.out.print(getPropertyValue("chooseRecordForRemoving"));
            String answer = sin.nextLine();
            try {
                removingId = Long.parseLong(answer);
            } catch (Exception e) {
                continue;
            }
            if (removingId >= 0) break;
        }
        if (removingId == 0) throw new RuntimeException();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
            rootElement = doc.createElement("pupils");
            doc.appendChild(rootElement);
        } catch (Exception e) {
            throw new SAXException();
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        lastElementName = qName;
        if (lastElementName.equals("pupil")) {
            pid = Long.parseLong(attributes.getValue("id"));
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String information = new String(ch, start, length);
        information = information.replace("\n", "").trim();
        if (!information.isEmpty()) {
            if (lastElementName.equals("name")) {
                pname = information;
                return;
            }
            if (lastElementName.equals("surname")) {
                psurname = information;
                return;
            }
            if (lastElementName.equals("age")) {
                page = Byte.parseByte(information);
                return;
            }
            if (lastElementName.equals("level")) {
                plevel = Byte.parseByte(information);
                return;
            }
            if (lastElementName.equals("schoolId")) {
                pschoolId = Byte.parseByte(information);
                return;
            }
            if (lastElementName.equals("subjectname")) {
                psubjects.add(information);
                return;
            }
            if (lastElementName.equals("mark")) {
                pmarks.add(Byte.parseByte(information));
                return;
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
        doc.getDocumentElement().normalize();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(doc);
            StreamResult file = new StreamResult(new File(getPropertyValue("XMLpath")));
            transformer.transform(source, file);
            if (removed) {
                System.out.println(getPropertyValue("success"));
            } else {
                System.out.println(getPropertyValue("noPupils"));
            }
        } catch (Exception e) {
            System.out.println(getPropertyValue("errorXMLRemoving"));
            return;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equals("pupil")) {
            try {
                if (pid != removingId) {
                    Element pupil = doc.createElement("pupil");
                    pupil.setAttribute("id", Long.toString(pid));
                    Element name = doc.createElement("name");
                    name.setTextContent(pname);
                    pupil.appendChild(name);
                    Element surname = doc.createElement("surname");
                    surname.setTextContent(psurname);
                    pupil.appendChild(surname);
                    Element age = doc.createElement("age");
                    age.setTextContent(Byte.toString(page));
                    pupil.appendChild(age);
                    Element level = doc.createElement("level");
                    level.setTextContent(Byte.toString(plevel));
                    pupil.appendChild(level);
                    Element school = doc.createElement("schoolId");
                    school.setTextContent(Byte.toString(pschoolId));
                    pupil.appendChild(school);
                    Element subjects = doc.createElement("subjects");
                    for (byte i = 0; i < psubjects.size(); i++) {
                        Element subject = doc.createElement("subject");
                        Element subjectName = doc.createElement("subjectname");
                        Element mark = doc.createElement("mark");
                        subjectName.setTextContent(psubjects.get(i));
                        mark.setTextContent(Byte.toString(pmarks.get(i)));
                        subject.appendChild(subjectName);
                        subject.appendChild(mark);
                        subjects.appendChild(subject);
                    }
                    pupil.appendChild(subjects);
                    rootElement.appendChild(pupil);
                    pid = 0;
                    pname = null;
                    psurname = null;
                    plevel = 0;
                    page = 0;
                    pschoolId = 0;
                    pmarks.clear();
                    psubjects.clear();
                } else {
                    removed = true;
                    pmarks.clear();
                    psubjects.clear();
                }
            } catch (Exception e) {
                throw new SAXException();
            }
        }
    }
}
