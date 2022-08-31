package ch.so.agi.sodata;

import com.google.gwt.i18n.client.Messages;

public interface MyMessages extends Messages {
    // Generic
    @DefaultMessage("Close")
    String close();
    
    // Search
    @DefaultMessage("Search terms")
    String search_terms();
    
    // Table
    @DefaultMessage("Topic")
    String table_header_topic();
    
    @DefaultMessage("Publication date")
    String table_header_publication_date();

    @DefaultMessage("Metadata")
    String table_header_metadata();

    @DefaultMessage("Download data")
    String table_header_data_download();
    
    @DefaultMessage("Subselection necessary")
    String table_subunit_selection(); 
    
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
