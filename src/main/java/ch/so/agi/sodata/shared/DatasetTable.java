package ch.so.agi.sodata.shared;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative=true, namespace= JsPackage.GLOBAL, name="Object")
public final class DatasetTable {
    private String title;
    private String description;

    @JsOverlay
    public String getTitle() {
        return title;
    }

    @JsOverlay
    public void setTitle(String title) {
        this.title = title;
    }

    @JsOverlay
    public String getDescription() {
        return description;
    }

    @JsOverlay
    public void setDescription(String description) {
        this.description = description;
    }
}
