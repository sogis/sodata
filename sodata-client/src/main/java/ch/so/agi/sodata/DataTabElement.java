package ch.so.agi.sodata;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.span;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import org.dominokit.domino.ui.tabs.Tab;
import org.dominokit.domino.ui.tabs.TabsPanel;
import org.dominokit.domino.ui.utils.TextNode;
import org.jboss.elemento.IsElement;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

import ch.so.agi.sodata.dto.FileFormatDTO;
import ch.so.agi.sodata.dto.ThemePublicationDTO;
import elemental2.dom.AbortController;
import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.RequestInit;
import ol.AtPixelOptions;
import ol.Feature;
import ol.FeatureAtPixelFunction;
import ol.Map;
import ol.MapBrowserEvent;
import ol.OLFactory;
import ol.format.GeoJson;
import ol.layer.Base;
import ol.layer.Layer;
import ol.layer.VectorLayerOptions;
import ol.source.Vector;
import ol.source.VectorOptions;
import ol.style.Fill;
import ol.style.Stroke;
import ol.style.Style;

public class DataTabElement implements IsElement<HTMLElement> {
    private final HTMLElement root;

    private MyMessages messages;
    private String filesServerUrl;
    
    private ThemePublicationMapper mapper;
    private List<ThemePublicationDTO> themePublications;
    private LocalListDataStore<ThemePublicationDTO> themePublicationListStore;
    private DataTable<ThemePublicationDTO> themePublicationTable;

    private AbortController abortController = null;

    // for lookup
    private HashMap<String, String> formatLookUp = new HashMap<String, String>() {
        {
            put("xtf", "INTERLIS");
            put("itf", "INTERLIS");
            put("shp", "Shapefile");
            put("dxf", "DXF");
            put("gpkg", "GeoPackage");
            put("tif", "GeoTIFF");
        }
    };

    // for sorting
    private ArrayList<String> fileFormatList = new ArrayList<String>() {
        {
            add("xtf");
            add("itf");
            add("gpkg");
            add("shp");
            add("dxf");
            add("tif");
        }
    };
    
    // ol3 vector layer
    private String ID_ATTR_NAME = "id";
    private String SUBUNIT_VECTOR_LAYER_ID = "subunit_vector_layer";
    private String SELECTED_VECTOR_LAYER_ID = "selected_vector_layer";
    
    // ol3 map
    private Map map;

    // Create model mapper interfaces
    public static interface ThemePublicationMapper extends ObjectMapper<List<ThemePublicationDTO>> {
    }

    public DataTabElement(MyMessages messages, String filesServerUrl) {
        root = div().element();
        
        this.messages = messages;
        this.filesServerUrl = filesServerUrl;
        
        // Mapper for mapping server json response to objects
        mapper = GWT.create(ThemePublicationMapper.class);
        
        // Get themes publications json from server for first time table initialization.
        DomGlobal.fetch("/themepublications").then(response -> {
            if (!response.ok) {
                DomGlobal.window.alert(response.statusText + ": " + response.body);
                return null;
            }
            return response.text();
        }).then(json -> {
            themePublications = mapper.read(json);
            Collections.sort(themePublications, new ThemePublicationComparator());
            
            init();

            return null;
        }).catch_(error -> {
            console.log(error);
            DomGlobal.window.alert(error.toString());
            return null;
        });
    }
    
    private void init() {        
        // Configuration of the theme publication table
        TableConfig<ThemePublicationDTO> tableConfig = new TableConfig<>();
        tableConfig
                .addColumn(ColumnConfig.<ThemePublicationDTO>create("title", messages.table_header_topic())
                        .setShowTooltip(false)
                        .textAlign("left")
                        .setCellRenderer(cell -> TextNode.of(cell.getTableRow().getRecord().getTitle())))
                .addColumn(ColumnConfig
                        .<ThemePublicationDTO>create("lastPublishingDate", messages.table_header_publication_date())
                        .setShowTooltip(false)
                        .textAlign("left")
                        .setCellRenderer(cell -> {
                            Date date = DateTimeFormat.getFormat("yyyy-MM-dd")
                                    .parse(cell.getTableRow().getRecord().getLastPublishingDate());
                            String dateString = DateTimeFormat.getFormat("dd.MM.yyyy").format(date);
                            return TextNode.of(dateString);
                        }))
                .addColumn(ColumnConfig.<ThemePublicationDTO>create("metadata", messages.table_header_metadata())
                        .setShowTooltip(false)
                        .textAlign("center")
                        .setCellRenderer(cell -> {
                            HTMLElement metadataLinkElement = div()
                                    .add(Icons.ALL.information_outline_mdi().style().setCursor("pointer"))
                                    .element();
                            metadataLinkElement.addEventListener("click", new EventListener() {
                                @Override
                                public void handleEvent(Event evt) {
                                    openMetadataDialog(cell.getRecord());
                                }
                            });
                            return metadataLinkElement;
                        }))
                .addColumn(ColumnConfig.<ThemePublicationDTO>create("formats", messages.table_header_data_download())
                        .setShowTooltip(false)
                        .textAlign("left")
                        .setCellRenderer(cell -> {
                            HTMLElement badgesElement = div().element();

                            if (cell.getRecord().isHasSubunits()) {
                                HTMLElement regionSelectionElement = a().css("default-link")
                                        .textContent(messages.table_subunit_selection())
                                        .element();
                                regionSelectionElement.addEventListener("click", new EventListener() {
                                    @Override
                                    public void handleEvent(Event evt) {
                                        openRegionSelectionDialog(cell.getRecord());
                                    }
                                });
                                return regionSelectionElement;
                            } else {
                                List<FileFormatDTO> sortedFileFormats = cell.getRecord()
                                        .getFileFormats()
                                        .stream()
                                        .sorted(new Comparator<FileFormatDTO>() {
                                            @Override
                                            public int compare(FileFormatDTO o1, FileFormatDTO o2) {
                                                return ((Integer) fileFormatList.indexOf(o1.getAbbreviation()))
                                                        .compareTo(((Integer) fileFormatList
                                                                .indexOf(o2.getAbbreviation())));
                                            }
                                        })
                                        .collect(Collectors.toList());

                                for (FileFormatDTO fileFormat : sortedFileFormats) {
                                    String fileUrl = filesServerUrl + "/" + cell.getRecord().getIdentifier()
                                            + "/aktuell/" + cell.getRecord().getIdentifier() + "."
                                            + fileFormat.getAbbreviation() + ".zip";
                                    badgesElement.appendChild(a().css("badge-link")
                                            .attr("href", fileUrl)
                                            .attr("target", "_blank")
                                            .add(Badge.create(formatLookUp.get(fileFormat.getAbbreviation()))
                                                    .setBackground(Color.GREY_LIGHTEN_2)
                                                    .style()
                                                    .setMarginRight("10px")
                                                    .setMarginTop("5px")
                                                    .setMarginBottom("5px")
                                                    .get()
                                                    .element())
                                            .element());
                                }
                                return badgesElement;
                            }
                        }));

        themePublicationListStore = new LocalListDataStore<>();
        themePublicationListStore.setData(themePublications);

        themePublicationTable = new DataTable<>(tableConfig, themePublicationListStore);
        themePublicationTable.setId("dataset-table");
        themePublicationTable.noStripes();
        themePublicationTable.load();

        root.appendChild(themePublicationTable.element());
    }
    
    private void openMetadataDialog(ThemePublicationDTO themePublication) {
        ModalDialog modal = ModalDialog.create(themePublication.getTitle()).setAutoClose(true);
        modal.css("modal-object");

        MetadataElement metaDataElement = new MetadataElement(themePublication, filesServerUrl, messages);
        modal.add(metaDataElement);

        Button closeButton = Button.create(messages.close().toUpperCase()).linkify();
        closeButton.removeWaves();
        closeButton.setBackground(Color.RED_DARKEN_3);
        EventListener closeModalListener = (evt) -> modal.close();
        closeButton.addClickListener(closeModalListener);
        modal.appendFooterChild(closeButton);
      
        modal.large().open();
    }

    private void openRegionSelectionDialog(ThemePublicationDTO themePublication) {
        List<FileFormatDTO> sortedFileFormats = themePublication
                .getFileFormats()
                .stream()
                .sorted(new Comparator<FileFormatDTO>() {
                    @Override
                    public int compare(FileFormatDTO o1, FileFormatDTO o2) {
                        return ((Integer) fileFormatList.indexOf(o1.getAbbreviation()))
                                .compareTo(((Integer) fileFormatList
                                        .indexOf(o2.getAbbreviation())));
                    }
                })
                .collect(Collectors.toList());
        
        ModalDialog modal = ModalDialog.create(messages.subunits_title_selection() + ": " + themePublication.getTitle()).setAutoClose(true);
        modal.css("modal-object");

        TabsPanel tabsPanel = TabsPanel.create().setColor(Color.RED_DARKEN_3);
        Tab selectionTab = Tab.create(Icons.ALL.map_outline_mdi(), messages.subunits_tab_selection().toUpperCase());
        Tab downloadTab = Tab.create(Icons.ALL.file_download_outline_mdi(), messages.subunits_tab_download().toUpperCase());
        tabsPanel.appendChild(selectionTab);
        tabsPanel.appendChild(downloadTab);

        HTMLDivElement mapDiv = div().id("map").element();
        modal.getBodyElement()
                .appendChild(div().css("modal-body-paragraph")
                        .textContent("Sie können einzelne Gebiete mit einem Klick in die Karte aus- und abwählen. "
                                + "Im Reiter 'Herunterladen' können Sie die Daten anschliessend herunterladen. "));

        selectionTab.appendChild(mapDiv);

        Button closeButton = Button.create(messages.close().toUpperCase()).linkify();
        closeButton.removeWaves();
        closeButton.setBackground(Color.RED_DARKEN_3);
        EventListener closeModalListener = (evt) -> modal.close();
        closeButton.addClickListener(closeModalListener);
        modal.appendFooterChild(closeButton);
        modal.large().open();

        DomGlobal.fetch("/subunits/"+themePublication.getIdentifier() + ".json")
        .then(response -> {
            if (!response.ok) {
                DomGlobal.window.alert(response.statusText + ": " + response.body);
                return null;
            }
            return response.text();
        })
        .then(json -> {            
            Feature[] features = (new GeoJson()).readFeatures(json); 
            createVectorLayers(features);
            return null;
        }).catch_(error -> {
            console.log(error);
            DomGlobal.window.alert(error.toString());
            return null;
        });

        TableConfig<Feature> tableConfig = new TableConfig<>();
        tableConfig
            .addColumn(ColumnConfig.<Feature>create("title", messages.subunits_download_table_name()).setShowTooltip(false).textAlign("left")
                .setCellRenderer(cell -> TextNode.of(cell.getTableRow().getRecord().get("title"))))
            .addColumn(ColumnConfig.<Feature>create("lastEditingDate", messages.subunits_download_table_publication_date()).setShowTooltip(false).textAlign("left")
                .setCellRenderer(cell -> {
                    Date date;
                    String dateString;
                    if (cell.getRecord().get("lastPublishingDate").toString().length() < 8) {
                        date = DateTimeFormat.getFormat("yyyy-MM").parse(cell.getRecord().get("lastPublishingDate"));
                        dateString = DateTimeFormat.getFormat("MMMM yyyy").format(date);
                    } else {
                        date = DateTimeFormat.getFormat("yyyy-MM-dd").parse(cell.getRecord().get("lastPublishingDate"));
                        dateString = DateTimeFormat.getFormat("dd.MM.yyyy").format(date);
                    }
                    return TextNode.of(dateString);
                }))
            .addColumn(ColumnConfig.<Feature>create("formats", messages.subunits_download_table_download()).setShowTooltip(false).textAlign("left")
                .setCellRenderer(cell -> {
                    HTMLElement badgesElement = div().element();

                    for (FileFormatDTO fileFormat : sortedFileFormats) {
                        String fileExtension = "zip";
                        if (themePublication.getModel() == null) {
                            fileExtension = fileFormat.getAbbreviation();
                        }
                        
                        String themeIdentifier = themePublication.getIdentifier();
                        String itemIdentifier = cell.getRecord().get("identifier");
                        
                        String fileUrl = null;
                        if (themePublication.getModel() == null) {
                            // Rasterdaten
                            fileExtension = fileFormat.getAbbreviation();
                            fileUrl = filesServerUrl + "/" + themeIdentifier
                                    + "/aktuell/" + itemIdentifier + "." + themeIdentifier + "."
                                    + fileFormat.getAbbreviation();                     
                        } else {
                            // Vektordaten
                            fileUrl = filesServerUrl + "/" + themeIdentifier
                                    + "/aktuell/" + itemIdentifier + "." + themeIdentifier + "."
                                    + fileFormat.getAbbreviation() + "." + fileExtension;
                        }
                       
                        badgesElement.appendChild(a().css("badge-link")
                                .attr("href", fileUrl)                                 
                                .attr("target", "_blank")
                                .add(Badge.create(formatLookUp.get(fileFormat.getAbbreviation()))
                                        .setBackground(Color.GREY_LIGHTEN_2).style()
                                        .setMarginRight("10px").setMarginTop("5px")
                                        .setMarginBottom("5px").get().element())
                                .element());
                     
                    }
                    return badgesElement;
                }));

        LocalListDataStore<Feature> subunitListStore = new LocalListDataStore<>();

        DataTable<Feature> subunitFeatureTable = new DataTable<>(tableConfig, subunitListStore);
        subunitFeatureTable.setId("dataset-table");
        subunitFeatureTable.noStripes();
        subunitFeatureTable.noHover();
        subunitFeatureTable.load();
        downloadTab.appendChild(subunitFeatureTable.element());

        modal.getBodyElement().appendChild(tabsPanel);

        downloadTab.addClickListener(new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                ol.layer.Vector vectorLayer = (ol.layer.Vector) getMapLayerById(SELECTED_VECTOR_LAYER_ID);
                ol.source.Vector vectorSource = vectorLayer.getSource();                                
                Feature[] features = vectorSource.getFeatures();

                List<Feature> featuresList = new ArrayList<Feature>();
                for (int i = 0; i < features.length; i++) {
                    Feature feature = features[i];
                    featuresList.add(feature);
                }
                
                Collections.sort(featuresList, new Comparator<Feature>() {
                    @Override
                    public int compare(Feature o1, Feature o2) {
                        String string0 = o1.get("title").toString().toLowerCase();
                        String string1 = o2.get("title").toString().toLowerCase();
                        string0 = string0.replace("ä", "a");
                        string0 = string0.replace("ö", "o");
                        string0 = string0.replace("ü", "u");
                        string1 = string1.replace("ä", "a");
                        string1 = string1.replace("ö", "o");
                        string1 = string1.replace("ü", "u");
                        return string0.compareTo(string1);
                    }
                });
                
                subunitListStore.setData(featuresList);
            }
        });

        map = MapPresets.getBlakeAndWhiteMap(mapDiv.id);
        map.addSingleClickListener(new MapSingleClickListener());

        closeButton.blur();
    }
    
    private final class MapSingleClickListener implements ol.event.EventListener<MapBrowserEvent> {
        @Override
        public void onEvent(MapBrowserEvent event) {
            AtPixelOptions featureAtPixelOptions = new AtPixelOptions();
            map.forEachFeatureAtPixel(event.getPixel(), new FeatureAtPixelFunction() {
                @Override
                public boolean call(Feature feature, Layer layer) {
                    if (layer.get(ID_ATTR_NAME).toString().equalsIgnoreCase(SELECTED_VECTOR_LAYER_ID)) {
                        ol.layer.Vector selectedLayer = (ol.layer.Vector) getMapLayerById(SELECTED_VECTOR_LAYER_ID);
                        Vector selectedSource = (Vector) selectedLayer.getSource();
                        selectedSource.removeFeature(feature);
                        return true;
                    }
                    if (layer.get(ID_ATTR_NAME).toString().equalsIgnoreCase(SUBUNIT_VECTOR_LAYER_ID)) {
                        ol.layer.Vector selectedLayer = (ol.layer.Vector) getMapLayerById(SELECTED_VECTOR_LAYER_ID);
                        Vector selectedSource = (Vector) selectedLayer.getSource();

                        Style style = new Style();
                        Stroke stroke = new Stroke();
                        stroke.setWidth(4);
                        stroke.setColor(new ol.color.Color(198, 40, 40, 1.0));
                        style.setStroke(stroke);
                        Fill fill = new Fill();
                        fill.setColor(new ol.color.Color(255, 255, 255, 0.6));
                        style.setFill(fill);

                        Feature f = feature.clone();
                        f.setStyle(style);
                        selectedSource.addFeature(f);
                    }
                    return false;
                }
            }, featureAtPixelOptions);
        }
    }

    private void createVectorLayers(Feature[] features) {
        removeVectorLayer(SUBUNIT_VECTOR_LAYER_ID);
        removeVectorLayer(SELECTED_VECTOR_LAYER_ID);

        {
            Style style = new Style();
            Stroke stroke = new Stroke();
            stroke.setWidth(4);
            // stroke.setColor(new ol.color.Color(56, 142, 60, 1.0));
            // stroke.setColor(new ol.color.Color(230, 0, 0, 0.6));
            stroke.setColor(new ol.color.Color(78, 127, 217, 1.0));
            style.setStroke(stroke);
            Fill fill = new Fill();
            fill.setColor(new ol.color.Color(255, 255, 255, 0.6));
            style.setFill(fill);

            ol.Collection<Feature> featureCollection = new ol.Collection<Feature>();
            for (Feature feature : features) {
                feature.setStyle(style);
                featureCollection.push(feature);
            }

            VectorOptions vectorSourceOptions = OLFactory.createOptions();
            vectorSourceOptions.setFeatures(featureCollection);
            Vector vectorSource = new Vector(vectorSourceOptions);

            VectorLayerOptions vectorLayerOptions = OLFactory.createOptions();
            vectorLayerOptions.setSource(vectorSource);
            ol.layer.Vector vectorLayer = new ol.layer.Vector(vectorLayerOptions);
            vectorLayer.setZIndex(100);
            vectorLayer.set(ID_ATTR_NAME, SUBUNIT_VECTOR_LAYER_ID);
            map.addLayer(vectorLayer);
        }

        {
            Style style = new Style();
            Stroke stroke = new Stroke();
            stroke.setWidth(4);
            stroke.setColor(new ol.color.Color(198, 40, 40, 1.0));
            style.setStroke(stroke);
            Fill fill = new Fill();
            fill.setColor(new ol.color.Color(255, 255, 255, 0.6));
            style.setFill(fill);

            VectorOptions vectorSourceOptions = OLFactory.createOptions();
            Vector vectorSource = new Vector(vectorSourceOptions);

            VectorLayerOptions vectorLayerOptions = OLFactory.createOptions();
            vectorLayerOptions.setSource(vectorSource);
            ol.layer.Vector vectorLayer = new ol.layer.Vector(vectorLayerOptions);
            vectorLayer.set(ID_ATTR_NAME, SELECTED_VECTOR_LAYER_ID);
            vectorLayer.setZIndex(1000);
            map.addLayer(vectorLayer);
        }
    }

    private void removeVectorLayer(String id) {
        Base vlayer = getMapLayerById(id);
        map.removeLayer(vlayer);
    }

    private Base getMapLayerById(String id) {
        ol.Collection<Base> layers = map.getLayers();
        for (int i = 0; i < layers.getLength(); i++) {
            Base item = layers.item(i);
            try {
                String layerId = item.get(ID_ATTR_NAME);
                if (layerId == null) {
                    continue;
                }
                if (layerId.equalsIgnoreCase(id)) {
                    return item;
                }
            } catch (Exception e) {
                console.log(e.getMessage());
                console.log("should not reach here");
            }
        }
        return null;
    }
    
    public void updateTable(String queryString) {
        if (abortController != null) {
            abortController.abort();
        }

        abortController = new AbortController();
        final RequestInit init = RequestInit.create();
        init.setSignal(abortController.signal);

        DomGlobal.fetch("/themepublications?query=" + queryString.toLowerCase(), init).then(response -> {
            if (!response.ok) {
                return null;
            }
            return response.text();
        }).then(json -> {
            List<ThemePublicationDTO> filteredThemePublications = mapper.read(json);
            filteredThemePublications.sort(new ThemePublicationComparator());

            themePublicationListStore.setData(filteredThemePublications);

            abortController = null;

            return null;
        }).catch_(error -> {
            console.log(error);
            return null;
        });
    }
    
    public void resetTable() {
        themePublicationListStore.setData(themePublications);
    } 
    
    @Override
    public HTMLElement element() {
        return root;
    }

}
