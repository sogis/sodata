package ch.so.agi.sodata;

import java.util.Comparator;

public class UmlautComparator implements Comparator<Dataset> {
    @Override
    public int compare(Dataset o1, Dataset o2) {
        String string0 = o1.getTitle().toLowerCase();
        String string1 = o2.getTitle().toLowerCase();
        string0 = string0.replace("ä", "a");
        string0 = string0.replace("ö", "o");
        string0 = string0.replace("ü", "u");
        string1 = string1.replace("ä", "a");
        string1 = string1.replace("ö", "o");
        string1 = string1.replace("ü", "u");
        return string0.compareTo(string1);
    }
}
