package ch.so.agi.sodata;

import org.jboss.elemento.IsElement;

import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.*;

public class MetadataElement implements IsElement<HTMLElement> {
    private MyMessages messages = GWT.create(MyMessages.class);

    private final HTMLElement root;

    public MetadataElement(Dataset dataset) {
        root = div().element();
        
        root.appendChild(h(4, messages.meta_description()).element());
        root.appendChild(p().css("meta-dataset-description-paragraph").textContent(dataset.getShortDescription()).element());
        
        if (dataset.getTables() != null) {
            root.appendChild(h(4, messages.meta_content()).element());

            HTMLElement tables = div().element();

            for (DatasetTable datasetTable : dataset.getTables()) {
                HTMLElement details = (HTMLElement) DomGlobal.document.createElement("details");
                details.className = "meta-details";
                HTMLElement summary = (HTMLElement) DomGlobal.document.createElement("summary");
                summary.className = "meta-summary";
                summary.textContent = datasetTable.getTitle();
                HTMLElement p = p().css("meta-table-description-paragraph").textContent(datasetTable.getDescription()).element();
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
