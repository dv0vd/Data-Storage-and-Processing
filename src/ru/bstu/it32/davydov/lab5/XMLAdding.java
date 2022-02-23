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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import static ru.bstu.it32.davydov.lab5.Program.*;

class XMLAdding extends DefaultHandler {

    private long lastid;
    private String lastElementName = "";
    private String name;
    private String surname;
    private String age;
    private String level;
    private byte schoolId;
    private String schoolName;
    private String oldname;
    private String oldsurname;
    private String oldage;
    private byte oldlevel;
    private long oldid = 0;
    private ArrayList<Byte> oldmarks = new ArrayList<Byte>();
    private ArrayList<String> oldsubjects = new ArrayList<String>();
    private byte oldschoolId;
    private ArrayList<Byte> subjectMarks = new ArrayList<Byte>();
    private ArrayList<String> subjectNames = new ArrayList<String>();
    Document doc;
    Element rootElement;

    protected XMLAdding() throws RuntimeException, SAXException {
        while (true) {
            System.out.print("Имя (0 - отмена добавления записи): ");
            name = sin.nextLine();
            if (name.equals("0")) {
                throw new RuntimeException();
            }
            if (name.length() != 0) break;
        }
        name = name.trim();
        while (true) {
            System.out.print("Фамилия (0 - отмена добавления записи): ");
            surname = sin.nextLine();
            if (surname.equals("0")) {
                throw new RuntimeException();
            }
            if (surname.length() != 0) break;
        }
        surname = surname.trim();
        while (true) {
            System.out.print("Возраст (0 - отмена добавления записи): ");
            age = sin.nextLine();
            if (age.equals("0")) {
                throw new RuntimeException();
            } else {
                byte minAge = Byte.parseByte(getPropertyValue("minAge"));
                byte maxAge = Byte.parseByte(getPropertyValue("maxAge"));
                byte aage;
                try {
                    aage = Byte.parseByte(age);
                } catch (Exception e) {
                    continue;
                }
                if ((aage >= minAge) && (aage <= maxAge)) break;
            }
        }
        while (true) {
            System.out.print("Класс (0 - отмена добавления записи): ");
            level = sin.nextLine();
            if (level.equals("0")) {
                throw new RuntimeException();
            } else {
                byte minLevel = Byte.parseByte(getPropertyValue("minLevel"));
                byte maxLevel = Byte.parseByte(getPropertyValue("maxLevel"));
                byte llevel;
                try {
                    llevel = Byte.parseByte(level);
                } catch (Exception e) {
                    continue;
                }
                if ((llevel >= minLevel) && (llevel <= maxLevel)) break;
            }
        }
        PreparedStatement stat;
        ResultSet rs;
        while (true) {
            System.out.println("Школа (0 - отмена добавления записи): ");
            try {
                stat = con.prepareStatement("select name from Schools");
                rs = stat.executeQuery();
                byte f = 0;
                while (rs.next()) {
                    f++;
                    System.out.println("\t" + f + " - " + rs.getString(1));
                }
                schoolName = sin.nextLine();
                if (schoolName.equals("0")) {
                    throw new RuntimeException();
                } else {
                    try {
                        schoolId = Byte.parseByte(schoolName);
                    } catch (Exception e) {
                        continue;
                    }
                    if ((schoolId >= 0) && (schoolId <= f)) {
                        schoolName = getSchoolName(schoolId);
                        break;
                    }
                }
            } catch (Exception e) {
                throw new SAXException();
            }
        }
        byte subjectsCount;
        try {
            stat = con.prepareStatement("select count(*) " +
                    "from Subjects ");
            rs = stat.executeQuery();
            rs.next();
            subjectsCount = rs.getByte(1);
        } catch (Exception e) {
            throw new RuntimeException();
        }
        try {
            stat = con.prepareStatement("select name " +
                    "from Subjects ");
            rs = stat.executeQuery();
            while (rs.next()) {
                subjectNames.add(rs.getString(1));
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        for (byte i = 0; i < subjectsCount; i++) {
            byte mark;
            while (true) {
                System.out.print("[ " + (i + 1) + " ] Оценка по '" + subjectNames.get(i) + "' (0 - отмена добавления записи): ");
                String mmark = sin.nextLine();
                if (mmark.equals("0")) {
                    throw new RuntimeException();
                } else {
                    byte minMark = Byte.parseByte(getPropertyValue("minMark"));
                    byte maxMark = Byte.parseByte(getPropertyValue("maxMark"));
                    try {
                        mark = Byte.parseByte(mmark);
                    } catch (Exception e) {
                        continue;
                    }
                    if ((mark >= minMark) && (mark <= maxMark)) break;
                }
            }
            subjectMarks.add(mark);
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (Exception e) {
            throw new SAXException();
        }
        doc = builder.newDocument();
        rootElement = doc.createElement("pupils");
        doc.appendChild(rootElement);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String information = new String(ch, start, length);
        information = information.replace("\n", "").trim();
        if (!information.isEmpty()) {
            if (lastElementName.equals("name")) {
                oldname = information;
                return;
            }
            if (lastElementName.equals("surname")) {
                oldsurname = information;
                return;
            }
            if (lastElementName.equals("age")) {
                oldage = information;
                return;
            }
            if (lastElementName.equals("level")) {
                oldlevel = Byte.parseByte(information);
                return;
            }
            if (lastElementName.equals("schoolId")) {
                oldschoolId = Byte.parseByte(information);
                return;
            }
            if (lastElementName.equals("subjectname")) {
                oldsubjects.add(information);
                return;
            }
            if (lastElementName.equals("mark")) {
                oldmarks.add(Byte.parseByte(information));
                return;
            }
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        lastElementName = qName;
        if (lastElementName.equals("pupil")) {
            oldid = Long.parseLong(attributes.getValue("id"));
            lastid = oldid;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equals("pupil")) {
            try {
                if ((name.equals(oldname) && (surname.equals(oldsurname)) && (age.equals(oldage)) && (level.equals(oldlevel)) && (oldschoolId == schoolId))) {
                    System.out.println(getPropertyValue("recordAlreadyExist"));
                    throw new RuntimeException();
                } else {
                    Element pupil = doc.createElement("pupil");
                    pupil.setAttribute("id", Long.toString(oldid));
                    Element name = doc.createElement("name");
                    name.setTextContent(oldname);
                    pupil.appendChild(name);
                    Element surname = doc.createElement("surname");
                    surname.setTextContent(oldsurname);
                    pupil.appendChild(surname);
                    Element age = doc.createElement("age");
                    age.setTextContent(oldage);
                    pupil.appendChild(age);
                    Element level = doc.createElement("level");
                    level.setTextContent(Byte.toString(oldlevel));
                    pupil.appendChild(level);
                    Element school = doc.createElement("schoolId");
                    school.setTextContent(Byte.toString(oldschoolId));
                    pupil.appendChild(school);
                    Element subjects = doc.createElement("subjects");
                    for (byte i = 0; i < oldsubjects.size(); i++) {
                        Element subject = doc.createElement("subject");
                        Element subjectName = doc.createElement("subjectname");
                        Element mark = doc.createElement("mark");
                        subjectName.setTextContent(oldsubjects.get(i));
                        mark.setTextContent(Byte.toString(oldmarks.get(i)));
                        subject.appendChild(subjectName);
                        subject.appendChild(mark);
                        subjects.appendChild(subject);
                    }
                    pupil.appendChild(subjects);
                    rootElement.appendChild(pupil);
                    oldid = 0;
                    oldname = null;
                    oldsurname = null;
                    oldlevel = 0;
                    oldage = null;
                    oldschoolId = 0;
                    oldmarks.clear();
                    oldsubjects.clear();
                }
            } catch (Exception e) {
                throw new SAXException();
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
        Element pupil = doc.createElement("pupil");
        lastid++;
        pupil.setAttribute("id", Long.toString(lastid));
        Element nname = doc.createElement("name");
        nname.setTextContent(name);
        pupil.appendChild(nname);
        Element ssurname = doc.createElement("surname");
        ssurname.setTextContent(surname);
        pupil.appendChild(ssurname);
        Element aage = doc.createElement("age");
        aage.setTextContent(age);
        pupil.appendChild(aage);
        Element llevel = doc.createElement("level");
        llevel.setTextContent(level);
        pupil.appendChild(llevel);
        Element school = doc.createElement("schoolId");
        school.setTextContent(Byte.toString(schoolId));
        pupil.appendChild(school);
        Element subjects = doc.createElement("subjects");
        for (byte i = 0; i < subjectNames.size(); i++) {
            Element subject = doc.createElement("subject");
            Element subjectName = doc.createElement("subjectname");
            Element mark = doc.createElement("mark");
            subjectName.setTextContent(subjectNames.get(i));
            mark.setTextContent(Byte.toString(subjectMarks.get(i)));
            subject.appendChild(subjectName);
            subject.appendChild(mark);
            subjects.appendChild(subject);
        }
        pupil.appendChild(subjects);
        rootElement.appendChild(pupil);
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
            System.out.println(getPropertyValue("success"));
        } catch (Exception e) {
            throw new SAXException();
        }
    }
}
