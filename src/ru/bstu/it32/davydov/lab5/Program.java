package ru.bstu.it32.davydov.lab5;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.Scanner;

public class Program {

    protected static long edditingid;
    protected static Properties prop;
    protected static FileInputStream fin;
    protected static Connection con;
    protected static Scanner sin;
    protected static byte minAge;
    protected static byte maxAge;
    protected static boolean foundEdit = false;

    private static final String ERROR_READING = "Ошибка чтения файла properties!";

    // Получение значения из файла property
    protected static String getPropertyValue(String property) {
        String str = "";
        try {
            str = new String(prop.getProperty(property).getBytes("ISO8859-1"));
        } catch (Exception e) {
            System.out.println(ERROR_READING);
            safeExit();
        }
        return str;
    }

    // Выбор формата представления данных
    private static boolean  chooseDataFormat() {
        byte choice;
        String question = getPropertyValue("chooseDataFormat");
        while (true) {
            System.out.print(question);
            String answer = sin.nextLine();
            try {
                choice = Byte.parseByte(answer);
            } catch (Exception e) {
                continue;
            }
            if (choice == 0) {
                safeExit();
            }
            if (choice == 1) return true;
            if (choice == 2) return false;
        }
    }

    // Выбор действия
    private static byte chooseAction() {
        byte choice;
        String question = getPropertyValue("chooseAction");
        while (true) {
            System.out.print(question);
            String answer = sin.nextLine();
            try {
                choice = Byte.parseByte(answer);
            } catch (Exception e) {
                continue;
            }
            if ((choice >=0) && (choice <= 5)) {
                switch (choice) {
                    case 0: return 0;
                    case 1: return 1;
                    case 2: return 2;
                    case 3: return 3;
                    case 4: return 4;
                    case 5: return 5;
                }
            }
        }
    }

    // Закрытие всего перед выходом
    private static void safeExit() {
        sin.close();
        try {
            con.close();
        } catch (Exception e) {}
        try {
            fin.close();
        } catch (Exception ex) {}
        System.exit(-1);
    }

    // Получение массива учеников по известному запросу
    private static ArrayList<Pupil> getPupilsMas(PreparedStatement stat) {
        ArrayList<Pupil> masPupil = new ArrayList<Pupil>();
        try {
            ResultSet rs = stat.executeQuery();
            while (rs.next()) {
                long id = rs.getLong(1);
                boolean check = false;
                int indexPupil = -1;
                for (Pupil p: masPupil) {
                    indexPupil++;
                    if (id == p.id) {
                        check = true;
                        break;
                    }
                }
                if (!check) {
                    String name = rs.getString(2);
                    String surname = rs.getString(3);
                    byte age = rs.getByte(4);
                    byte level = rs.getByte(6);
                    String school = rs.getString(5);
                    String subject = rs.getString(7);
                    byte mark = rs.getByte(8);
                    Pupil p = new Pupil(id, name, surname, age, level, school, subject, mark);
                    masPupil.add(p);
                } else {
                    String subject = rs.getString(7);
                    byte mark = rs.getByte(8);
                    Pupil p = masPupil.get(indexPupil);
                    p.subjects.add(subject);
                    p.marks.add(mark);
                }
            }
        } catch (Exception e) {
            System.out.println(getPropertyValue("errorGettingRequestFromDB"));
        }
        return masPupil;
    }

    // Получение названия школы по id
    protected static String getSchoolName(byte id) throws SQLException {
        PreparedStatement stat = Program.con.prepareStatement("select name from Schools where id_school = ?");
        stat.setString(1, Byte.toString(id));
        ResultSet rs = stat.executeQuery();
        rs.next();
       return rs.getString(1);
    }

    protected static void pupilOut(Pupil p) {
        System.out.println("--------------------------------------------------------------------");
        System.out.println("id: " + p.id);
        System.out.println("Имя: " + p.name);
        System.out.println("Фамилия: " + p.surname);
        System.out.println("Возраст: " + p.age);
        System.out.println("Школа: " + p.school);
        System.out.println("Класс: " + p.level);
        System.out.println("Предмет - оценка:");
        for (int i = 0; i < p.subjects.size(); i++) {
            System.out.println("\t" + p.subjects.get(i) + " - " + p.marks.get(i));
        }
    }

    // Вывод всех учеников на экран
    private static void pupilsOutput(ArrayList<Pupil> masPupil) {
        for (Pupil p: masPupil) {
            pupilOut(p);
        }
        System.out.println("--------------------------------------------------------------------");
    }

    // Вывод всех записей непосредственно из БД
    private static void showRecordsFromDB () {
        try {
            PreparedStatement stat = con.prepareStatement(
                    "select PupilId,  ResTable.name, ResTable.surname, ResTable.age, Schools.name, ResTable.level, SubjectName, mark " +
                        "from Schools inner join( " +
                            "select *" +
                            "from Pupils inner join ( " +
                                "select Subjects.id_subject as SubjectId, mark, Pupils_Subjects.id_subject, Subjects.name as SubjectName, " +
                                    "Pupils_Subjects.id_pupil as PupilId " +
                                "from Subjects inner join Pupils_Subjects on Subjects.id_subject = Pupils_Subjects.id_subject " +
                            ") as SubjectResult on Pupils.id_pupil = SubjectResult.PupilId " +
                        ") as ResTable on Schools.id_school = ResTable.id_school");
            ArrayList<Pupil> masPupil = getPupilsMas(stat);
            pupilsOutput(masPupil);
        } catch (Exception e) {
            System.out.println(getPropertyValue("SQLrequestError"));
        }
    }

    // Поиск записей
    private static void searchRecordFromDB() {
        while (true) {
            System.out.println(getPropertyValue("searchSubject"));
            byte choice;
            String answer = sin.nextLine();
            try {
                choice = Byte.parseByte(answer);
            } catch (Exception e) {
                continue;
            }
            if ((choice >= 0) && (choice <= 4)) {
                PreparedStatement stat = null;
                ArrayList<Pupil> masPupil;
                switch (choice) {
                    case 0: return;
                    case 1: // по фамилии и имени
                        System.out.print("Имя: ");
                        String name = sin.nextLine();
                        name = name.trim();
                        System.out.print("Фамилия: ");
                        String surname = sin.nextLine();
                        surname = surname.trim();
                        try {
                            stat = con.prepareStatement("select PupilId,  ResTable.name, ResTable.surname, ResTable.age, Schools.name, ResTable.level, SubjectName, mark " +
                                                            "from Schools inner join( " +
                                                                "select *" +
                                                                "from Pupils inner join ( " +
                                                                    "select Subjects.id_subject as SubjectId, mark, Pupils_Subjects.id_subject, Subjects.name as SubjectName, " +
                                                                        "Pupils_Subjects.id_pupil as PupilId " +
                                                                    "from Subjects inner join Pupils_Subjects on Subjects.id_subject = Pupils_Subjects.id_subject " +
                                                                ") as SubjectResult on Pupils.id_pupil = SubjectResult.PupilId " +
                                                            ") as ResTable on Schools.id_school = ResTable.id_school " +
                                                            "where ResTable.name = ? and ResTable.surname = ?");
                            stat.setString(1, name);
                            stat.setString(2, surname);
                        } catch (Exception e) {
                            System.out.println(getPropertyValue("SQLrequestError"));
                        }
                        break;
                    case 2:
                        System.out.print("Название школы: ");
                        String school = sin.nextLine();
                        school = school.trim();
                        byte level;
                        while (true) {
                            System.out.print("Номер класса: ");
                            String str = sin.nextLine();
                            byte minLevel = Byte.parseByte(getPropertyValue("minLevel"));
                            byte maxLevel = Byte.parseByte(getPropertyValue("maxLevel"));
                           try {
                               level = Byte.parseByte(str);
                           } catch (Exception e) {
                               continue;
                           }
                           if ((level >= minLevel) && (level <= maxLevel)) break;
                        }
                        try {
                            stat = con.prepareStatement("select PupilId,  ResTable.name, ResTable.surname, ResTable.age, Schools.name, ResTable.level, SubjectName, mark " +
                                                            "from Schools inner join( " +
                                                                "select *" +
                                                                    "from Pupils inner join ( " +
                                                                        "select Subjects.id_subject as SubjectId, mark, Pupils_Subjects.id_subject, Subjects.name as SubjectName, " +
                                                                            "Pupils_Subjects.id_pupil as PupilId " +
                                                                        "from Subjects inner join Pupils_Subjects on Subjects.id_subject = Pupils_Subjects.id_subject " +
                                                                    ") as SubjectResult on Pupils.id_pupil = SubjectResult.PupilId " +
                                                            ") as ResTable on Schools.id_school = ResTable.id_school " +
                                                            "where Schools.name = ? and ResTable.level = ?");
                            stat.setString(1, school);
                            stat.setString(2, Byte.toString(level));
                        } catch (Exception e) {
                            System.out.println(getPropertyValue("SQLrequestError"));
                        }
                        break;
                    case 3:
                        try {
                            stat = con.prepareStatement("select PupilId,  ResTable.name, ResTable.surname, ResTable.age, Schools.name, ResTable.level, SubjectName, mark " +
                                    "from Schools inner join( " +
                                    "select *" +
                                    "from Pupils inner join ( " +
                                    "select Subjects.id_subject as SubjectId, mark, Pupils_Subjects.id_subject, Subjects.name as SubjectName, " +
                                    "Pupils_Subjects.id_pupil as PupilId " +
                                    "from Subjects inner join Pupils_Subjects on Subjects.id_subject = Pupils_Subjects.id_subject " +
                                    ") as SubjectResult on Pupils.id_pupil = SubjectResult.PupilId " +
                                    ") as ResTable on Schools.id_school = ResTable.id_school ");
                        } catch (Exception e) {
                            System.out.println(getPropertyValue("SQLrequestError"));
                        }
                        masPupil = getPupilsMas(stat);
                        masPupil = Pupil.sort(masPupil);
                        byte minAge = masPupil.get(0).age;
                        for (int i = 0; i < masPupil.size(); i++) {
                            if (minAge == masPupil.get(i).age) {
                                Pupil p = masPupil.get(i);
                                System.out.println("--------------------------------------------------------------------");
                                System.out.println("id: " + p.id);
                                System.out.println("Имя: " + p.name);
                                System.out.println("Фамилия: " + p.surname);
                                System.out.println("Возраст: " + p.age);
                                System.out.println("Класс: " + p.level);
                                System.out.println("Школа: " + p.school);
                                System.out.println("Предмет - оценка:");
                                for (int j = 0; j < p.subjects.size(); j++) {
                                    System.out.println("\t" + p.subjects.get(j) + " - " + p.marks.get(j));
                                }
                            } else break;
                        }
                        System.out.println("--------------------------------------------------------------------");
                        break;
                    case 4:
                        try {
                            stat = con.prepareStatement("select PupilId,  ResTable.name, ResTable.surname, ResTable.age, Schools.name, ResTable.level, SubjectName, mark " +
                                    "from Schools inner join( " +
                                    "select *" +
                                    "from Pupils inner join ( " +
                                    "select Subjects.id_subject as SubjectId, mark, Pupils_Subjects.id_subject, Subjects.name as SubjectName, " +
                                    "Pupils_Subjects.id_pupil as PupilId " +
                                    "from Subjects inner join Pupils_Subjects on Subjects.id_subject = Pupils_Subjects.id_subject " +
                                    ") as SubjectResult on Pupils.id_pupil = SubjectResult.PupilId " +
                                    ") as ResTable on Schools.id_school = ResTable.id_school ");
                        } catch (Exception e) {
                            System.out.println(getPropertyValue("SQLrequestError"));
                        }
                        masPupil = getPupilsMas(stat);
                        masPupil = Pupil.sort(masPupil);
                        Collections.reverse(masPupil);
                        byte maxAge = masPupil.get(0).age;
                        for (int i = 0; i < masPupil.size(); i++) {
                            if (maxAge == masPupil.get(i).age) {
                                Pupil p = masPupil.get(i);
                                System.out.println("--------------------------------------------------------------------");
                                System.out.println("id: " + p.id);
                                System.out.println("Имя: " + p.name);
                                System.out.println("Фамилия: " + p.surname);
                                System.out.println("Возраст: " + p.age);
                                System.out.println("Класс: " + p.level);
                                System.out.println("Школа: " + p.school);
                                System.out.println("Предмет - оценка:");
                                for (int j = 0; j < p.subjects.size(); j++) {
                                    System.out.println("\t" + p.subjects.get(j) + " - " + p.marks.get(j));
                                }
                            } else break;
                        }
                        System.out.println("--------------------------------------------------------------------");
                        break;
                }
                if ((choice == 1) || (choice == 2)) {
                    masPupil = getPupilsMas(stat);
                    if (masPupil.size() == 0) { // записей не найдено
                        System.out.println(getPropertyValue("noRecords"));
                    } else {
                        pupilsOutput(masPupil);
                    }
                }
            }
        }
    }

    // Удаление записи непосредственно из БД
    private static void removeRecordFromDB() {
        while (true) {
            System.out.print(getPropertyValue("chooseRecordForRemoving"));
            long id;
            String answer = sin.nextLine();
            try {
                id = Long.parseLong(answer);
            } catch (Exception e) {
                continue;
            }
            if (id >= 0) {
                if (id == 0) return;
                try {
                    PreparedStatement stat = con.prepareStatement("select count(id_pupil), id_pupil " +
                                                                    "from Pupils " +
                                                                    "where id_pupil =  " + id);
                    ResultSet rs = stat.executeQuery();
                    rs.next();
                    if (rs.getLong(1) == 0 ) { // запись не найдена
                        System.out.println(getPropertyValue("noPupils"));
                    } else {
                        stat = con.prepareStatement("delete from Pupils where id_pupil  =  " + id);
                        stat.executeUpdate();
                        System.out.println(getPropertyValue("success"));
                        return;
                    }
                } catch (Exception e) {
                    System.out.println(getPropertyValue("errorGettingRequestFromDB"));
                    return;
                }
            }
        }
    }

    // Добавление записи непосредственно в БД
    private static void addRecordToDB() {
        String name;
        while (true) {
            System.out.print("Имя (0 - отмена добавления записи): ");
            name = sin.nextLine();
            if (name.equals("0")) {
                return;
            }
            if (name.length() != 0) break;
        }
        name = name.trim();
        String surname;
        while (true) {
            System.out.print("Фамилия (0 - отмена добавления записи): ");
            surname = sin.nextLine();
            if (surname.equals("0")) {
                return;
            }
            if (surname.length() != 0) break;
        }
        surname = surname.trim();
        String age;
        while (true) {
            System.out.print("Возраст (0 - отмена добавления записи): ");
            age = sin.nextLine();
            if (age.equals("0")) {
                return;
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
        String level;
        while (true) {
            System.out.print("Класс (0 - отмена добавления записи): ");
            level = sin.nextLine();
            if (level.equals("0")) {
                return;
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
        byte schoolId;
        String schoolName;
        while (true) {
            System.out.println("Школа (0 - отмена добавления записи): ");
            try {
                PreparedStatement stat = con.prepareStatement("select name from Schools");
                ResultSet rs = stat.executeQuery();
                byte i = 0;
                while (rs.next()) {
                    i++;
                    System.out.println("\t" + i + " - " + rs.getString(1));
                }
                schoolName = sin.nextLine();
                if (schoolName.equals("0")) {
                    return;
                } else {

                    try {
                        schoolId = Byte.parseByte(schoolName);
                    } catch (Exception e) {
                        continue;
                    }
                    if ((schoolId >= 0) && (schoolId <= i)) {
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println(getPropertyValue("SQLrequestError"));
                return;
            }
        }
        // Проверка есть ли данный ученик уже в БД
        try {
            PreparedStatement stat = con.prepareStatement("select count(*) " +
                                            "from Pupils " +
                                            "where name = ? and surname = ? and age = ? and id_school = ? and level = ?");
            stat.setString(1, name);
            stat.setString(2, surname);
            stat.setString(3, age);
            stat.setString(4, Byte.toString(schoolId));
            stat.setString(5, level);
            ResultSet rs = stat.executeQuery();
            rs.next();
            if (rs.getLong(1) > 0) {
                System.out.println(getPropertyValue("recordAlreadyExist"));
                return;
            }
        } catch (Exception e) {
            System.out.println(getPropertyValue("SQLrequestError"));
            return;
        }
        byte subjectsCount;
        PreparedStatement stat;
        try {
            stat = con.prepareStatement("select count(*) " +
                    "from Subjects ");
            ResultSet rs = stat.executeQuery();
            rs.next();
            subjectsCount = rs.getByte(1);
        } catch (Exception e) {
            System.out.println(getPropertyValue("errorAdding"));
            return;
        }
        String[] subjectNames = new String[subjectsCount];
        try {
            stat = con.prepareStatement("select name " +
                    "from Subjects ");
            ResultSet rs = stat.executeQuery();
            int i = 0;
            while (rs.next()) {
                subjectNames[i] = rs.getString(1);
                i++;
            }
        } catch (Exception e) {
            System.out.println(getPropertyValue("errorAdding"));
            return;
        }
        byte[] subjectMarks = new byte[subjectsCount];
        for (byte i = 0; i < subjectsCount; i++) {
            byte mark;
            while (true) {
                System.out.print("[ " + (i + 1) + " ] Оценка по '" + subjectNames[i] + "' (0 - отмена добавления записи): ");
                String mmark = sin.nextLine();
                if (mmark.equals("0")) {
                    return;
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
            subjectMarks[i] = mark;
        }
        try {
            stat = con.prepareStatement("insert into Pupils (name, surname, age, level, id_school) " +
                                                        "values (?, ?, ?, ?, ?)");
            stat.setString(1, name);
            stat.setString(2, surname);
            stat.setString(3, age);
            stat.setString(5, Byte.toString(schoolId));
            stat.setString(4, level);
            stat.executeUpdate();
            stat = con.prepareStatement("select id_pupil " +
                    "from Pupils " +
                    "where name = ? and surname = ? and age = ? and id_school = ? and level = ?");
            stat.setString(1, name);
            stat.setString(2, surname);
            stat.setString(3, age);
            stat.setString(4, Byte.toString(schoolId));
            stat.setString(5, level);
            ResultSet rs = stat.executeQuery();
            rs.next();
            long pupilId = rs.getLong(1);
            for (byte i = 0; i < subjectsCount; i++) {
                stat = con.prepareStatement("select id_subject from Subjects where name = ?");
                stat.setString(1, subjectNames[i]);
                rs = stat.executeQuery();
                rs.next();
                byte subjectId = rs.getByte(1);
                stat = con.prepareStatement("insert into Pupils_Subjects (id_pupil, id_subject, mark) " +
                        "values (?, ?, ?)");
                stat.setString(1, Long.toString(pupilId));
                stat.setString(2, Byte.toString(subjectId));
                stat.setString(3, Byte.toString(subjectMarks[i]));
                stat.executeUpdate();
            }
        } catch (Exception e) {
            System.out.println(getPropertyValue("errorAdding"));
            return;
        }
        System.out.println(getPropertyValue("success"));
    }

    // Изменение записи непосредственно через БД
    private static void editRecordFromDB() {
        while (true) {
            System.out.print(getPropertyValue("chooseRecordForEditing"));
            long id;
            String answer = sin.nextLine();
            try {
                id = Long.parseLong(answer);
            } catch (Exception e) {
                continue;
            }
            if (id >= 0) {
                if (id == 0) return;
                try {
                    PreparedStatement stat = con.prepareStatement("select count(id_pupil), id_pupil " +
                            "from Pupils " +
                            "where id_pupil =  " + id);
                    ResultSet rs = stat.executeQuery();
                    rs.next();
                    if (rs.getLong(1) == 0 ) { // запись не найдена
                        System.out.println(getPropertyValue("noPupils"));
                    } else {
                        // Получение ученика
                        stat = con.prepareStatement("select PupilId,  ResTable.name, ResTable.surname, ResTable.age, Schools.name, ResTable.level, SubjectName, mark " +
                                "from Schools inner join( " +
                                "select *" +
                                "from Pupils inner join ( " +
                                "select Subjects.id_subject as SubjectId, mark, Pupils_Subjects.id_subject, Subjects.name as SubjectName, " +
                                "Pupils_Subjects.id_pupil as PupilId " +
                                "from Subjects inner join Pupils_Subjects on Subjects.id_subject = Pupils_Subjects.id_subject " +
                                ") as SubjectResult on Pupils.id_pupil = SubjectResult.PupilId " +
                                ") as ResTable on Schools.id_school = ResTable.id_school " +
                                "where PupilId = " + id);
                        ArrayList<Pupil> mas = getPupilsMas(stat);
                        long PupilId = mas.get(0).id;
                        String action;
                        String newName = "";
                        // Изменение имени
                        while (true) {
                            System.out.print("Изменить имя? да/нет (0 - отмена изменения записи): ");
                            action = sin.nextLine();
                            action.trim();
                            if (action.equals("0")) {
                                return;
                            }
                            if (action.equals("да")) {
                                while (true) {
                                    System.out.print("Новое имя (0 - отмена изменения записи): ");
                                    newName = sin.nextLine();
                                    newName.trim();
                                    if (newName.equals("0")) {
                                        return;
                                    }
                                    if (newName.length() != 0) break;
                                }
                                break;
                            }
                            if (action.equals("нет")) break;
                        }
                        String newSurname = "";
                        // Изменение фамилии
                        while (true) {
                            System.out.print("Изменить фамилию? да/нет (0 - отмена изменения записи): ");
                            action = sin.nextLine();
                            action.trim();
                            if (action.equals("0")) {
                                return;
                            }
                            if (action.equals("да")) {
                                while (true) {
                                    System.out.print("Новая фамилия (0 - отмена изменения записи): ");
                                    newSurname = sin.nextLine();
                                    newSurname.trim();
                                    if (newSurname.equals("0")) {
                                        return;
                                    }
                                    if (newSurname.length() != 0) break;
                                }
                                break;
                            }
                            if (action.equals("нет")) break;
                        }
                        byte newAge = 0;
                        // Изменение возраста
                        while (true) {
                            System.out.print("Изменить возраст? да/нет (0 - отмена изменения записи): ");
                            action = sin.nextLine();
                            action.trim();
                            if (action.equals("0")) {
                                return;
                            }
                            if (action.equals("да")) {
                                while (true) {
                                    System.out.print("Новый возраст (0 - отмена изменения записи): ");
                                    action = sin.nextLine();
                                    action.trim();
                                    if (action.equals("0")) {
                                        return;
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
                        byte newLevel = 0;
                        // Изменение класса
                        while (true) {
                            System.out.print("Изменить класс? да/нет (0 - отмена изменения записи): ");
                            action = sin.nextLine();
                            action.trim();
                            if (action.equals("0")) {
                                return;
                            }
                            if (action.equals("да")) {
                                while (true) {
                                    System.out.print("Новый номер класса (0 - отмена изменения записи): ");
                                    action = sin.nextLine();
                                    action.trim();
                                    if (action.equals("0")) {
                                        return;
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
                        // Изменение школы
                        byte newSchoolId = 0;
                        while (true) {
                            System.out.print("Изменить школу? да/нет (0 - отмена изменения записи): ");
                            action = sin.nextLine();
                            action.trim();
                            if (action.equals("0")) {
                                return;
                            }
                            if (action.equals("да")) {
                                while (true) {
                                    System.out.println("Новая школа (0 - отмена изменения записи): ");
                                    stat = con.prepareStatement("select name from Schools");
                                    rs = stat.executeQuery();
                                    int i = 0;
                                    while (rs.next()) {
                                        i++;
                                        System.out.println("\t" + i + " - " + rs.getString(1));
                                    }
                                    action = sin.nextLine();
                                    action.trim();
                                    if (action.equals("0")) {
                                        return;
                                    } else {
                                        try {
                                            newSchoolId = Byte.parseByte(action);
                                        } catch (Exception e) {
                                            continue;
                                        }
                                        if ((newSchoolId >= 0) && (newSchoolId <= i)) break;
                                    }
                                }
                                break;
                            }
                            if (action.equals("нет")) break;
                        }
                        // Обновление таблиц БД
                        if (!newName.equals("")) {
                            stat = con.prepareStatement("update Pupils set name = ? where id_pupil = " + PupilId);
                            stat.setString(1, newName);
                            stat.executeUpdate();
                        }
                        if (!newSurname.equals("")) {
                            stat = con.prepareStatement("update Pupils set surname = ? where id_pupil = " + PupilId);
                            stat.setString(1, newSurname);
                            stat.executeUpdate();
                        }
                        if (newAge != 0) {
                            stat = con.prepareStatement("update Pupils set age = ? where id_pupil = " + PupilId);
                            stat.setString(1, Byte.toString(newAge));
                            stat.executeUpdate();
                        }
                        if (newLevel != 0) {
                            stat = con.prepareStatement("update Pupils set level = ? where id_pupil = " + PupilId);
                            stat.setString(1, Byte.toString(newLevel));
                            stat.executeUpdate();
                        }
                        if (newSchoolId != 0) {
                            stat = con.prepareStatement("update Pupils set id_school = ? where id_pupil = " + PupilId);
                            stat.setString(1, Byte.toString(newSchoolId));
                            stat.executeUpdate();
                        }
                        System.out.println(getPropertyValue("success"));
                        return;
                    }
                } catch (Exception e) {
                    System.out.println(getPropertyValue("errorGettingRequestFromDB"));
                    return;
                }
            }
        }
    }

    // Преобразование БД в XML
    private static StreamResult convertDBToXML() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        StreamResult file = null;
        try {
            PreparedStatement stat = con.prepareStatement(
                    "select PupilId,  ResTable.name, ResTable.surname, ResTable.age, Schools.name, ResTable.level, SubjectName, mark " +
                            "from Schools inner join( " +
                            "select *" +
                            "from Pupils inner join ( " +
                            "select Subjects.id_subject as SubjectId, mark, Pupils_Subjects.id_subject, Subjects.name as SubjectName, " +
                            "Pupils_Subjects.id_pupil as PupilId " +
                            "from Subjects inner join Pupils_Subjects on Subjects.id_subject = Pupils_Subjects.id_subject " +
                            ") as SubjectResult on Pupils.id_pupil = SubjectResult.PupilId " +
                            ") as ResTable on Schools.id_school = ResTable.id_school");
            ArrayList<Pupil> masPupil = getPupilsMas(stat);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element rootElement = doc.createElement("pupils");
            doc.appendChild(rootElement);
            for (Pupil p: masPupil) {
                Element pupil = doc.createElement("pupil");
                pupil.setAttribute("id", Long.toString(p.id));
                Element name = doc.createElement("name");
                name.setTextContent(p.name);
                pupil.appendChild(name);
                Element surname = doc.createElement("surname");
                surname.setTextContent(p.surname);
                pupil.appendChild(surname);
                Element age = doc.createElement("age");
                age.setTextContent(Byte.toString(p.age));
                pupil.appendChild(age);
                Element level = doc.createElement("level");
                level.setTextContent(Byte.toString(p.level));
                pupil.appendChild(level);
                Element school = doc.createElement("schoolId");
                stat = con.prepareStatement("select id_school from Pupils where id_pupil = " + p.id);
                ResultSet rs = stat.executeQuery();
                rs.next();
                byte schoolId = rs.getByte(1);
                school.setTextContent(Byte.toString(schoolId));
                pupil.appendChild(school);
                Element subjects = doc.createElement("subjects");
                for (byte i = 0; i < p.subjects.size(); i++) {
                    Element subject = doc.createElement("subject");
                    Element subjectName = doc.createElement("subjectname");
                    Element mark = doc.createElement("mark");
                    subjectName.setTextContent(p.subjects.get(i));
                    mark.setTextContent(Byte.toString(p.marks.get(i)));
                    subject.appendChild(subjectName);
                    subject.appendChild(mark);
                    subjects.appendChild(subject);
                }
                pupil.appendChild(subjects);
                rootElement.appendChild(pupil);
            }
            doc.getDocumentElement().normalize();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(doc);
            file = new StreamResult(new File(getPropertyValue("XMLpath")));
            transformer.transform(source, file);
        } catch (Exception e) {
            System.out.println(getPropertyValue("errorXMLCreation"));
        }
        return file;
    }

    // Вывод записей из XML
    private static void showRecordsFromXML() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            XMLOutput handler = new XMLOutput();
            parser.parse(new File(getPropertyValue("XMLpath")), handler);
            System.out.println("--------------------------------------------------------------------");
        } catch (Exception e) {
            System.out.println(getPropertyValue("errorXMLReading"));
        }
    }

    // Конвертация xml в бд
    private static void convertXMLToDB() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLSaveToDB handler = new XMLSaveToDB();
            PreparedStatement stat = con.prepareStatement("delete from Pupils");
            stat.executeUpdate();
            parser.parse(new File(getPropertyValue("XMLpath")), handler);
        } catch (Exception e) {
            System.out.println(getPropertyValue("errorXMLToDBconverting"));
        }
    }

    // Удаление записи из xml
    private static void removeRecordFromXML() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLRemoving handler = new XMLRemoving();
            parser.parse(new File(getPropertyValue("XMLpath")), handler);
        } catch (RuntimeException e) {
            return;
        } catch (Exception e) {
            System.out.println(getPropertyValue("errorXMLRemoving"));
        }
    }

    // Изменение записи в XML
    private static void editRecordInXML() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            while(true) {
                System.out.print(getPropertyValue("chooseRecordForEditing"));
                String answer = sin.nextLine();
                try {
                    edditingid = Long.parseLong(answer);
                    if (edditingid >= 0) break;
                    break;
                } catch (Exception e) {
                    continue;
                }
            }
            XMLSearchEdditingPupil hand = new XMLSearchEdditingPupil();
            parser.parse(new File(getPropertyValue("XMLpath")), hand);
            if (!foundEdit) {
                System.out.println(getPropertyValue("noPupils"));
                return;
            }
            XMLEdditing handler = new XMLEdditing();
            parser.parse(new File(getPropertyValue("XMLpath")), handler);
        } catch (RuntimeException e) {
            return;
        } catch (Exception e) {
            System.out.println(getPropertyValue("errorXMLEdditing"));
        }
    }

    // Добавление записи в XML
    private static void addRecordToXML() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLAdding handler = new XMLAdding();
            parser.parse(new File(getPropertyValue("XMLpath")), handler);

        } catch (RuntimeException e) {
            return;
        } catch (Exception e) {
            System.out.println(getPropertyValue("errorXMLAdding"));
        }
    }

    // Поиск записи в xml
    public static void searchRecordInXML() {
        while (true) {
            System.out.println(getPropertyValue("searchSubject"));
            byte choice;
            String answer = sin.nextLine();
            try {
                choice = Byte.parseByte(answer);
            } catch (Exception e) {
                continue;
            }
            if ((choice >= 0) && (choice <= 4)) {
                switch (choice) {
                    case 0: return;
                    case 1: // по фамилии и имени
                        try {
                            SAXParserFactory factory = SAXParserFactory.newInstance();
                            SAXParser parser = factory.newSAXParser();
                            XMLSearchNameSurname handler = new XMLSearchNameSurname();
                            parser.parse(new File(getPropertyValue("XMLpath")), handler);

                        } catch (RuntimeException e) {
                            return;
                        } catch (Exception e) {
                            System.out.println(getPropertyValue("errorXMLSearching"));
                        }
                        break;
                    case 2:
                        try {
                            SAXParserFactory factory = SAXParserFactory.newInstance();
                            SAXParser parser = factory.newSAXParser();
                            XMLSearchSchoolLevel handler = new XMLSearchSchoolLevel();
                            parser.parse(new File(getPropertyValue("XMLpath")), handler);
                        } catch (RuntimeException e) {
                            return;
                        } catch (Exception e) {
                            System.out.println(getPropertyValue("errorXMLSearching"));
                        }
                        break;
                    case 3:
                        try {
                            SAXParserFactory factory = SAXParserFactory.newInstance();
                            SAXParser parser = factory.newSAXParser();
                            minAge = Byte.parseByte(getPropertyValue("maxAge"));
                            maxAge = Byte.parseByte(getPropertyValue("minAge"));
                            XMLAgeMinMax handler = new XMLAgeMinMax();
                            parser.parse(new File(getPropertyValue("XMLpath")), handler);
                            XMLAgeMin handlerr = new XMLAgeMin();
                            parser.parse(new File(getPropertyValue("XMLpath")), handlerr);
                        } catch (RuntimeException e) {
                            return;
                        } catch (Exception e) {
                            System.out.println(getPropertyValue("errorXMLSearching"));
                        }
                        break;
                    case 4:
                        try {
                            SAXParserFactory factory = SAXParserFactory.newInstance();
                            SAXParser parser = factory.newSAXParser();
                            XMLAgeMinMax handler = new XMLAgeMinMax();
                            minAge = Byte.parseByte(getPropertyValue("maxAge"));
                            maxAge = Byte.parseByte(getPropertyValue("minAge"));
                            parser.parse(new File(getPropertyValue("XMLpath")), handler);
                            XMLAgeMax handlerr = new XMLAgeMax();
                            parser.parse(new File(getPropertyValue("XMLpath")), handlerr);
                        } catch (RuntimeException e) {
                            return;
                        } catch (Exception e) {
                            System.out.println(getPropertyValue("errorXMLSearching"));
                        }
                        break;
                }
            }
        }
    }

    public static void main(String[] args) {
        sin = new Scanner(System.in);
        // Подключение к файлу properties
        prop = new Properties();
        try {
            fin = new FileInputStream(args[0]);
            prop.load(fin);
        } catch (Exception e) {
            System.out.println("Ошибка! Не найден файл properties!");
            sin.close();
            return;
        }
        // Регистрация драйвера
//        try {
//            Class.forName("com.mysql.jdbc.Driver");
//        } catch (Exception e) {
//            System.out.println(getPropertyValue("errorDriverRegistration"));
//            safeExit();
//        }
        // Подключение к БД
        try {
            String port = getPropertyValue("DBport");
            String name = getPropertyValue("DBname");
            String user = getPropertyValue("DBuser");
            String password = getPropertyValue("DBpassword");
            con = DriverManager.getConnection("jdbc:mysql://localhost:" + port + "/" + name + "?serverTimezone=Europe/Moscow&useUnicode=true&characterEncoding=utf8 ", user, password);
        } catch (Exception e) {
            try {
                System.out.println(getPropertyValue("errorConnectionToDB"));
            } catch (Exception ex) { }
            safeExit();
        }
        while (true) {
            if(chooseDataFormat()) { // работа  с xml
                StreamResult file = convertDBToXML();
                while (true) {
                    byte action;
                    if (file == null) {
                        System.out.println(getPropertyValue("errorXMLCreation"));
                        break;
                    }
                    action = chooseAction();
                    boolean again = false;
                    switch (action) {
                        case 0: // выход к выбору формата представления или выходу
                            again = true;
                            break;
                        case 1:
                            showRecordsFromXML();
                            break;
                        case 2:
                            addRecordToXML();
                            break;
                        case 3:
                            editRecordInXML();
                            break;
                        case 4:
                            removeRecordFromXML();
                            break;
                        case 5:
                            searchRecordInXML();
                            break;
                    }
                    if (again) break;
                }
                convertXMLToDB();
            } else { // работа с бд
                while (true) {
                    byte action = chooseAction();
                    boolean again = false;
                    switch (action) {
                        case 0: // выход к выбору формата представления или выходу
                            again = true;
                            break;
                        case 1: // вывод всех записей
                            showRecordsFromDB();
                            break;
                        case 2: // добавление записи
                            addRecordToDB();
                            break;
                        case 3: // изменение записи
                            editRecordFromDB();
                            break;
                        case 4: // удаление записи
                            removeRecordFromDB();
                            break;
                        case 5: // поиск записей
                            searchRecordFromDB();
                            break;
                    }
                    if (again) break;
                }
            }
        }
    }
}
