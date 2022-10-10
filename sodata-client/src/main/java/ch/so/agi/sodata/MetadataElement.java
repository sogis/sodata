package ch.so.agi.sodata;

import org.jboss.elemento.IsElement;

import com.google.gwt.core.client.GWT;

import ch.so.agi.sodata.dto.TableInfoDTO;
import ch.so.agi.sodata.dto.ThemePublicationDTO;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.*;

public class MetadataElement implements IsElement<HTMLElement> {
    private MyMessages messages = GWT.create(MyMessages.class);

    private final HTMLElement root;

    public MetadataElement(ThemePublicationDTO themePublication) {
        root = div().element();
        
        root.appendChild(h(4, messages.meta_description()).element());
        root.appendChild(p().css("meta-dataset-description-paragraph").textContent(themePublication.getShortDescription()).element());
        
        if (themePublication.getTablesInfo() != null) {
            root.appendChild(h(4, messages.meta_content()).element());

            HTMLElement tables = div().element();

            for (TableInfoDTO tableInfo : themePublication.getTablesInfo()) {
                HTMLElement details = (HTMLElement) DomGlobal.document.createElement("details");
                details.className = "meta-details";
                HTMLElement summary = (HTMLElement) DomGlobal.document.createElement("summary");
                summary.className = "meta-summary";
                summary.textContent = tableInfo.getTitle();
                HTMLElement p = p().css("meta-table-description-paragraph").textContent(tableInfo.getShortDescription()).element();
                details.appendChild(summary);
                details.appendChild(p);
                tables.appendChild(details);
            }

            root.appendChild(p().css("meta-tables-paragraph").add(tables).element()); 
        }
    }
     
    @Override
    public HTMLElement element() {
        return root;
    }
}
