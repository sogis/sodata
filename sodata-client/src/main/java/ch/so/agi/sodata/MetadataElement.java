package ch.so.agi.sodata;

import org.jboss.elemento.IsElement;

import com.google.gwt.core.client.GWT;

import ch.so.agi.sodata.dto.TableInfoDTO;
import ch.so.agi.sodata.dto.ThemePublicationDTO;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.*;

import org.dominokit.domino.ui.badges.Badge;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.style.Color;
import org.gwtproject.safehtml.shared.SafeHtmlUtils;

public class MetadataElement implements IsElement<HTMLElement> {
    private MyMessages messages = GWT.create(MyMessages.class);

    private final HTMLElement root;

    public MetadataElement(ThemePublicationDTO themePublication, String filesServerUrl, MyMessages messages) {
        root = div().element();
        
        root.appendChild(h(4, messages.meta_description()).element());
        
        root.appendChild(p().css("meta-dataset-description-paragraph").innerHtml(SafeHtmlUtils.fromTrustedString(themePublication.getShortDescription())).element());
        
        if (themePublication.getTablesInfo() != null) {
            root.appendChild(h(4, messages.meta_content()).element());

            HTMLElement tables = div().element();

            for (TableInfoDTO tableInfo : themePublication.getTablesInfo()) {
                HTMLElement details = (HTMLElement) DomGlobal.document.createElement("details");
                details.className = "meta-details";
                HTMLElement summary = (HTMLElement) DomGlobal.document.createElement("summary");
                summary.className = "meta-summary";
                summary.textContent = tableInfo.getTitle();
                                               
                HTMLElement p = p().css("meta-table-description-paragraph")
                        .add(div().css("meta-table")
                                .add(div().style("font-style: italic;")
                                        .textContent(messages.meta_details_p_header_table() + ": "))
                                .add(div().textContent(tableInfo.getSqlName()))
                                .add(div().style("font-style: italic;")
                                        .textContent(messages.meta_details_p_header_description() + ": "))
                                .add(div().innerHtml(SafeHtmlUtils.fromTrustedString(tableInfo.getShortDescription()))))
                        .element();               

                details.appendChild(summary);
                details.appendChild(p);
                tables.appendChild(details);
            }

            root.appendChild(p().css("meta-tables-paragraph").add(tables).element()); 
        }

        root.appendChild(h(4, messages.meta_complete_meta()).element());

        String fileUrl = filesServerUrl + "/" + themePublication.getIdentifier()
        + "/aktuell/meta/datenbeschreibung.html";

        root.appendChild(p().css("meta-dataset-description-paragraph")
                .add(a().css("default-link")
                        .attr("href", fileUrl)
                        .attr("target", "_blank")
                        .textContent(messages.meta_complete_meta_datasheet() + " ").add(Icons.ALL.launch_mdi().size18().style().setCursor("pointer"))
                        .element())
                .element());
    }
     
    @Override
    public HTMLElement element() {
        return root;
    }
}
