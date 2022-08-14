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
import org.dominokit.domino.ui.button.Button;
import org.dominokit.domino.ui.datatable.ColumnConfig;
import org.dominokit.domino.ui.datatable.DataTable;
import org.dominokit.domino.ui.datatable.TableConfig;
import org.dominokit.domino.ui.datatable.store.LocalListDataStore;
import org.dominokit.domino.ui.forms.TextBox;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.modals.ModalDialog;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.utils.TextNode;
import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.jboss.elemento.IsElement;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;

import ch.so.agi.sodata.App.MapSingleClickListener;
import ch.so.agi.sodata.model.Dataproduct;
import elemental2.dom.AbortController;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.HTMLTableElement;
import elemental2.dom.HTMLTableRowElement;
import elemental2.dom.HTMLTableSectionElement;
import elemental2.dom.Node;
import elemental2.dom.RequestInit;
import ol.OLFactory;
import ol.layer.Image;
import ol.layer.LayerOptions;
import ol.source.ImageWms;
import ol.source.ImageWmsOptions;
import ol.source.ImageWmsParams;

public class MaplayerElement implements IsElement<HTMLElement> {
    private final HTMLElement root;
    
    private String dataBaseUrl;
    private DataproductMapper mapper;
    private List<Dataproduct> mapLayers;
    private List<Dataproduct> completeMapLayers;

    private HTMLElement resetIcon;
    
    private AbortController abortController = null;

    private HTMLTableElement rootTable = table().element();
//    private HTMLTableSectionElement mapsTableBody = tbody().element();

    private String MAP_DIV_ID = "maplayers-map";
    private ol.Map map;

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
            completeMapLayers = mapLayers;
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

        resetIcon = Icons.ALL.close().style().setCursor("pointer").get().element();
        resetIcon.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                textBox.clear();
                
                Collections.sort(mapLayers, new UmlautComparatorMaplayer());

                removeResults();

                updatingResultTableElement(completeMapLayers);

                resetIcon.style.setProperty("color", "rgba(0, 0, 0, 0.54)");

                //removeQueryParam(FILTER_PARAM_KEY);
            }
        });
        textBox.addRightAddOn(resetIcon);

        textBox.addEventListener("keyup", event -> {
            if (textBox.getValue().trim().length() == 0) {
                removeResults();
                updatingResultTableElement(completeMapLayers);
                resetIcon.style.setProperty("color", "rgba(0, 0, 0, 0.54)");
                return;
            }
            
            resetIcon.style.setProperty("color", "#c62828");

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
                
                removeResults();
                
                updatingResultTableElement(mapLayers);
                                
                abortController = null;

                return null;
            }).catch_(error -> {
                console.log(error);
                return null;
            });
        });
        
        root.appendChild(textBox.element());
        
        updatingResultTableElement(mapLayers);
        
        root.appendChild(rootTable);        
    }
    
    
    private void updatingResultTableElement(List<Dataproduct> mapLayers) {
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
        
        for (Dataproduct dataproduct : mapLayers) {
            HTMLTableSectionElement tbodyParent = tbody().element();
            
            HTMLTableRowElement tr = tr().element();
            HTMLTableCellElement tdSublayerIcon = null;
            
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
            tr.appendChild(td().add("").element()); // TODO Thema
            
            HTMLElement metadataLinkElement = div()
                    .add(Icons.ALL.information_outline_mdi().style().setCursor("pointer"))
                    .element();
            metadataLinkElement.addEventListener("click", new EventListener() {
                @Override
                public void handleEvent(Event evt) {
                    openMetadataDialog(dataproduct.getTheAbstract());
                }
            });

            tr.appendChild(td().attr("align", "center").add(metadataLinkElement).element());
            
            // Preview
            if (dataproduct.getSublayers() == null && !dataproduct.getTitle().contains("Quelle geodienste.ch") && !dataproduct.getTitle().contains("Quelle Bund")) {
                HTMLElement previewLinkElement = div()
                        .add(Icons.ALL.eye_outline_mdi().style().setCursor("pointer"))
                        .element();
                previewLinkElement.addEventListener("click", new EventListener() {
                    @Override
                    public void handleEvent(Event evt) {
                        openPreviewDialog(dataproduct);
                    }
                });
                tr.appendChild(td().attr("align", "center").add(previewLinkElement).element());
            } else {
                tr.appendChild(td().attr("align", "center").add("—").element());
            }
                        
            // Web GIS Client
            HTMLElement wgcLinkElement = div()
                    .add(Icons.ALL.launch_mdi().style().setCursor("pointer"))
                    .element();
            HTMLElement wgcLink = a().attr("class", "icon-link")
                    .attr("href",
                            "https://geo.so.ch/map/?l="
                                    + dataproduct.getIdent())
                    .attr("target", "_blank")
                    .add(wgcLinkElement)
                    .element();

            tr.appendChild(td().attr("align", "center").add(wgcLink).element());
            
            tbodyParent.appendChild(tr);
            rootTable.appendChild(tbodyParent);
            
            if (dataproduct.getSublayers() != null) {
                HTMLTableSectionElement tbodyChildren = tbody().css("hide").element();
                for (Dataproduct sublayer : dataproduct.getSublayers()) {
                    HTMLElement layerIcon = Icons.ALL.layers_outline_mdi().style().setCursor("pointer").get().element();
                    
                    HTMLTableRowElement trSublayer = tr().add(td().add("")).add(td().add(layerIcon)).add(td().add(sublayer.getTitle())).add(td().add("")).element();
                    
                    HTMLElement sublayerMetadataLinkElement = div()
                            .add(Icons.ALL.information_outline_mdi().style().setCursor("pointer"))
                            .element();
                    sublayerMetadataLinkElement.addEventListener("click", new EventListener() {
                        @Override
                        public void handleEvent(Event evt) {
                            openMetadataDialog(sublayer.getTheAbstract());
                        }
                    });

                    trSublayer.appendChild(td().attr("align", "center").add(sublayerMetadataLinkElement).element());
                    
                    // Preview
                    if (!sublayer.getTitle().contains("Quelle geodienste.ch") && !sublayer.getTitle().contains("Quelle Bund")) {
                        HTMLElement sublayerPreviewLinkElement = div()
                                .add(Icons.ALL.eye_outline_mdi().style().setCursor("pointer"))
                                .element();
                        sublayerPreviewLinkElement.addEventListener("click", new EventListener() {
                            @Override
                            public void handleEvent(Event evt) {
                                openPreviewDialog(sublayer);
                            }
                        });
                        trSublayer.appendChild(td().attr("align", "center").add(sublayerPreviewLinkElement).element()); 
                    } else {
                        trSublayer.appendChild(td().attr("align", "center").add("—").element()); 
                    }


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
                
                tdSublayerIcon.addEventListener("click", new EventListener() {
                    @Override
                    public void handleEvent(Event evt) {
                        tbodyChildren.classList.toggle("hide");
                    }
                });
                
                tdParentTitle.addEventListener("click", new EventListener() {
                    @Override
                    public void handleEvent(Event evt) {
                        tbodyChildren.classList.toggle("hide");
                    }
                });

            }
        }
    }
    
    private void openPreviewDialog(Dataproduct dataproduct) {
        ModalDialog modal = ModalDialog.create("Vorschau: " + dataproduct.getTitle()).large().setAutoClose(true);
        modal.css("modal-object");

        HTMLDivElement mapDiv = div().id("map").element();

        modal.appendChild(mapDiv);

        
        Button closeButton = Button.create("SCHLIESSEN").linkify();
        closeButton.removeWaves();
        closeButton.setBackground(Color.RED_DARKEN_3);
        EventListener closeModalListener = (evt) -> modal.close();
        closeButton.addClickListener(closeModalListener);
        modal.appendFooterChild(closeButton);
        modal.open();

        map = MapPresets.getBlakeAndWhiteMap(mapDiv.id); //, subunitsWmsLayer);
        
        Image wmsLayer = createWmsLayer(dataproduct);
        map.addLayer(wmsLayer);

        closeButton.blur();
    }
    
    private void openMetadataDialog(String theAbstract) {
        ModalDialog modal = ModalDialog.create("Beschreibung").setAutoClose(true);
        modal.css("modal-object");
        
        modal.add(div().innerHtml(SafeHtmlUtils.fromTrustedString(theAbstract)));
                
        Button closeButton = Button.create("SCHLIESSEN").linkify();
        closeButton.removeWaves();
        closeButton.setBackground(Color.RED_DARKEN_3);
        EventListener closeModalListener = (evt) -> modal.close();
        closeButton.addClickListener(closeModalListener);
        modal.appendFooterChild(closeButton);
        modal.open();

        closeButton.blur();
    }

    private Image createWmsLayer(Dataproduct dataproduct) {
        ImageWmsParams imageWMSParams = OLFactory.createOptions();
        imageWMSParams.setLayers(dataproduct.getIdent());

        ImageWmsOptions imageWMSOptions = OLFactory.createOptions();

        String baseUrl = "https://geo.so.ch/api/wms";

        imageWMSOptions.setUrl(baseUrl);
        imageWMSOptions.setParams(imageWMSParams);
        imageWMSOptions.setRatio(1.5f);

        ImageWms imageWMSSource = new ImageWms(imageWMSOptions);

        LayerOptions layerOptions = OLFactory.createOptions();
        layerOptions.setSource(imageWMSSource);

        Image wmsLayer = new Image(layerOptions);
        wmsLayer.set("id", dataproduct.getIdent());
        wmsLayer.setVisible(true);
        wmsLayer.setOpacity(dataproduct.getOpacity() / 255.0);

        return wmsLayer;
    }

    
    private void removeResults() {
        rootTable.getElementsByTagName("tbody");
        
        while(rootTable.firstChild != null) {
            rootTable.removeChild(rootTable.firstChild);
        }        
    }

    @Override
    public HTMLElement element() {
        return root;
    }
    
    public static interface DataproductMapper extends ObjectMapper<List<Dataproduct>> {
    }
}
