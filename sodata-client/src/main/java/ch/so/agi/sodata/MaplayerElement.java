package ch.so.agi.sodata;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.span;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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

import ch.so.agi.sodata.model.SimpleDataproduct;
import elemental2.dom.AbortController;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLElement;
import elemental2.dom.RequestInit;

public class MaplayerElement implements IsElement<HTMLElement> {
    private final HTMLElement root;
    
    private String dataBaseUrl;
    private SimpleDataproductMapper mapper;
    private List<SimpleDataproduct> mapLayers;
    private LocalListDataStore<SimpleDataproduct> listStore;
    private DataTable<SimpleDataproduct> mapTable;

    private HashMap<String, String> formatLookUp = new HashMap<String, String>();

    private AbortController abortController = null;

    public MaplayerElement(String dataBaseUrl) {
        root = div().element();
        
        this.dataBaseUrl = dataBaseUrl;
                
        // Create the json-mapper
        mapper = GWT.create(SimpleDataproductMapper.class);

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
//            console.log(mapLayers.size());
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
                listStore.setData(mapLayers);

                //removeQueryParam(FILTER_PARAM_KEY);
            }
        });
        textBox.addRightAddOn(resetIcon);

        textBox.addEventListener("keyup", event -> {
            if (textBox.getValue().trim().length() == 0) {
                listStore.setData(mapLayers);

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
                List<SimpleDataproduct> filteredDataproducts = mapper.read(json);

                Collections.sort(filteredDataproducts, new UmlautComparatorMaplayer());

                listStore.setData(filteredDataproducts);

                // if (ident == null) {
                // updateUrlLocation(FILTER_PARAM_KEY, textBox.getValue().trim());
                // }

                abortController = null;

                return null;
            }).catch_(error -> {
                console.log(error);
                return null;
            });
        });
        
        root.appendChild(textBox.element());
        
        TableConfig<SimpleDataproduct> tableConfig = new TableConfig<>();
        tableConfig
                .addColumn(ColumnConfig.<SimpleDataproduct>create("title", "Name")
                        .setShowTooltip(false)
                        .textAlign("left")
                        .setCellRenderer(cell -> TextNode.of(cell.getTableRow().getRecord().getTitle())))
                .addColumn(ColumnConfig.<SimpleDataproduct>create("theme", "Kartenthema")
                        .setShowTooltip(false)
                        .textAlign("left")
                        .setCellRenderer(cell -> TextNode.of(cell.getTableRow().getRecord().getLayerGroupDisplay())));

//                .addColumn(ColumnConfig.<SimpleDataproduct>create("model", "Datenmodell")
//                        .setShowTooltip(false)
//                        .textAlign("center")
//                        .setCellRenderer(cell -> {
//                            if (cell.getRecord().getModel() != null && cell.getRecord().getModel().length() > 0) {
//                                HTMLElement modelLinkElement = div()
//                                        .add(Icons.ALL.launch_mdi().style().setCursor("pointer"))
//                                        .element();
//                                HTMLElement modelLink = a().attr("class", "icon-link")
//                                        .attr("href",
//                                                "https://geo.so.ch/modelfinder/?expanded=true&query="
//                                                        + cell.getRecord().getModel())
//                                        .attr("target", "_blank")
//                                        .add(modelLinkElement)
//                                        .element();
//
//                                return modelLink;
//                            }
//                            return span().element();
//                        }))
//                .addColumn(ColumnConfig.<SimpleDataproduct>create("formats", "Daten herunterladen")
//                        .setShowTooltip(false)
//                        .textAlign("left")
//                        .setCellRenderer(cell -> {
//                            HTMLElement badgesElement = div().element();
//
//                            if (cell.getRecord().getSubunits() != null) {
//                                HTMLElement regionSelectionElement = a().css("default-link")
//                                        .textContent("Gebietsauswahl notwendig")
//                                        .attr("href", cell.getRecord().getSubunits())
//                                        .attr("target", "_blank")
//                                        .element();
//                                return regionSelectionElement;
//                            } else {
//                                for (String fileStr : cell.getRecord().getFileFormats()) {
//                                    badgesElement.appendChild(a().css("badge-link")
//                                            .attr("href",
//                                                    dataBaseUrl + cell.getRecord().getId() + "_" + fileStr + ".zip")
//                                            .attr("target", "_blank")
//                                            .add(Badge.create(formatLookUp.get(fileStr))
//                                                    .setBackground(Color.GREY_LIGHTEN_2)
//                                                    .style()
//                                                    .setMarginRight("10px")
//                                                    .setMarginTop("5px")
//                                                    .setMarginBottom("5px")
//                                                    .get()
//                                                    .element())
//                                            .element());
//                                }
//                                return badgesElement;
//                            }
//                        }));

        listStore = new LocalListDataStore<>();
        listStore.setData(mapLayers);

        mapTable = new DataTable<>(tableConfig, listStore);
        mapTable.css("dataset-table");
        //datasetTable.setId("dataset-table");
        mapTable.noStripes();
        mapTable.load();
        
        root.appendChild(mapTable.element());
    }
    
    
    @Override
    public HTMLElement element() {
        return root;
    }
    
    public static interface SimpleDataproductMapper extends ObjectMapper<List<SimpleDataproduct>> {
    }
}
