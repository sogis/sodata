package ch.so.agi.sodata;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dominokit.domino.ui.badges.Badge;
import org.dominokit.domino.ui.datatable.ColumnConfig;
import org.dominokit.domino.ui.datatable.DataTable;
import org.dominokit.domino.ui.datatable.TableConfig;
import org.dominokit.domino.ui.datatable.store.LocalListDataStore;
import org.dominokit.domino.ui.forms.TextBox;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.utils.TextNode;
import org.jboss.elemento.IsElement;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;

import ch.so.agi.sodata.model.Dataproduct;
import elemental2.dom.AbortController;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.HTMLTableElement;
import elemental2.dom.HTMLTableRowElement;
import elemental2.dom.HTMLTableSectionElement;
import elemental2.dom.Node;
import elemental2.dom.RequestInit;

public class MaplayerElement implements IsElement<HTMLElement> {
    private final HTMLElement root;
    
    private String dataBaseUrl;
    private DataproductMapper mapper;
    private List<Dataproduct> mapLayers;
    private List<Dataproduct> sortedMapLayers;
//    private List<GroupedDataproduct> groupedMapLayers;
//    List<GroupedDataproduct> listGroupedDataproduct;
    private LocalListDataStore<Dataproduct> listStore;
    private DataTable<Dataproduct> mapTable;

    private HashMap<String, String> formatLookUp = new HashMap<String, String>();

    private AbortController abortController = null;

    private HTMLTableElement rootTable = table().element();
    private HTMLTableSectionElement mapsTableBody = tbody().element();

    public MaplayerElement(String dataBaseUrl) {
        root = div().element();
        
        this.dataBaseUrl = dataBaseUrl;
                
        // Create the json-mapper
        mapper = GWT.create(DataproductMapper.class);

        // Get all datasets for initialize the table.
        DomGlobal.fetch("/maplayers")
        .then(response -> {
            if (!response.ok) {
                DomGlobal.window.alert(response.statusText + ": " + response.body);
                return null;
            }
            return response.text();
        })
        .then(json -> {
            mapLayers = mapper.read(json);
            Collections.sort(mapLayers, new UmlautComparatorMaplayer());            
            init();
            return null;
        }).catch_(error -> {
            console.log(error);
            DomGlobal.window.alert(error.toString());
            return null;
        }); 
    }

    private void init() {
        TextBox textBox = TextBox.create().setLabel("Suchbegriff");
        textBox.css("filter-text-box");
        textBox.addLeftAddOn(Icons.ALL.search());
        textBox.setFocusColor(Color.RED_DARKEN_3);
        textBox.getInputElement().setAttribute("autocomplete", "off");
        textBox.getInputElement().setAttribute("spellcheck", "false");

        HTMLElement resetIcon = Icons.ALL.close().style().setCursor("pointer").get().element();
        resetIcon.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                textBox.clear();
                
                Collections.sort(mapLayers, new UmlautComparatorMaplayer());

                //removeQueryParam(FILTER_PARAM_KEY);
            }
        });
        textBox.addRightAddOn(resetIcon);

        textBox.addEventListener("keyup", event -> {
            if (textBox.getValue().trim().length() == 0) {
//                listStore.setData(mapLayers);

                //removeQueryParam(FILTER_PARAM_KEY);

                return;
            }

            if (abortController != null) {
                abortController.abort();
            }

            abortController = new AbortController();
            final RequestInit init = RequestInit.create();
            init.setSignal(abortController.signal);

            DomGlobal.fetch("/maplayers?query=" + textBox.getValue().toLowerCase(), init)
            .then(response -> {
                if (!response.ok) {
                    return null;
                }
                return response.text();
            }).then(json -> {
                mapLayers = mapper.read(json);

                Collections.sort(mapLayers, new UmlautComparatorMaplayer());
                
                // TODO 
                removeResults();
                
                
                //TODO
                for (Dataproduct dataproduct : mapLayers) {
                    mapsTableBody
                            .appendChild(
                                    tr().add(td().add(dataproduct.getTitle())).add(td().add("")).element());
                }
                

                abortController = null;

                return null;
            }).catch_(error -> {
                console.log(error);
                return null;
            });
        });
        
        root.appendChild(textBox.element());
        
        
        // TODO die Tabelle muss früher initialisert werden
        //HTMLTableElement rootTable = table().element();
        rootTable.id = "maps-table";

        rootTable.appendChild(colgroup()
                .add(col().attr("span", "1").style("width: 2%"))
                .add(col().attr("span", "1").style("width: 2%"))
                .add(col().attr("span", "1").style("width: 56%"))
                .add(col().attr("span", "1").style("width: 20%"))
                .add(col().attr("span", "1").style("width: 6%"))
                .add(col().attr("span", "1").style("width: 7%"))
                .add(col().attr("span", "1").style("width: 7%"))
                .element());
        HTMLTableSectionElement mapsTableHead = thead()
                .add(tr()
                        .add(th().add(""))
                        .add(th().attr("colspan", "2").add("Kartenebene"))
                        .add(th().add("Thema")) // Können mehrere sein....
                        .add(th().add("Beschreibung"))
                        .add(th().add("Vorschau"))
                        .add(th().add("Absprung")))
                .element();
        rootTable.appendChild(mapsTableHead);

        //HTMLTableSectionElement mapsTableBody = tbody().element();
        //mapsTableBody.id = "maps-table-body";
        
        for (Dataproduct dataproduct : mapLayers) {
            HTMLTableSectionElement tbodyParent = tbody().element();
            
//            tbodyParent
//                    .appendChild(
//                            tr().add(td().add("+")).add(td().add(dataproduct.getTitle())).add(td().add("")).element());
            
            HTMLTableRowElement tr = tr().element();
            if (dataproduct.getSublayers() != null) {
                HTMLElement sublayersIcon = Icons.ALL.plus_mdi().style().setCursor("pointer").get().element();
                tr.appendChild(td().add(sublayersIcon).element());
            } else {
                HTMLElement layerIcon = Icons.ALL.layers_outline_mdi().style().setCursor("pointer").get().element();
                tr.appendChild(td().add(layerIcon).element());
            }
            
            tr.appendChild(td().attr("colspan", "2").add(dataproduct.getTitle()).element());
            tr.appendChild(td().add("").element());
            
            tbodyParent.appendChild(tr);
            rootTable.appendChild(tbodyParent);
            
            if (dataproduct.getSublayers() != null) {
                HTMLTableSectionElement tbodyChildren = tbody().css("hide").element();
                for (Dataproduct sublayer : dataproduct.getSublayers()) {
                    HTMLElement layerIcon = Icons.ALL.layers_outline_mdi().style().setCursor("pointer").get().element();                    
                    tbodyChildren
                        .appendChild(
                            tr().add(td().add("")).add(td().add(layerIcon)).add(td().add(sublayer.getTitle())).add(td().add("")).element());
                }
                rootTable.appendChild(tbodyChildren);
                
                tbodyParent.addEventListener("click", new EventListener() {
                    @Override
                    public void handleEvent(Event evt) {
                        tbodyChildren.classList.toggle("hide");
                    }
                });
            }
        }

        root.appendChild(rootTable);        
    }
    
    private void removeResults() {
        while(mapsTableBody.firstChild != null) {
            mapsTableBody.removeChild(mapsTableBody.firstChild);
        }        
    }

    @Override
    public HTMLElement element() {
        return root;
    }
    
    public static interface DataproductMapper extends ObjectMapper<List<Dataproduct>> {
    }
}
