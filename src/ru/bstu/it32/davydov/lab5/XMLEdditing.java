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

class XMLEdditing extends DefaultHandler {

    private String newName = "";
    private String newSurname = "";
    private boolean eddited = false;
    private byte newAge = 0;
    private byte newLevel = 0;
    private byte newSchoolId = 0;
    private String name;
    private String surname;
    private byte age = 0;
    private byte level = 0;
    private byte schoolId = 0;
    private String lastElementName;
    private long id;
    private long edditingid;
    private ArrayList<Byte> marks = new ArrayList<Byte>();
    private ArrayList<String> subjects = new ArrayList<String>();
    Document doc;
    Element rootElement;

    protected XMLEdditing() throws RuntimeException, SAXException {
        while (true) {
            if (Program.edditingid == 0) throw new RuntimeException();
            try {
                // Изменение имени
                String action;
                while (true) {
                    System.out.print("Изменить имя? да/нет (0 - отмена изменения записи): ");
                    action = sin.nextLine();
                    action.trim();
                    if (action.equals("0")) {
                        throw new RuntimeException();
                    }
                    if (action.equals("да")) {
                        while (true) {
                            System.out.print("Новое имя (0 - отмена изменения записи): ");
                            newName = sin.nextLine();
                            newName.trim();
                            if (newName.equals("0")) {
                                throw new RuntimeException();
                            }
                            if (newName.length() != 0) break;
                        }
                        break;
                    }
                    if (action.equals("нет")) break;
                }
                // Изменение фамилии
                while (true) {
                    System.out.print("Изменить фамилию? да/нет (0 - отмена изменения записи): ");
                    action = sin.nextLine();
                    action.trim();
                    if (action.equals("0")) {
                        throw new RuntimeException();
                    }
                    if (action.equals("да")) {
                        while (true) {
                            System.out.print("Новая фамилия (0 - отмена изменения записи): ");
                            newSurname = sin.nextLine();
                            newSurname.trim();
                            if (newSurname.equals("0")) {
                                throw new RuntimeException();
                            }
                            if (newSurname.length() != 0) break;
                        }
                        break;
                    }
                    if (action.equals("нет")) break;
                }
                // Изменение возраста
                while (true) {
                    System.out.print("Изменить возраст? да/нет (0 - отмена изменения записи): ");
                    action = sin.nextLine();
                    action.trim();
                    if (action.equals("0")) {
                        throw new RuntimeException();
                    }
                    if (action.equals("да")) {
                        while (true) {
                            System.out.print("Новый возраст (0 - отмена изменения записи): ");
                            action = sin.nextLine();
                            action.trim();
                            if (action.equals("0")) {
                                throw new RuntimeException();
                            }
                            byte minAge = Byte.parseByte(getPropertyValue("minAge"));
                            byte maxAge = Byte.parseByte(getPropertyValue("maxAge"));
                            try {
                                newAge = Byte.parseByte(action);
                            } catch (Exception e) {
                                continue;
                            }
                            if ((newAge >= minAge) && (newAge <= maxAge)) break;
                        }
                        break;
                    }
                    if (action.equals("нет")) break;
                }
                // Изменение класса
                while (true) {
                    System.out.print("Изменить класс? да/нет (0 - отмена изменения записи): ");
                    action = sin.nextLine();
                    action.trim();
                    if (action.equals("0")) {
                        throw new RuntimeException();
                    }
                    if (action.equals("да")) {
                        while (true) {
                            System.out.print("Новый номер класса (0 - отмена изменения записи): ");
                            action = sin.nextLine();
                            action.trim();
                            if (action.equals("0")) {
                                throw new RuntimeException();
                            }
                            byte minLevel = Byte.parseByte(getPropertyValue("minLevel"));
                            byte maxLevel = Byte.parseByte(getPropertyValue("maxLevel"));
                            try {
                                newLevel = Byte.parseByte(action);
                            } catch (Exception e) {
                                continue;
                            }
                            if ((newLevel >= minLevel) && (newLevel <= maxLevel)) break;
                        }
                        break;
                    }
                    if (action.equals("нет")) break;
                }
                PreparedStatement stat;
                ResultSet rs;
                // Изменение школы
                while (true) {
                    System.out.print("Изменить школу? да/нет (0 - отмена изменения записи): ");
                    action = sin.nextLine();
                    action.trim();
                    if (action.equals("0")) {
                        throw new RuntimeException();
                    }
                    if (action.equals("да")) {
                        while (true) {
                            int i = 0;
                            try {
                                System.out.println("Новая школа (0 - отмена изменения записи): ");
                                stat = con.prepareStatement("select name from Schools");
                                rs = stat.executeQuery();
                                while (rs.next()) {
                                    i++;
                                    System.out.println("\t" + i + " - " + rs.getString(1));
                                }
                            } catch (Exception e) {
                                throw new SAXException();
                            }
                            action = sin.nextLine();
                            action.trim();
                            if (action.equals("0")) {
                                throw new RuntimeException();
                            } else {
                                try {
                                    newSchoolId = Byte.parseByte(action);
                                } catch (Exception e) {
                                    throw new RuntimeException();
                                }
                                if ((newSchoolId >= 0) && (newSchoolId <= i)) break;
                            }
                        }
                        break;
                    }
                    if (action.equals("нет")) break;
                }
                break;
            } catch (Exception e) {
                System.out.println(getPropertyValue("errorGettingRequestFromDB"));
                return;
            }
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
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

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equals("pupil")) {
            try {
                if (id == Program.edditingid) {
                    eddited = true;
                    Element pupil = doc.createElement("pupil");
                    pupil.setAttribute("id", Long.toString(id));
                    Element nname = doc.createElement("name");
                    if (newName.equals("")) {
                        nname.setTextContent(name);
                    } else {
                        nname.setTextContent(newName);
                    }
                    pupil.appendChild(nname);
                    Element ssurname = doc.createElement("surname");
                    if (newSurname.equals("")) {
                        ssurname.setTextContent(surname);
                    } else {
                        ssurname.setTextContent(newSurname);
                    }
                    pupil.appendChild(ssurname);
                    Element aage = doc.createElement("age");
                    if (newAge == 0) {
                        aage.setTextContent(Byte.toString(age));
                    } else {
                        aage.setTextContent(Byte.toString(newAge));
                    }
                    pupil.appendChild(aage);
                    Element llevel = doc.createElement("level");
                    if (newLevel == 0) {
                        llevel.setTextContent(Byte.toString(level));
                    } else {
                        llevel.setTextContent(Byte.toString(newLevel));
                    }
                    pupil.appendChild(llevel);
                    Element school = doc.createElement("schoolId");
                    if (newSchoolId == 0) {
                        school.setTextContent(Byte.toString(schoolId));
                    } else {
                        school.setTextContent(Byte.toString(newSchoolId));
                    }
                    pupil.appendChild(school);
                    Element ssubjects = doc.createElement("subjects");
                    for (byte i = 0; i < subjects.size(); i++) {
                        Element ssubject = doc.createElement("subject");
                        Element subjectName = doc.createElement("subjectname");
                        Element mark = doc.createElement("mark");
                        subjectName.setTextContent(subjects.get(i));
                        mark.setTextContent(Byte.toString(marks.get(i)));
                        ssubject.appendChild(subjectName);
                        ssubject.appendChild(mark);
                        ssubjects.appendChild(ssubject);
                    }
                    pupil.appendChild(ssubjects);
                    rootElement.appendChild(pupil);
                } else {
                    Element pupil = doc.createElement("pupil");
                    pupil.setAttribute("id", Long.toString(id));
                    Element nname = doc.createElement("name");
                    nname.setTextContent(name);
                    pupil.appendChild(nname);
                    Element ssurname = doc.createElement("surname");
                    ssurname.setTextContent(surname);
                    pupil.appendChild(ssurname);
                    Element aage = doc.createElement("age");
                    aage.setTextContent(Byte.toString(age));
                    pupil.appendChild(aage);
                    Element llevel = doc.createElement("level");
                    llevel.setTextContent(Byte.toString(level));
                    pupil.appendChild(llevel);
                    Element school = doc.createElement("schoolId");
                    school.setTextContent(Byte.toString(schoolId));
                    pupil.appendChild(school);
                    Element ssubjects = doc.createElement("subjects");
                    for (byte i = 0; i < subjects.size(); i++) {
                        Element ssubject = doc.createElement("subject");
                        Element subjectName = doc.createElement("subjectname");
                        Element mark = doc.createElement("mark");
                        subjectName.setTextContent(subjects.get(i));
                        mark.setTextContent(Byte.toString(marks.get(i)));
                        ssubject.appendChild(subjectName);
                        ssubject.appendChild(mark);
                        ssubjects.appendChild(ssubject);
                    }
                    pupil.appendChild(ssubjects);
                    rootElement.appendChild(pupil);
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
