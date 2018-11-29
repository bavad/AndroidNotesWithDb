package team.corpore.in.lab1notes.data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Note implements Serializable {
    private int id;
    private String title;
    private String text;
    private String time;
    private ClassImportance classImportance;
    private String icon;

    public Note(int id) {
        this.id = id;
        this.classImportance = ClassImportance.FirstClass;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        String currentTime = sdf.format(new Date());
        this.time = currentTime;
    }

    public Note(int id, String title, String text, String time, ClassImportance classImportance, String icon) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.time = time;
        this.classImportance = classImportance;
        this.icon = icon;
    }

    public enum ClassImportance {
        FirstClass,
        SecondClass,
        ThirdClass;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public ClassImportance getClassImportance() {
        return classImportance;
    }

    public void setClassImportance(ClassImportance classImportance) {
        this.classImportance = classImportance;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
