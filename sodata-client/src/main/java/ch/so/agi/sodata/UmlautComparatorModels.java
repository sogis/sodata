package ch.so.agi.sodata;

import java.util.Comparator;

import ch.so.agi.sodata.model.ModelInfo;

public class UmlautComparatorModels implements Comparator<ModelInfo> {
    @Override
    public int compare(ModelInfo o1, ModelInfo o2) {
        String string0 = o1.getName().toLowerCase();
        String string1 = o2.getName().toLowerCase();
        return string0.compareTo(string1);
    }
}
