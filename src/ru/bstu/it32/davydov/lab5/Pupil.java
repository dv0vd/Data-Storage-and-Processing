package ru.bstu.it32.davydov.lab5;

import java.util.ArrayList;

class Pupil {

    protected long id;
    protected String name;
    protected String surname;
    protected byte age;
    protected byte level;
    protected String school;
    protected ArrayList<String> subjects = new ArrayList<String>();
    protected ArrayList<Byte> marks = new ArrayList<Byte>();

    Pupil (long id, String name, String surname, byte age, byte level, String school, String subject, byte mark){
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.level = level;
        this.school = school;
        this.subjects.add(subject);
        this.marks.add(mark);
    }

    protected static ArrayList<Pupil> sort(ArrayList<Pupil> arrayList) {
        for (int i = 0; i < arrayList.size() - 1; i++) {
            for (int j = i; j < arrayList.size(); j++) {
                if (arrayList.get(i).age > arrayList.get(j).age) {
                    Pupil temp = arrayList.get(j);
                    arrayList.set(j, arrayList.get(i));
                    arrayList.set(i, temp);
                }
            }
        }
        return arrayList;
    }
}
