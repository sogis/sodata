package ch.so.agi.sodata;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dominokit.domino.ui.button.Button;
import org.dominokit.domino.ui.datatable.DataTable;
import org.dominokit.domino.ui.datatable.store.LocalListDataStore;
import org.dominokit.domino.ui.forms.TextBox;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.modals.ModalDialog;
import org.dominokit.domino.ui.style.Color;
import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.jboss.elemento.IsElement;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.EventListener;

import ch.so.agi.sodata.DataTabElement.ThemePublicationMapper;
import ch.so.agi.sodata.dto.DataproductDTO;
import ch.so.agi.sodata.dto.ThemePublicationDTO;
import elemental2.dom.AbortController;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.HTMLTableElement;
import elemental2.dom.HTMLTableRowElement;
import elemental2.dom.HTMLTableSectionElement;
import elemental2.dom.RequestInit;


public class MapLayerTabElement implements IsElement<HTMLElement> {
    private final HTMLElement root;

    // i18n
    private MyMessages messages;
    
    private DataproductMapper mapper;
    private List<DataproductDTO> mapLayers;
//    private LocalListDataStore<ThemePublicationDTO> themePublicationListStore;
    //private DataTable<DataproductMapperDTO> themePublicationTable;
    
    private HTMLTableElement rootTable = table().element();

    private AbortController abortController = null;

    public static interface DataproductMapper extends ObjectMapper<List<DataproductDTO>> {
    }
    
    public MapLayerTabElement(MyMessages messages) {
        root = div().element();
        
        this.messages = messages;

        // Mapper for mapping server json response to objects
        mapper = GWT.create(DataproductMapper.class);
        
        // Get themes publications json from server for first time table initialization.
        DomGlobal.fetch("/maplayers").then(response -> {
            if (!response.ok) {
                DomGlobal.window.alert(response.statusText + ": " + response.body);
                return null;
            }
            return response.text();
        }).then(json -> {
            mapLayers = mapper.read(json);
            Collections.sort(mapLayers, new DataproductComparator());
            
            //init();
            renderTable(mapLayers);

            return null;
        }).catch_(error -> {
            console.log(error);
            DomGlobal.window.alert(error.toString());
            return null;
        });

        
        
        root.appendChild(rootTable);                
    }
    
    private void renderTable(List<DataproductDTO> mapLayers) {
        rootTable.id = "maps-table";

        rootTable.appendChild(colgroup()
                .add(col().attr("span", "1").style("width: 2%"))
                .add(col().attr("span", "1").style("width: 2%"))
                .add(col().attr("span", "1").style("width: 53%"))
                .add(col().attr("span", "1").style("width: 30%"))
                .add(col().attr("span", "1").style("width: 6%"))
//                .add(col().attr("span", "1").style("width: 7%"))
                .add(col().attr("span", "1").style("width: 7%"))
                .element());
        HTMLTableSectionElement mapsTableHead = thead()
                .add(tr()
                        .add(th().add(""))
                        .add(th().attr("colspan", "2").add(messages.map_table_header_maplayer()))
                        .add(th().add(messages.map_table_header_topic())) // Können mehrere sein....
                        .add(th().add(messages.map_table_header_description()))
//                        .add(th().add("Vorschau"))
                        .add(th().add(messages.map_table_header_view())))
                .element();
        rootTable.appendChild(mapsTableHead);
        
        for (DataproductDTO dataproduct : mapLayers) {
            HTMLTableSectionElement tbodyParent = tbody().element();
            
            HTMLTableRowElement tr = tr().element();
            HTMLTableCellElement tdSublayerIcon = null;
            
            // Title
            if (dataproduct.getSublayers() != null) {
                HTMLElement sublayersIcon = Icons.ALL.plus_mdi().style().setCursor("pointer").get().element();
                tdSublayerIcon = td().add(sublayersIcon).element();
                tr.appendChild(tdSublayerIcon);
            } else {
                HTMLElement layerIcon = Icons.ALL.layers_outline_mdi().style().setCursor("pointer").get().element();
                tr.appendChild(td().add(layerIcon).element());
            }
            
            HTMLTableCellElement tdParentTitle = td().attr("colspan", "2").add(dataproduct.getTitle()).element();
            tr.appendChild(tdParentTitle);
            
            // Thema
            tr.appendChild(td().add(dataproduct.getThemeTitle()!=null?dataproduct.getThemeTitle():"").element());
            
            HTMLElement metadataLinkElement = div()
                    .add(Icons.ALL.information_outline_mdi().style().setCursor("pointer"))
                    .element();
            metadataLinkElement.addEventListener("click", new elemental2.dom.EventListener() {
                @Override
                public void handleEvent(Event evt) {
                    openMetadataDialog(dataproduct.getTheAbstract());
                }
            });

            tr.appendChild(td().attr("align", "center").add(metadataLinkElement).element());
            
            // Preview
//            if (dataproduct.getSublayers() == null && !dataproduct.getTitle().contains("Quelle geodienste.ch") && !dataproduct.getTitle().contains("Quelle Bund")) {
//                HTMLElement previewLinkElement = div()
//                        .add(Icons.ALL.eye_outline_mdi().style().setCursor("pointer"))
//                        .element();
//                previewLinkElement.addEventListener("click", new elemental2.dom.EventListener() {
//                    @Override
//                    public void handleEvent(Event evt) {
//                        //openPreviewDialog(dataproduct);
//                    }
//                });
//                tr.appendChild(td().attr("align", "center").add(previewLinkElement).element());
//            } else {
//                tr.appendChild(td().attr("align", "center").add("—").element());
//            }
                        
            // Web GIS Client
            String layerString;
            if (dataproduct.getSublayers() != null) {
                List<String> sublayers = new ArrayList<String>();
                for (DataproductDTO sublayer : dataproduct.getSublayers()) {
                    sublayers.add(createLayerString(sublayer));
                }
                Collections.reverse(sublayers);
                layerString = String.join(",", sublayers);
            } else {
                layerString = createLayerString(dataproduct);
            }
            
            HTMLElement wgcLinkElement = div()
                    .add(Icons.ALL.launch_mdi().style().setCursor("pointer"))
                    .element();
            HTMLElement wgcLink = a().attr("class", "icon-link")
                    .attr("href",
                            "https://geo.so.ch/map/?l="
                                    + layerString)
                    .attr("target", "_blank")
                    .add(wgcLinkElement)
                    .element();

            tr.appendChild(td().attr("align", "center").add(wgcLink).element());
            
            tbodyParent.appendChild(tr);
            rootTable.appendChild(tbodyParent);
            
            if (dataproduct.getSublayers() != null) {
                HTMLTableSectionElement tbodyChildren = tbody().css("hide").element();
                for (DataproductDTO sublayer : dataproduct.getSublayers()) {
                    HTMLElement layerIcon = Icons.ALL.layers_outline_mdi().style().setCursor("pointer").get().element();
                    
                    HTMLTableRowElement trSublayer = tr().element();
                    
                    // Title
                    trSublayer.appendChild(td().add("").element());
                    trSublayer.appendChild((td().add(layerIcon)).element());
                    trSublayer.appendChild(td().add(sublayer.getTitle()).element());
                    
                    // Theme
                    trSublayer.appendChild(td().add(sublayer.getThemeTitle()!=null?sublayer.getThemeTitle():"").element());
                    
                    // Metadata
                    HTMLElement sublayerMetadataLinkElement = div()
                            .add(Icons.ALL.information_outline_mdi().style().setCursor("pointer"))
                            .element();
                    sublayerMetadataLinkElement.addEventListener("click", new elemental2.dom.EventListener() {
                        @Override
                        public void handleEvent(Event evt) {
                            openMetadataDialog(sublayer.getTheAbstract());
                        }
                    });

                    trSublayer.appendChild(td().attr("align", "center").add(sublayerMetadataLinkElement).element());
                    
//                    // Preview
//                    if (!sublayer.getTitle().contains("Quelle geodienste.ch") && !sublayer.getTitle().contains("Quelle Bund")) {
//                        HTMLElement sublayerPreviewLinkElement = div()
//                                .add(Icons.ALL.eye_outline_mdi().style().setCursor("pointer"))
//                                .element();
//                        sublayerPreviewLinkElement.addEventListener("click", new elemental2.dom.EventListener() {
//                            @Override
//                            public void handleEvent(Event evt) {
//                                //openPreviewDialog(sublayer);
//                            }
//                        });
//                        trSublayer.appendChild(td().attr("align", "center").add(sublayerPreviewLinkElement).element()); 
//                    } else {
//                        trSublayer.appendChild(td().attr("align", "center").add("—").element()); 
//                    }

                    // Web GIS Client
                    HTMLElement sublayerWgcLinkElement = div()
                            .add(Icons.ALL.launch_mdi().style().setCursor("pointer"))
                            .element();
                    HTMLElement sublayerWgcLink = a().attr("class", "icon-link")
                            .attr("href",
                                    "https://geo.so.ch/map/?l="
                                            + sublayer.getIdent())
                            .attr("target", "_blank")
                            .add(sublayerWgcLinkElement)
                            .element();
                    
                    trSublayer.appendChild(td().attr("align", "center").add(sublayerWgcLink).element()); 

                    tbodyChildren.appendChild(trSublayer);
                }
                rootTable.appendChild(tbodyChildren);
                
                tdSublayerIcon.addEventListener("click", new elemental2.dom.EventListener() {
                    @Override
                    public void handleEvent(Event evt) {
                        tbodyChildren.classList.toggle("hide");
                    }
                });
                
                tdParentTitle.addEventListener("click", new elemental2.dom.EventListener() {
                    @Override
                    public void handleEvent(Event evt) {
                        tbodyChildren.classList.toggle("hide");
                    }
                });
            }
        }  
    }
    
    private void openMetadataDialog(String theAbstract) {
        ModalDialog modal = ModalDialog.create(messages.meta_description()).setAutoClose(true);
        modal.css("modal-object");
        
        String fixedAbstract = theAbstract.replace("a href", "a class=\"default-link\" href");
        modal.add(div().innerHtml(SafeHtmlUtils.fromTrustedString(fixedAbstract)));
                
        Button closeButton = Button.create(messages.close().toUpperCase()).linkify();
        closeButton.removeWaves();
        closeButton.setBackground(Color.RED_DARKEN_3);
        elemental2.dom.EventListener closeModalListener = (evt) -> modal.close();
        closeButton.addClickListener(closeModalListener);
        modal.appendFooterChild(closeButton);
        modal.open();

        closeButton.blur();
    }
    
    private String createLayerString(DataproductDTO dataproduct) {
        return dataproduct.getIdent() + "[" + String.valueOf(100 - dataproduct.getOpacity()*100/255) + "]" + (dataproduct.isVisibility()?"":"!");
    }
    
    private void removeResults() {
        rootTable.getElementsByTagName("tbody");
        
        while(rootTable.firstChild != null) {
            rootTable.removeChild(rootTable.firstChild);
        }        
    }

    public void updateTable(String queryString) {
        if (abortController != null) {
            abortController.abort();
        }

        abortController = new AbortController();
        final RequestInit init = RequestInit.create();
        init.setSignal(abortController.signal);

        DomGlobal.fetch("/maplayers?query=" + queryString.toLowerCase(), init).then(response -> {
            if (!response.ok) {
                return null;
            }
            return response.text();
        }).then(json -> {
            List<DataproductDTO> filteredMapLayers = mapper.read(json);
            filteredMapLayers.sort(new DataproductComparator());

            this.removeResults();
            this.renderTable(filteredMapLayers);
            
            abortController = null;

            return null;
        }).catch_(error -> {
            console.log(error);
            return null;
        });
    }
    
    public void resetTable() {
        removeResults();
        renderTable(mapLayers);
    }

    @Override
    public HTMLElement element() {
        return root;
    }

}
