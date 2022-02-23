package ru.bstu.it32.davydov.lab5;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.ArrayList;

import static ru.bstu.it32.davydov.lab5.Program.*;

class XMLAgeMax extends DefaultHandler {

    private boolean founded = false;
    private String name;
    private String surname;
    private byte age = 0;
    private byte level = 0;
    private byte schoolId = 0;
    private String lastElementName;
    private long id;
    private ArrayList<Byte> marks = new ArrayList<Byte>();
    private ArrayList<String> subjects = new ArrayList<String>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        lastElementName = qName;
        if (lastElementName.equals("pupil")) {
            id = Long.parseLong(attributes.getValue("id"));
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String information = new String(ch, start, length);
        information = information.replace("\n", "").trim();
        if (!information.isEmpty()) {
            if (lastElementName.equals("name")) {
                name = information;
                return;
            }
            if (lastElementName.equals("surname")) {
                surname = information;
                return;
            }
            if (lastElementName.equals("age")) {
                age = Byte.parseByte(information);
                return;
            }
            if (lastElementName.equals("level")) {
                level = Byte.parseByte(information);
                return;
            }
            if (lastElementName.equals("schoolId")) {
                schoolId = Byte.parseByte(information);
                return;
            }
            if (lastElementName.equals("subjectname")) {
                subjects.add(information);
                return;
            }
            if (lastElementName.equals("mark")) {
                marks.add(Byte.parseByte(information));
                return;
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equals("pupil")) {
            try {
                if (age == maxAge) {
                    founded = true;
                    String schoolName = getSchoolName(schoolId);
                    Pupil p = new Pupil(id, name, surname, age, level, schoolName, subjects.get(0), marks.get(0));
                    for (byte i = 1; i < subjects.size(); i++) {
                        p.subjects.add(subjects.get(i));
                        p.marks.add(marks.get(i));
                    }
                    Program.pupilOut(p);
                    System.out.println("--------------------------------------------------------------------");
                }
                id = 0;
                name = null;
                surname = null;
                level = 0;
                age = 0;
                schoolId = 0;
                marks.clear();
                subjects.clear();
            } catch (Exception e) {
                throw new SAXException();
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
        if (!founded) {
            System.out.println(getPropertyValue("noRecords"));
        }
    }
}
