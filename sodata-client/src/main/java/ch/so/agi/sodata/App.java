package ch.so.agi.sodata;

import static elemental2.dom.DomGlobal.console;
import static elemental2.dom.DomGlobal.fetch;
import static org.jboss.elemento.Elements.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.dominokit.domino.ui.badges.Badge;
import org.dominokit.domino.ui.breadcrumbs.Breadcrumb;
import org.dominokit.domino.ui.button.Button;
import org.dominokit.domino.ui.datatable.ColumnConfig;
import org.dominokit.domino.ui.datatable.DataTable;
import org.dominokit.domino.ui.datatable.TableConfig;
import org.dominokit.domino.ui.datatable.store.LocalListDataStore;
import org.dominokit.domino.ui.forms.TextBox;
import org.dominokit.domino.ui.grid.Column;
import org.dominokit.domino.ui.grid.Row;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.infoboxes.InfoBox;
import org.dominokit.domino.ui.modals.ModalDialog;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.style.ColorScheme;
import org.dominokit.domino.ui.tabs.Tab;
import org.dominokit.domino.ui.tabs.TabsPanel;
import org.dominokit.domino.ui.themes.Theme;
import org.dominokit.domino.ui.utils.TextNode;

import com.google.gwt.core.client.GWT;
import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.jboss.elemento.HtmlContentBuilder;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.i18n.client.DateTimeFormat;

import elemental2.core.Global;
import elemental2.dom.AbortController;
import elemental2.dom.CSSProperties;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLDocument;
import elemental2.dom.HTMLElement;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.Location;
import elemental2.dom.RequestInit;
import elemental2.dom.URL;
import elemental2.dom.URLSearchParams;
import elemental2.dom.XMLHttpRequest;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import ol.AtPixelOptions;
import ol.Extent;
import ol.Map;
import ol.MapBrowserEvent;
import ol.OLFactory;
import ol.Pixel;
import ol.events.condition.Condition;
import ol.Feature;
import ol.FeatureAtPixelFunction;
import ol.format.GeoJson;
import ol.interaction.Select;
import ol.interaction.SelectOptions;
import ol.layer.Base;
import ol.layer.Layer;
import ol.layer.VectorLayerOptions;
import ol.proj.Projection;
import ol.proj.ProjectionOptions;
import ol.source.Vector;
import ol.source.VectorOptions;
import ol.style.Fill;
import ol.style.Stroke;
import ol.style.Style;

import ch.so.agi.sodata.dto.FileFormatDTO;
import ch.so.agi.sodata.dto.ThemePublicationDTO;

public class App implements EntryPoint {
    // Internationalization
    private MyMessages messages = GWT.create(MyMessages.class);

    // Client application settings
    private String myVar;
    @Deprecated
    private String FILES_SERVER_URL;

    // Format settings
    private NumberFormat fmtDefault = NumberFormat.getDecimalFormat();
    private NumberFormat fmtPercent = NumberFormat.getFormat("#0.0");

    // Browser-URL components
    private Location location;
    private String pathname;
    private String filter = null;
    private String FILTER_PARAM_KEY = "filter";

    // Main HTML elements
    private HTMLElement container;
    private HTMLElement topLevelContent;
    private HTMLElement datasetContent;

    // Theme publications vars
    private ThemePublicationMapper mapper;
    private List<ThemePublicationDTO> themePublications;
    private LocalListDataStore<ThemePublicationDTO> themePublicationListStore;
    private DataTable<ThemePublicationDTO> themePublicationTable;

    // Used for naming the badges based on format abbreviation.
    private HashMap<String, String> formatLookUp = new HashMap<String, String>() {
        {
            put("xtf.zip", "INTERLIS");
            put("itf.zip", "INTERLIS");
            put("gpkg.zip", "GeoPackage");
            put("shp.zip", "Shapefile");
            put("dxf.zip", "DXF");
            put("tif", "GeoTIFF");
            put("laz", "LAZ");
        }
    };

    // Used for sorting based on format abbreviation.
    private ArrayList<String> fileFormatList = new ArrayList<String>() {
        {
            add("xtf.zip");
            add("itf.zip");
            add("gpkg.zip");
            add("shp.zip");
            add("dxf.zip");
            add("tif");
            add("laz");
        }
    };

    // ol3 vector layer
    private String ID_ATTR_NAME = "id";
    private String SUBUNIT_VECTOR_LAYER_ID = "subunit_vector_layer";
    private String SUBUNIT_VECTOR_FEATURE_ID = "subunit_fid";

    // ol3 map
    private String MAP_DIV_ID = "map";
    private Map map;

    // Abort controller for fetching from server
    private AbortController abortController = null;

    // Create model mapper interfaces
    public static interface ThemePublicationMapper extends ObjectMapper<List<ThemePublicationDTO>> {
    }

    public void onModuleLoad() {
//        datasetMapper = GWT.create(DatasetMapper.class);   
        mapper = GWT.create(ThemePublicationMapper.class);

        // Change Domino UI color scheme.
        Theme theme = new Theme(ColorScheme.RED);
        theme.apply();

        // Get url from browser (client) to find out the correct location of resources.
        location = DomGlobal.window.location;
        pathname = location.pathname;

        if (pathname.contains("index.html")) {
            pathname = pathname.replace("index.html", "");
        }

        // Get settings with a synchronous request.
        XMLHttpRequest httpRequest = new XMLHttpRequest();
        httpRequest.open("GET", pathname + "settings", false);
        httpRequest.onload = event -> {
            if (Arrays.asList(200, 201, 204).contains(httpRequest.status)) {
                String responseText = httpRequest.responseText;
                try {
                    JsPropertyMap<Object> propertiesMap = Js.asPropertyMap(Global.JSON.parse(responseText));
                    FILES_SERVER_URL = propertiesMap.getAsAny("filesServerUrl").asString();

                } catch (Exception e) {
                    DomGlobal.window.alert("Error loading settings!");
                    DomGlobal.console.error("Error loading settings!", e);
                }
            } else {
                DomGlobal.window.alert("Error loading settings!" + httpRequest.status);
            }

        };

        httpRequest.addEventListener("error", event -> {
            DomGlobal.window
                    .alert("Error loading settings! Error: " + httpRequest.status + " " + httpRequest.statusText);
        });

        httpRequest.send();

        // Get themes publications json from server and initialize the site.
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

    public void init() {
        // HTMLDocument: used for creating html elements that are not
        // available in elemento (e.g. summary, details).
        HTMLDocument document = DomGlobal.document;

        // This cannot be done in index.html since href depends
        // on the real world url.
        Element head = document.getElementsByTagName("head").getAt(0);
        HTMLElement opensearchdescription = (HTMLElement) document.createElement("link");
        opensearchdescription.setAttribute("rel", "search");
        opensearchdescription.setAttribute("type", "application/opensearchdescription+xml");

        String host = location.host;
        String protocol = location.protocol;
        opensearchdescription.setAttribute("href", protocol + "//" + host + pathname + "opensearchdescription.xml");
        opensearchdescription.setAttribute("title", "Geodaten Kanton Solothurn");
        head.appendChild(opensearchdescription);

        // Get search params to control the browser url
        URLSearchParams searchParams = new URLSearchParams(location.search);

        if (searchParams.has(FILTER_PARAM_KEY)) {
            filter = searchParams.get(FILTER_PARAM_KEY);
        }

        // Add invisible download anchor element. Used for downloading file when clicking into map.
        body().add(a().attr("download", "").id("download").element());
        
        // Add our "root" container
        container = div().id("container").element();
        body().add(container);

        // Add logo
        HTMLElement logoDiv = div().css("logo")
                .add(div().add(
                        img().attr("src", location.protocol + "//" + location.host + location.pathname + "Logo.png")
                                .attr("alt", "Logo Kanton"))
                        .element())
                .element();
        container.appendChild(logoDiv);

        // Create a top level content div for everything except the logo.
        // Not sure why this was done this way. Or if it is necessary.
        topLevelContent = div().id("top-level-content").element();
        container.appendChild(topLevelContent);

        // Add breadcrumb
        Breadcrumb breadcrumb = Breadcrumb.create().appendChild(Icons.ALL.home(), " Home ", (evt) -> {
            DomGlobal.window.open("https://geo.so.ch/", "_self");
        }).appendChild(" Geodaten ", (evt) -> {
        });
        topLevelContent.appendChild(breadcrumb.element());

        topLevelContent.appendChild(div().css("sodata-title").textContent("Geodaten Kanton Solothurn").element());

        String infoString = "Geodaten vom Kanton Solothurn können kostenlos heruntergeladen werden. Die Vektordaten sowie die Rasterdaten werden "
                + "in vordefinierten Formaten und Gebieten (Kanton, Gemeinde oder andere) angeboten. Bei der Gebietseinteilung Gemeinde oder andere "
                + "kann der Benutzer das gewünschte Gebiet selber wählen. Weitere Informationen zur Datenliste und alternativen Bezugsmöglichkeiten "
                + "finden Sie <a class='default-link' href='https://so.ch/verwaltung/bau-und-justizdepartement/amt-fuer-geoinformation/geoportal/geodaten-herunterladen/' target='_blank'>hier</a>."
                + "<br><br>"
                + "Der Aufbau des Datenangebotes wird im Frühjahr 2023 abgeschlossen. Ab dann sind alle öffentlichen Geodaten des Kantons enthalten.";

        topLevelContent.appendChild(div().css("info").innerHtml(SafeHtmlUtils.fromTrustedString(infoString)).element());

        TextBox textBox = TextBox.create().setLabel(messages.search_terms());
        textBox.addLeftAddOn(Icons.ALL.search());
        textBox.setFocusColor(Color.RED_DARKEN_3);
        textBox.getInputElement().setAttribute("autocomplete", "off");
        textBox.getInputElement().setAttribute("spellcheck", "false");

        textBox.focus();

        HTMLElement resetIcon = Icons.ALL.close().style().setCursor("pointer").get().element();
        resetIcon.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                textBox.clear();
                themePublicationListStore.setData(themePublications);
                removeQueryParam(FILTER_PARAM_KEY);
            }
        });
        textBox.addRightAddOn(resetIcon);

        textBox.addEventListener("keyup", event -> {
            if (textBox.getValue().trim().length() > 0 && textBox.getValue().trim().length() <= 2) {
                themePublicationListStore.setData(themePublications);
                return;
            }

            if (textBox.getValue().trim().length() == 0) {
                themePublicationListStore.setData(themePublications);
                removeQueryParam(FILTER_PARAM_KEY);
                return;
            }

            if (abortController != null) {
                abortController.abort();
            }

            abortController = new AbortController();
            final RequestInit init = RequestInit.create();
            init.setSignal(abortController.signal);

            DomGlobal.fetch("/themepublications?query=" + textBox.getValue().toLowerCase(), init).then(response -> {
                if (!response.ok) {
                    return null;
                }
                return response.text();
            }).then(json -> {
                List<ThemePublicationDTO> filteredThemePublications = mapper.read(json);
                filteredThemePublications.sort(new ThemePublicationComparator());

                themePublicationListStore.setData(filteredThemePublications);

                updateUrlLocation(FILTER_PARAM_KEY, textBox.getValue().trim());

                abortController = null;

                return null;
            }).catch_(error -> {
                console.log(error);
                return null;
            });
        });
        topLevelContent.appendChild(div().id("search-panel").add(div().id("suggestbox-div").add(textBox)).element());

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

                            if (cell.getRecord().isHasSubunits()) {
                                for (FileFormatDTO fileFormat : sortedFileFormats) {

                                    HTMLElement badge = Badge.create(formatLookUp.get(fileFormat.getAbbreviation()))
                                    .setBackground(Color.GREY_LIGHTEN_2)
                                    .style()
                                    .setMarginRight("10px")
                                    .setMarginTop("5px")
                                    .setMarginBottom("5px")
                                    .get()
                                    .element();
                                                                        
                                    badge.id = fileFormat.getAbbreviation();
                                    
                                    badge.addEventListener("click", new EventListener() {
                                        @Override
                                        public void handleEvent(Event evt) {
                                            HTMLElement clickedBadge = (HTMLElement) evt.currentTarget;
                                            String fileFormatAbbreviation = clickedBadge.id;
                                            openRegionSelectionDialog(cell.getRecord(), fileFormatAbbreviation);
                                        }
                                        
                                    });

                                    badgesElement.appendChild(
                                            span()
                                            .css("badge-link")
                                            .style("cursor: pointer;")
                                            .add(badge)
                                            .element());
                                    
                                }

                                return badgesElement;
                            } else {                               
                                for (FileFormatDTO fileFormat : sortedFileFormats) {
                                    String fileUrl = cell.getRecord().getDownloadHostUrl() + "/" + cell.getRecord().getIdentifier()
                                            + "/aktuell/" + cell.getRecord().getIdentifier() + "."
                                            + fileFormat.getAbbreviation();
                                    fileUrl = "proxy?file=" + fileUrl;
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

        topLevelContent.appendChild(themePublicationTable.element());

        if (filter != null && filter.trim().length() > 0) {
            textBox.setValue(filter);
            textBox.element().dispatchEvent(new KeyboardEvent("keyup"));
        }
    }

    private void openMetadataDialog(ThemePublicationDTO themePublication) {
        ModalDialog modal = ModalDialog.create(themePublication.getTitle()).setAutoClose(true);
        modal.css("modal-object");

        MetadataElement metaDataElement = new MetadataElement(themePublication, themePublication.getDownloadHostUrl(), messages);
        modal.add(metaDataElement);

        Button closeButton = Button.create(messages.close().toUpperCase()).linkify();
        closeButton.removeWaves();
        closeButton.setBackground(Color.RED_DARKEN_3);
        EventListener closeModalListener = (evt) -> modal.close();
        closeButton.addClickListener(closeModalListener);
        modal.appendFooterChild(closeButton);
        
        // Links und rechts aligniert.
        //modal.appendFooterChild(div().add(div().style("float:left;").textContent("foo")).add(div().style("float:right;").textContent("bar")));
      
        modal.large().open();
    }

    private void openRegionSelectionDialog(ThemePublicationDTO themePublication, String fileFormatAbbreviation) {
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
        Tab mapTab = Tab.create(Icons.ALL.map_outline_mdi(), messages.subunits_tab_map().toUpperCase());
        Tab listTab = Tab.create(Icons.ALL.file_download_outline_mdi(), messages.subunits_tab_list().toUpperCase());
        tabsPanel.appendChild(mapTab);
        tabsPanel.appendChild(listTab);

        HTMLDivElement mapDiv = div().id("map").element();
        modal.getBodyElement()
                .appendChild(div().css("modal-body-paragraph")
                        .textContent("Sie können Daten einzelner Gebiete mit einem Klick in die Karte herunterladen. "
                                + "Im Reiter 'Liste' können Sie die Daten in einer Liste suchen und herunterladen. "));

        mapTab.appendChild(mapDiv);

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
        
        // Teil des Downloadlinks. Wird in der Tabelle wie auch in der Karte benötigt.
        String fileBaseUrl = themePublication.getDownloadHostUrl() + "/" + themePublication.getIdentifier() + "/aktuell/";
        
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
                        String themeIdentifier = themePublication.getIdentifier();
                        String itemIdentifier = cell.getRecord().get("identifier");
                        
                        String fileUrl = fileBaseUrl + itemIdentifier + "." + themeIdentifier + "."
                                + fileFormat.getAbbreviation();     
                        
                        fileUrl = "proxy?file=" + fileUrl;
                       
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
                
        listTab.appendChild(subunitFeatureTable.element());

        modal.getBodyElement().appendChild(tabsPanel);
        
        listTab.addClickListener(new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                ol.layer.Vector vectorLayer = (ol.layer.Vector) getMapLayerById(SUBUNIT_VECTOR_LAYER_ID);
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
        map.addSingleClickListener(new MapSingleClickListener(fileBaseUrl, themePublication.getIdentifier(), fileFormatAbbreviation));
        
        closeButton.blur();
    }
    
    private final class MapSingleClickListener implements ol.event.EventListener<MapBrowserEvent> {
        String fileBaseUrl;
        String themeIdentifier;
        String fileFormatAbbreviation;
        
        public MapSingleClickListener(String fileBaseUrl, String themeIdentifier, String fileFormatAbbreviation) {
            this.fileBaseUrl = fileBaseUrl;
            this.themeIdentifier = themeIdentifier;
            this.fileFormatAbbreviation = fileFormatAbbreviation;
        }
        
        @Override
        public void onEvent(MapBrowserEvent event) {
            AtPixelOptions featureAtPixelOptions = new AtPixelOptions();
            map.forEachFeatureAtPixel(event.getPixel(), new FeatureAtPixelFunction() {
                @Override
                public boolean call(Feature feature, Layer layer) {                    
                    String itemIdentifier = feature.get("identifier").toString();
                    String fileName = itemIdentifier + "." + themeIdentifier + "." + fileFormatAbbreviation;
                    String fileUrl = fileBaseUrl + fileName;
                    fileUrl = "proxy?file=" + fileUrl;
                    DomGlobal.window.open(fileUrl, "_blank");
                                        
                    return false;
                }
            }, featureAtPixelOptions);
        }
    }

    private void createVectorLayers(Feature[] features) {
        removeVectorLayer(SUBUNIT_VECTOR_LAYER_ID);

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

    private void removeQueryParam(String key) {
        URL url = new URL(DomGlobal.location.href);
        String host = url.host;
        String protocol = url.protocol;
        String pathname = url.pathname;
        URLSearchParams params = url.searchParams;
        params.delete(key);

        String newUrl = protocol + "//" + host + pathname + "?" + params.toString();
        updateUrlWithoutReloading(newUrl);
    }

    private void updateUrlLocation(String key, String value) {
        URL url = new URL(DomGlobal.location.href);
        String host = url.host;
        String protocol = url.protocol;
        String pathname = url.pathname;
        URLSearchParams params = url.searchParams;
        params.set(key, value);

        String newUrl = protocol + "//" + host + pathname + "?" + params.toString();

        updateUrlWithoutReloading(newUrl);
    }

    // Update the URL in the browser without reloading the page.
    private static native void updateUrlWithoutReloading(String newUrl) /*-{
        $wnd.history.pushState(newUrl, "", newUrl);
    }-*/;
}