package ru.bstu.it32.davydov.lab5;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import static ru.bstu.it32.davydov.lab5.Program.con;
import static ru.bstu.it32.davydov.lab5.Program.getSchoolName;

class XMLSaveToDB extends DefaultHandler {

    private long id = 0;
    private String name = null;
    private String surname = null;
    private byte age = 0;
    private byte level = 0;
    private byte schoolId = 0;
    private ArrayList<Byte> marks = new ArrayList<Byte>();
    private ArrayList<String> subjects = new ArrayList<String>();
    private String lastElementName = "";

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        lastElementName = qName;
        if (qName.equals("pupil")) {
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
                String schoolName =getSchoolName(schoolId);
                Pupil p = new Pupil(id, name, surname, age, level, schoolName, subjects.get(0), marks.get(0));
                for (byte i = 1; i < subjects.size(); i++) {
                    p.subjects.add(subjects.get(i));
                    p.marks.add(marks.get(i));
                }
                PreparedStatement stat = con.prepareStatement("select count(*) " +
                        "from Pupils " +
                        "where name = ? and surname = ? and age = ? and id_school = ? and level = ?");
                stat.setString(1, name);
                stat.setString(2, surname);
                stat.setString(3, Byte.toString(age));
                stat.setString(4, Byte.toString(schoolId));
                stat.setString(5, Byte.toString(level));
                ResultSet rs = stat.executeQuery();
                rs.next();
                if (rs.getLong(1) != 0) { // ученик уже существует
                  return;
                }
                stat = con.prepareStatement("insert into Pupils (name, surname, age, level, id_school) " +
                        "values (?, ?, ?, ?, ?)");
                stat.setString(1, p.name);
                stat.setString(2, p.surname);
                stat.setString(3, Byte.toString(p.age));
                stat.setString(5, Byte.toString(schoolId));
                stat.setString(4, Byte.toString(p.level));
                stat.executeUpdate();
                stat = con.prepareStatement("select id_pupil " +
                        "from Pupils " +
                        "where name = ? and surname = ? and age = ? and id_school = ? and level = ?");
                stat.setString(1, name);
                stat.setString(2, surname);
                stat.setString(3, Byte.toString(p.age));
                stat.setString(4, Byte.toString(schoolId));
                stat.setString(5, Byte.toString(p.level));
                rs = stat.executeQuery();
                rs.next();
                long pupilId = rs.getLong(1);
                for (byte i = 0; i < subjects.size(); i++) {
                    stat = con.prepareStatement("select id_subject from Subjects where name = ?");
                    stat.setString(1, subjects.get(i));
                    rs = stat.executeQuery();
                    rs.next();
                    byte subjectId = rs.getByte(1);
                    stat = con.prepareStatement("insert into Pupils_Subjects (id_pupil, id_subject, mark) " +
                            "values (?, ?, ?)");
                    stat.setString(1, Long.toString(pupilId));
                    stat.setString(2, Byte.toString(subjectId));
                    stat.setString(3, Byte.toString(marks.get(i)));
                    stat.executeUpdate();
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
}
