package ch.so.agi.sodata;

import static elemental2.dom.DomGlobal.console;
import static elemental2.dom.DomGlobal.fetch;
import static org.jboss.elemento.Elements.*;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.dominokit.domino.ui.badges.Badge;
import org.dominokit.domino.ui.breadcrumbs.Breadcrumb;
import org.dominokit.domino.ui.button.Button;
import org.dominokit.domino.ui.datatable.ColumnConfig;
import org.dominokit.domino.ui.datatable.DataTable;
import org.dominokit.domino.ui.datatable.TableConfig;
import org.dominokit.domino.ui.datatable.store.LocalListDataStore;
import org.dominokit.domino.ui.forms.Radio;
import org.dominokit.domino.ui.forms.RadioGroup;
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

import org.gwtproject.http.client.Request;
import org.gwtproject.http.client.RequestBuilder;
import org.gwtproject.http.client.RequestCallback;
import org.gwtproject.http.client.RequestException;
import org.gwtproject.http.client.Response;
import org.gwtproject.safehtml.shared.SafeHtmlUtils;

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
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLDocument;
import elemental2.dom.HTMLElement;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.Location;
import elemental2.dom.RequestInit;
import elemental2.dom.URL;
import elemental2.dom.URLSearchParams;
import elemental2.dom.XMLHttpRequest;
import elemental2.core.JsString;
import elemental2.core.JsObject;
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
import proj4.Proj4;

import ch.so.agi.sodata.Dataset;

public class App implements EntryPoint {
    // Internationalization
    private MyMessages messages = GWT.create(MyMessages.class);

    // Application settings
    private String myVar;
    private String DATA_BASE_URL;

    // Format settings
    private NumberFormat fmtDefault = NumberFormat.getDecimalFormat();
    private NumberFormat fmtPercent = NumberFormat.getFormat("#0.0");

    private Location location;
    private String pathname;
    private String filter = null;
    //    private String ident = null;
    private String FILTER_PARAM_KEY = "filter";
//    private String IDENT_PARAM_KEY = "ident";

    private HTMLElement container;
    private HTMLElement topLevelContent;
    private HTMLElement datasetContent;

    private DatasetMapper mapper;
    private List<Dataset> datasets;
    private LocalListDataStore<Dataset> listStore;
    private DataTable<Dataset> datasetTable;

    private HashMap<String, String> formatLookUp = new HashMap<String, String>();

    private String ID_ATTR_NAME = "id";
    private String SUBUNIT_VECTOR_LAYER_ID = "subunit_vector_layer";
    private String SUBUNIT_VECTOR_FEATURE_ID = "subunit_fid";
    private String SELECTED_VECTOR_LAYER_ID = "selected_vector_layer";
    private String SELECTED_VECTOR_FEATURE_ID = "selected_fid";

    private AbortController abortController = null;

    // Projection
    //private static final String EPSG_2056 = "EPSG:2056";
    //private static final String EPSG_4326 = "EPSG:4326"; 
    //private Projection projection;

    public static interface DatasetMapper extends ObjectMapper<List<Dataset>> {
    }

    private String MAP_DIV_ID = "map";
    private Map map;

    public void onModuleLoad() {
        formatLookUp.put("xtf", "INTERLIS");
        formatLookUp.put("itf", "INTERLIS");
        formatLookUp.put("shp", "Shapefile");
        formatLookUp.put("dxf", "DXF");
        formatLookUp.put("gpkg", "GeoPackage");
        formatLookUp.put("gtiff", "GeoTIFF");

        mapper = GWT.create(DatasetMapper.class);

        // Change Domino UI color scheme.
        Theme theme = new Theme(ColorScheme.RED);
        theme.apply();

        // Get url from browser (client) to find out the correct location of resources.
        location = DomGlobal.window.location;
        String host = location.host;
        String protocol = location.protocol;
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
                    DATA_BASE_URL = propertiesMap.getAsAny("dataBaseUrl").asString();

                } catch (Exception e) {
                    DomGlobal.window.alert("Error loading settings!");
                    DomGlobal.console.error("Error loading settings!", e);
                }
            } else {
                DomGlobal.window.alert("Error loading settings!" + httpRequest.status);
            }

        };

        httpRequest.addEventListener("error", event -> {
            DomGlobal.window.alert("Error loading settings! Error: " + httpRequest.status + " " + httpRequest.statusText);
        });

        httpRequest.send();

        // Get settings
//        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, protocol + "//" + host + pathname + "settings");
//        try {
//            Request request = builder.sendRequest(null, new RequestCallback() {
//                @Override
//                public void onResponseReceived(Request request, Response response) {
//                    console.log(response.getText());
//                    
//                }
//
//                @Override
//                public void onError(Request request, Throwable exception) {
//                    console.log("Request was canceled - no timeout should occur");
//                }
//            });
//            //request.cancel();
//
//        } catch (RequestException e) {
//            console.log(e.getMessage());
//        }


        // Get datasets json from server and initialize the site.
        DomGlobal.fetch("/datasets")
                .then(response -> {
                    if (!response.ok) {
                        DomGlobal.window.alert(response.statusText + ": " + response.body);
                        return null;
                    }
                    return response.text();
                })
                .then(json -> {
                    datasets = mapper.read(json);

                    Collections.sort(datasets, new Comparator<Dataset>() {
//                @Override
//                public int compare(Dataset o1, Dataset o2) {
//                    return o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase());
//                }

                        // GWT does not know Locale.GERMAN hence the Collator approach cannot be used.
                        @Override
                        public int compare(Dataset o1, Dataset o2) {
                            String string0 = o1.getTitle().toLowerCase();
                            String string1 = o2.getTitle().toLowerCase();
                            string0 = string0.replace("ä", "a");
                            string0 = string0.replace("ö", "o");
                            string0 = string0.replace("ü", "u");
                            string1 = string1.replace("ä", "a");
                            string1 = string1.replace("ö", "o");
                            string1 = string1.replace("ü", "u");
                            return string0.compareTo(string1);
                        }

                    });

                    init();

                    return null;
                }).catch_(error -> {
                    console.log(error);
                    DomGlobal.window.alert(error.toString());
                    return null;
                });
    }

    public void init() {
	    /*
	    // Registering EPSG:2056 / LV95 reference frame.
        Proj4.defs(EPSG_2056, "+proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 +k_0=1 +x_0=2600000 +y_0=1200000 +ellps=bessel +towgs84=674.374,15.056,405.346,0,0,0,0 +units=m +no_defs");
        ol.proj.Proj4.register(Proj4.get());

        ProjectionOptions projectionOptions = OLFactory.createOptions();
        projectionOptions.setCode(EPSG_2056);
        projectionOptions.setUnits("m");
        projectionOptions.setExtent(new Extent(2420000, 1030000, 2900000, 1350000));
        projection = new Projection(projectionOptions);
        Projection.addProjection(projection);
        */

        // HTML document: used for creating html elements that are not
        // available in elemento (e.g. summary, details).
        HTMLDocument document = DomGlobal.document;

        // This cannot be done in index.html since href depends
        // on the url.
        Element head = document.getElementsByTagName("head").getAt(0);
        HTMLElement opensearchdescription = (HTMLElement) document.createElement("link");
        opensearchdescription.setAttribute("rel", "search");
        opensearchdescription.setAttribute("type", "application/opensearchdescription+xml");

        String host = location.host;
        String protocol = location.protocol;
        opensearchdescription.setAttribute("href", protocol + "//" + host + pathname + "opensearchdescription.xml");
        opensearchdescription.setAttribute("title", "Karten, Dienste und Daten Kanton Solothurn");
        head.appendChild(opensearchdescription);

        // Get search params to control some parts of the gui.
        URLSearchParams searchParams = new URLSearchParams(location.search);

        if (searchParams.has(FILTER_PARAM_KEY)) {
            filter = searchParams.get(FILTER_PARAM_KEY);
        }

//        if (searchParams.has(IDENT_PARAM_KEY)) {
//            ident = searchParams.get(IDENT_PARAM_KEY);            
//        }

        // Add our "root" container
        container = div().id("container").element();
        body().add(container);

        // Add logo
        HTMLElement logoDiv = div().css("logo")
                .add(div()
                        .add(img()
                                .attr("src", location.protocol + "//" + location.host + location.pathname + "Logo.png")
                                .attr("alt", "Logo Kanton")))
                .element();
        container.appendChild(logoDiv);

        // Create a top level content div for everything except the logo.
        // Not sure why this was done this way. Or if it is necessary.
        topLevelContent = div().id("top-level-content").element();
        container.appendChild(topLevelContent);

        // Add breadcrumb
        Breadcrumb breadcrumb = Breadcrumb.create()
                .appendChild(Icons.ALL.home(), " Home ", (evt) -> {
                    DomGlobal.window.open("https://geo.so.ch/", "_self");
                })
                .appendChild(" Karten, Dienste und Daten ", (evt) -> {
                });
        topLevelContent.appendChild(breadcrumb.element());

        topLevelContent.appendChild(div().css("sodata-title").textContent("Karten, Dienste und Daten Kanton Solothurn").element());

        String infoString = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy "
                + "<a class='default-link' href='https://geoweb.so.ch/geodaten/index.php' target='_blank'>https://geoweb.so.ch/geodaten/index.php</a> eirmod "
                + "tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et "
                + "justo <a class='default-link' href='https://geo.so.ch/geodata' target='_blank'>https://geo.so.ch/geodata</a> "
                + "duo dolores et ea rebum. Stet clita kasd gubergren <a class='default-link' href='ftp://geo.so.ch/' target='_blank'>ftp://geo.so.ch/</a>, "
                + "no sea takimata sanctus est Lorem ipsum dolor sit amet.";
        topLevelContent.appendChild(div().css("info").innerHtml(SafeHtmlUtils.fromTrustedString(infoString)).element());

        Radio<String> radio1 = Radio.create("radio1", "Karten und Dienste").setColor(Color.RED_DARKEN_3).withGap().check();
        Radio<String> radio2 = Radio.create("radio2", "Daten").setColor(Color.RED_DARKEN_3).withGap();

        RadioGroup<String> horizontalRadioGroup =
                RadioGroup.<String>create("searchToggleRadio", "Suche nach Karten und Dienste oder Daten").setPaddingLeft("0px").setMarginTop("40px", true).setMarginBottom("0px", true)
                    //.setHelperText("Suche nach Karten und Dienste oder Daten")
                    .appendChild(radio1)
                    .appendChild(radio2)
                    .horizontal();
        
        topLevelContent.appendChild(horizontalRadioGroup.element());
        
        TextBox textBox = TextBox.create().setLabel("Suchbegriff");
        textBox.addLeftAddOn(Icons.ALL.search());
        textBox.setFocusColor(Color.RED_DARKEN_3);
        textBox.getInputElement().setAttribute("autocomplete", "off");
        textBox.getInputElement().setAttribute("spellcheck", "false");

        HTMLElement resetIcon = Icons.ALL.close().style().setCursor("pointer").get().element();
        resetIcon.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                textBox.clear();
                listStore.setData(datasets);

                removeQueryParam(FILTER_PARAM_KEY);
                //removeQueryParam(IDENT_PARAM_KEY);
            }
        });
        textBox.addRightAddOn(resetIcon);

        textBox.addEventListener("keyup", event -> {
                    // Ident-Parameter wird entfernt, sobald es ein Interagieren seitens Benutzer gibt.
                    //removeQueryParam(IDENT_PARAM_KEY);

                    if (textBox.getValue().trim().length() == 0) {
                        listStore.setData(datasets);

                        removeQueryParam(FILTER_PARAM_KEY);

                        return;
                    }

                    if (abortController != null) {
                        abortController.abort();
                    }

                    abortController = new AbortController();
                    final RequestInit init = RequestInit.create();
                    init.setSignal(abortController.signal);

                    DomGlobal.fetch("/datasets?query=" + textBox.getValue().toLowerCase(), init)
                            .then(
                                    response -> {
                                        if (!response.ok) {
                                            return null;
                                        }
                                        return response.text();
                                    }
                            )
                            .then(
                                    json -> {
                                        List<Dataset> filteredDatasets = mapper.read(json);

                                        Collections.sort(
                                                filteredDatasets, new Comparator<Dataset>() {
                                                    @Override
                                                    public int compare(Dataset o1, Dataset o2) {
                                                        return o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase());
                                                    }
                                                }
                                        );

                                        listStore.setData(filteredDatasets);

                                        //if (ident == null) {
                                        updateUrlLocation(FILTER_PARAM_KEY, textBox.getValue().trim());
                                        //}

                                        abortController = null;

                                        return null;
                                    }
                            )
                            .catch_(
                                    error -> {
                                        console.log(error);
                                        return null;
                                    }
                            );
                }
        );
        topLevelContent.appendChild(div().id("search-panel").add(div().id("suggestbox-div").add(textBox)).element());

        TableConfig<Dataset> tableConfig = new TableConfig<>();
        tableConfig
                //.setFixed(true)
                .addColumn(
                        ColumnConfig.<Dataset>create("title", "Name")
                                .setShowTooltip(false)
                                .textAlign("left")
                                //.asHeader()
                                .setCellRenderer(
                                        cell -> TextNode.of(cell.getTableRow().getRecord().getTitle()))
                )
//                .addColumn(
//                        ColumnConfig.<Dataset>create("owner", "Zuständige Stelle")
//                            .setShowTooltip(false)
//                            .textAlign("left")
//                            .setCellRenderer(
//                                   cell -> {                                       
//                                       HTMLElement regionSelectionElement = a()
//                                               .css("black-link")
//                                               .textContent(cell.getRecord().getProvider())
//                                               .attr("href", cell.getRecord().getOwnerUrl())
//                                               .attr("target", "_blank")
//                                               .element();
//    
//                                       return regionSelectionElement;
//
//                                  })
//                        )

//                .addColumn(
//                        ColumnConfig.<Dataset>create("lastEditingDate", "Aktualisiert")
//                            .setShowTooltip(false)
//                            .textAlign("left")
//                            .setCellRenderer(
//                                    cell -> {
//                                        Date date = DateTimeFormat.getFormat("yyyy-MM-dd")
//                                                .parse(cell.getTableRow().getRecord().getLastEditingDate());
//                                        String dateString = DateTimeFormat.getFormat("dd.MM.yyyy").format(date);
//                                        return TextNode.of(dateString);
//                                    })
//                        )
                .addColumn(
                        ColumnConfig.<Dataset>create("model", "Datenmodell")
                                .setShowTooltip(false)
                                .textAlign("center")
                                .setCellRenderer(
                                        cell -> {
                                            if (cell.getRecord().getModel() != null && cell.getRecord().getModel().length() > 0) {
                                                HTMLElement modelLinkElement = div()
                                                        .add(Icons.ALL.launch_mdi().style().setCursor("pointer")).element();
                                                HTMLElement modelLink = a()
                                                        .attr("class", "icon-link")
                                                        .attr("href", "https://geo.so.ch/modelfinder/?expanded=true&query=" + cell.getRecord().getModel())
                                                        .attr("target", "_blank").add(modelLinkElement).element();

                                                return modelLink;
                                            }
                                            return span().element();
                                        })
                )
                .addColumn(
                        ColumnConfig.<Dataset>create("formats", "Daten herunterladen")
                                .setShowTooltip(false)
                                .textAlign("left")
                                .setCellRenderer(
                                        cell -> {
                                            HTMLElement badgesElement = div().element();

                                            if (cell.getRecord().getSubunits() != null) {
                                                HTMLElement regionSelectionElement = a().css("default-link")
                                                        .textContent("Gebietsauswahl notwendig")
                                                        .attr("href", cell.getRecord().getSubunits())
                                                        .attr("target", "_blank")
                                                        .element();
                                                return regionSelectionElement;
                                            } else {
                                                for (String fileStr : cell.getRecord().getFileFormats()) {
                                                    badgesElement.appendChild(a().css("badge-link")
                                                            .attr("href", DATA_BASE_URL + cell.getRecord().getId() + "_" + fileStr + ".zip")
                                                            .attr("target", "_blank")
                                                            .add(Badge.create(formatLookUp.get(fileStr))
                                                                    .setBackground(Color.GREY_LIGHTEN_2).style()
                                                                    .setMarginRight("10px").setMarginTop("5px")
                                                                    .setMarginBottom("5px").get().element())
                                                            .element());
                                                }
                                                return badgesElement;
                                            }
                                        })
                );
//                .addColumn(
//                        ColumnConfig.<Dataset>create("services", "Servicelinks")
//                            .setShowTooltip(false)
//                            .textAlign("center").setCellRenderer(
//                                    cell -> {
//                                        HTMLElement serviceLinkElement = div()
//                                                .add(Icons.ALL.information_outline_mdi().style().setCursor("pointer")).element();
//                                        serviceLinkElement.addEventListener("click", new EventListener() {
//                                            @Override
//                                            public void handleEvent(Event evt) {
//                                                openServiceLinkDialog(cell.getRecord());
//                                            }
//                                        });
//                                        return serviceLinkElement;
//                                    })
//                        );

        listStore = new LocalListDataStore<>();
        listStore.setData(datasets);

        datasetTable = new DataTable<>(tableConfig, listStore);
        datasetTable.setId("dataset-table");
        datasetTable.noStripes();
        //datasetTable.noHover();
        datasetTable.load();

        topLevelContent.appendChild(datasetTable.element());

//        if (ident != null && ident.trim().length() > 0) {
//            textBox.setValue(ident);
//            textBox.element().dispatchEvent(new KeyboardEvent("keyup"));
//            
//            ident = null;
//            removeQueryParam(QUERY_PARAM_KEY);
//        } else 
        if (filter != null && filter.trim().length() > 0) {
            textBox.setValue(filter);
            textBox.element().dispatchEvent(new KeyboardEvent("keyup"));
        }
    }

    private void openMetadataDialog(Dataset dataset) {
        ModalDialog modal = ModalDialog.create(dataset.getTitle()).setAutoClose(true);
        modal.css("modal-object");

        MetadataElement metaDataElement = new MetadataElement(dataset);
        modal.add(metaDataElement);

        Button closeButton = Button.create(messages.close().toUpperCase()).linkify();
        closeButton.removeWaves();
        closeButton.setBackground(Color.RED_DARKEN_3);
        EventListener closeModalListener = (evt) -> modal.close();
        closeButton.addClickListener(closeModalListener);
        modal.appendFooterChild(closeButton);
        modal.open();

        closeButton.blur();
    }

    private void openRegionSelectionDialog(Dataset dataset) {
        ModalDialog modal = ModalDialog.create("Gebietsauswahl: " + dataset.getTitle()).setAutoClose(true);
        modal.css("modal-object");

        TabsPanel tabsPanel = TabsPanel.create().setColor(Color.RED_DARKEN_3);
        Tab selectionTab = Tab.create(Icons.ALL.map_outline_mdi(), "AUSWAHL");
        Tab downloadTab = Tab.create(Icons.ALL.file_download_outline_mdi(), "HERUNTERLADEN");
        tabsPanel.appendChild(selectionTab);
        tabsPanel.appendChild(downloadTab);

        HTMLDivElement mapDiv = div().id("map").element();
        modal.getBodyElement().appendChild(div().css("modal-body-paragraph")
                .textContent("Sie können einzelne Datensätze mit einem Klick in die Karte aus- und abwählen. "
                        + "Im Reiter 'Herunterladen' können Sie die Daten anschliessend herunterladen. "
                        + "Wünschen Sie viele Datensätze herunterzuladen ... Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor "
                        + "invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et."));

        selectionTab.appendChild(mapDiv);

        // FIXME TODO
        String subunits = dataset.getSubunits();
        DomGlobal.fetch("/subunits/" + subunits)
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
                .addColumn(ColumnConfig.<Feature>create("title", "Name").setShowTooltip(false).textAlign("left")
                        .setCellRenderer(cell -> TextNode.of(cell.getTableRow().getRecord().get("title"))))
                .addColumn(ColumnConfig.<Feature>create("lastEditingDate", "Aktualisiert").setShowTooltip(false).textAlign("left")
                        .setCellRenderer(cell -> {
                            Date date;
                            String dateString;
                            if (cell.getRecord().get("last_editing_date").toString().length() < 8) {
                                date = DateTimeFormat.getFormat("yyyy-MM").parse(cell.getRecord().get("last_editing_date"));
                                dateString = DateTimeFormat.getFormat("MMMM yyyy").format(date);
                            } else {
                                date = DateTimeFormat.getFormat("yyyy-MM-dd").parse(cell.getRecord().get("last_editing_date"));
                                dateString = DateTimeFormat.getFormat("dd.MM.yyyy").format(date);
                            }
                            return TextNode.of(dateString);
                        }))
                .addColumn(ColumnConfig.<Feature>create("formats", "Daten herunterladen").setShowTooltip(false).textAlign("left")
                        .setCellRenderer(cell -> {
                            HTMLElement badgesElement = div().element();
                            for (String fileFormatsStr : dataset.getFileFormats()) {
                                badgesElement.appendChild(a().css("badge-link")
                                        .attr("href", "/dataset/" + cell.getRecord().getId() + "_" + fileFormatsStr + ".zip") // TODO Was kommt woher?
                                        .attr("target", "_blank")
                                        .add(Badge.create(formatLookUp.get(fileFormatsStr))
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

        Button closeButton = Button.create("SCHLIESSEN").linkify();
        closeButton.removeWaves();
        closeButton.setBackground(Color.RED_DARKEN_3);
        EventListener closeModalListener = (evt) -> modal.close();
        closeButton.addClickListener(closeModalListener);
        modal.appendFooterChild(closeButton);
        modal.large().open();

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
                        return o1.get("title").toString().toLowerCase().compareTo(o2.get("title").toString().toLowerCase());
                    }
                });

                subunitListStore.setData(featuresList);
            }
        });

        map = MapPresets.getBlakeAndWhiteMap(mapDiv.id); //, subunitsWmsLayer);
        map.addSingleClickListener(new MapSingleClickListener());

        closeButton.blur();
    }

    public final class MapSingleClickListener implements ol.event.EventListener<MapBrowserEvent> {
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

    private void openServiceLinkDialog(Dataset dataset) {
        ModalDialog modal = ModalDialog.create("Servicelinks").setAutoClose(true);

        modal.appendChild(InfoBox.create(Icons.ALL.map(), "WMS", "https://geo.so.ch/wms").setIconBackground(Color.RED_DARKEN_3));
        modal.appendChild(InfoBox.create(Icons.ALL.file_download_mdi(), "WFS", "https://geo.so.ch/wfs")
                .setIconBackground(Color.RED_DARKEN_3));
        modal.appendChild(
                InfoBox.create(Icons.ALL.file_download_mdi(), "Data Service", "https://geo.so.ch/api/data/v1/api/")
                        .setIconBackground(Color.RED_DARKEN_3));

        Button closeButton = Button.create("SCHLIESSEN").linkify();
        closeButton.removeWaves();
        closeButton.setBackground(Color.RED_DARKEN_3);
        EventListener closeModalListener = (evt) -> modal.close();
        closeButton.addClickListener(closeModalListener);
        modal.appendFooterChild(closeButton);
        modal.open();

        closeButton.blur();
    }

    private void createVectorLayers(Feature[] features) {
        removeVectorLayer(SUBUNIT_VECTOR_LAYER_ID);
        removeVectorLayer(SELECTED_VECTOR_LAYER_ID);

        {
            Style style = new Style();
            Stroke stroke = new Stroke();
            stroke.setWidth(4);
            //stroke.setColor(new ol.color.Color(56, 142, 60, 1.0)); 
            stroke.setColor(new ol.color.Color(78, 127, 217, 1.0));
            //stroke.setColor(new ol.color.Color(230, 0, 0, 0.6));
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