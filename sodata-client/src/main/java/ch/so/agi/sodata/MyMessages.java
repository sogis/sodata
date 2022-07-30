package ch.so.agi.sodata;

import com.google.gwt.i18n.client.Messages;

public interface MyMessages extends Messages {
    // Generic
    @DefaultMessage("Close")
    String close();
    
    
    // Meta
    @DefaultMessage("Description")
    String meta_description();
    
    @DefaultMessage("Content")
    String meta_content();

    
    
    
    @DefaultMessage("Fubar")
    String fubar();
    
    @DefaultMessage("Füü {0} bar {1}")
    String yinyang(String yin, String yang);
}
