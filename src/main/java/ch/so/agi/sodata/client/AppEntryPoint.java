package ch.so.agi.sodata.client;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
import org.gwtproject.safehtml.shared.SafeHtmlUtils;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.i18n.client.DateTimeFormat;

import ch.so.agi.sodata.shared.Dataset;
import ch.so.agi.sodata.shared.DatasetTable;
import ch.so.agi.sodata.shared.SettingsResponse;
import ch.so.agi.sodata.shared.SettingsService;
import ch.so.agi.sodata.shared.SettingsServiceAsync;
import elemental2.core.Global;
import elemental2.dom.CSSProperties;
import elemental2.dom.DomGlobal;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.Headers;
import elemental2.dom.RequestInit;
import ol.Feature;
import ol.Map;
import ol.layer.Base;
import ol.layer.Vector;

public class AppEntryPoint implements EntryPoint {
    private MyMessages messages = GWT.create(MyMessages.class);
    private final SettingsServiceAsync settingsService = GWT.create(SettingsService.class);

    // Application settings
    private String myVar;

    // Format settings
    private NumberFormat fmtDefault = NumberFormat.getDecimalFormat();
    private NumberFormat fmtPercent = NumberFormat.getFormat("#0.0");

    private Map map;
//    private HTMLElement mapDiv;
//    private ModalDialog modal;
    private HTMLElement container;
    private HTMLElement topLevelContent;
    private HTMLElement datasetContent;
    private Dataset[] datasets;
    private List<Dataset> datasetList;

    private LocalListDataStore<Dataset> listStore;
    private DataTable<Dataset> datasetTable;

    private HashMap<String, String> formats = new HashMap<String, String>();

    public void onModuleLoad() {
        formats.put("xtf", "INTERLIS");
        formats.put("shp", "Shapefile");
        formats.put("dxf", "DXF");
        formats.put("gpkg", "GeoPackage");
        formats.put("gtiff", "GeoTIFF");

        settingsService.settingsServer(new AsyncCallback<SettingsResponse>() {
            @Override
            public void onFailure(Throwable caught) {
                console.error(caught.getMessage());
                DomGlobal.window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(SettingsResponse result) {
                myVar = (String) result.getSettings().get("MY_VAR");

                // Alle vorhandenen Datensätze anfordern.
                RequestInit requestInit = RequestInit.create();
                Headers headers = new Headers();
                headers.append("Content-Type", "application/x-www-form-urlencoded");
                requestInit.setHeaders(headers);

                DomGlobal.fetch("datasets", requestInit).then(response -> {
                    if (!response.ok) {
                        return null;
                    }
                    return response.text();
                }).then(json -> {
                    datasets = (Dataset[]) Global.JSON.parse(json);
                    datasetList = Arrays.asList(datasets);

                    Collections.sort(datasetList, new Comparator<Dataset>() {
                        @Override
                        public int compare(Dataset o1, Dataset o2) {
                            return o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase());
                        }
                    });

                    // GUI initialisieren.
                    init();
                    return null;
                }).catch_(error -> {
                    console.log(error);
                    return null;
                });
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void init() {
        Theme theme = new Theme(ColorScheme.RED);
        theme.apply();

        container = div().id("container").element();

        HTMLElement logoDiv = div().css("logo").element();
        HTMLElement logoCanton = div()
                .add(img().attr("src", GWT.getHostPageBaseURL() + "Logo.png").attr("alt", "Logo Kanton")).element();
        logoDiv.appendChild(logoCanton);
        container.appendChild(logoDiv);

        topLevelContent = div().id("top-level-content").element();

        // Breadcrumb
        Breadcrumb breadcrumb = Breadcrumb.create().appendChild(Icons.ALL.home(), " Home ", (evt) -> {
            Window.open("https://geo.so.ch/", "_self", null);
        }).appendChild(" Geodaten ", (evt) -> {
        });
        topLevelContent.appendChild(breadcrumb.element());

        topLevelContent.appendChild(div().css("sodata-title").textContent("Geodaten Kanton Solothurn").element());
//        container.appendChild(div().css("sodata-title").textContent("Geodaten Kanton Solothurn").element());

        String infoString = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy "
                + "<a class='generic-link' href='https://geoweb.so.ch/geodaten/index.php' target='_blank'>https://geoweb.so.ch/geodaten/index.php</a> eirmod "
                + "tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et "
                + "justo <a class='generic-link' href='https://geo.so.ch/geodata' target='_blank'>https://geo.so.ch/geodata</a> "
                + "duo dolores et ea rebum. Stet clita kasd gubergren <a class='generic-link' href='ftp://geo.so.ch/' target='_blank'>ftp://geo.so.ch/</a>, "
                + "no sea takimata sanctus est Lorem ipsum dolor sit amet.";
        topLevelContent.appendChild(div().css("info").innerHtml(SafeHtmlUtils.fromTrustedString(infoString)).element());
//        container.appendChild(div().id("info").innerHtml(SafeHtmlUtils.fromTrustedString(infoString)).element());

        // Suche
        TextBox textBox = TextBox.create().setLabel("Suchbegriff");
        textBox.addLeftAddOn(Icons.ALL.search());
        textBox.setFocusColor(Color.RED);
        textBox.getInputElement().setAttribute("autocomplete", "off");
        textBox.getInputElement().setAttribute("spellcheck", "false");

        HTMLElement resetIcon = Icons.ALL.close().style().setCursor("pointer").get().element();
        resetIcon.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(elemental2.dom.Event evt) {
                textBox.clear();
                listStore.setData(Arrays.asList(datasets));
            }
        });
        textBox.addRightAddOn(resetIcon);

        textBox.addEventListener("keyup", event -> {
            if (textBox.getValue().trim().length() == 0) {
                return;
            }

            DomGlobal.fetch("/datasets?query=" + textBox.getValue().toLowerCase()).then(response -> {
                if (!response.ok) {
                    return null;
                }
                return response.text();
            }).then(json -> {
                Dataset[] searchResults = (Dataset[]) Global.JSON.parse(json);
                List<Dataset> searchResultList = Arrays.asList(searchResults);
                listStore.setData(searchResultList);
                return null;
            }).catch_(error -> {
                console.log(error);
                return null;
            });
        });
        topLevelContent.appendChild(div().id("search-panel").add(div().id("suggestbox-div").add(textBox)).element());

        // Liste (sämtlicher) Datensätze
        TableConfig<Dataset> tableConfig = new TableConfig<>();
        tableConfig
                .addColumn(ColumnConfig.<Dataset>create("title", "Name").setShowTooltip(false).textAlign("left")
                        .setCellRenderer(cell -> TextNode.of(cell.getTableRow().getRecord().getTitle())))
                .addColumn(ColumnConfig.<Dataset>create("lastEditingDate", "Aktualisiert").setShowTooltip(false)
                        .textAlign("left").setCellRenderer(cell -> {
                            Date date = DateTimeFormat.getFormat("yyyy-MM-dd")
                                    .parse(cell.getTableRow().getRecord().getLastEditingDate());
                            String dateString = DateTimeFormat.getFormat("dd.MM.yyyy").format(date);
                            return TextNode.of(dateString);

                        }))
                .addColumn(ColumnConfig.<Dataset>create("metadata", "Metadaten").setShowTooltip(false)
                        .textAlign("center").setCellRenderer(cell -> {
                            HTMLElement metadataLinkElement = div()
                                    .add(Icons.ALL.information_outline_mdi().style().setCursor("pointer")).element();
                            metadataLinkElement.addEventListener("click", new EventListener() {
                                @Override
                                public void handleEvent(elemental2.dom.Event evt) {
                                    openMetadataDialog(cell.getRecord());
                                }
                            });
                            return metadataLinkElement;
                        }))
                .addColumn(ColumnConfig.<Dataset>create("formats", "Daten herunterladen").setShowTooltip(false)
                        .textAlign("left").setCellRenderer(cell -> {
                            HTMLElement badgesElement = div().element();

                            if (cell.getRecord().getSubunits() != null) {
                                HTMLElement regionSelectionElement = a().css("generic-link")
                                        .textContent("Gebietsauswahl nötig").element();
                                regionSelectionElement.addEventListener("click", new EventListener() {
                                    @Override
                                    public void handleEvent(elemental2.dom.Event evt) {
                                        openRegionSelectionDialog(cell.getRecord());
                                    }

                                });
                                return regionSelectionElement;
                            } else {
                                for (String fileStr : cell.getRecord().getFiles()) {
                                    badgesElement.appendChild(a().css("badge-link")
                                            .attr("href",
                                                    "/dataset/" + cell.getRecord().getId() + "_" + fileStr + ".zip")
                                            .add(Badge.create(formats.get(fileStr))
                                                    .setBackground(Color.GREY_LIGHTEN_2).style()
                                                    .setMarginRight("10px").setMarginTop("5px")
                                                    .setMarginBottom("5px").get().element())
                                            .element());
                                }
                                return badgesElement;
                            }
                        }))
                .addColumn(ColumnConfig.<Dataset>create("services", "Servicelink").setShowTooltip(false)
                        .textAlign("center").setCellRenderer(cell -> {
                            HTMLElement serviceLinkElement = div()
                                    .add(Icons.ALL.information_outline_mdi().style().setCursor("pointer")).element();
                            serviceLinkElement.addEventListener("click", new EventListener() {
                                @Override
                                public void handleEvent(elemental2.dom.Event evt) {
                                    openServiceLinkDialog(cell.getRecord());
                                }
                            });
                            return serviceLinkElement;
                        }));

        listStore = new LocalListDataStore<>();
        listStore.setData(Arrays.asList(datasets));

        datasetTable = new DataTable<>(tableConfig, listStore);
        datasetTable.setId("dataset-table");
        datasetTable.noStripes();
        datasetTable.noHover();
        datasetTable.load();

        topLevelContent.appendChild(datasetTable.element());

        container.appendChild(topLevelContent);
        body().add(container);
    }

    private void openMetadataDialog(Dataset dataset) {
        ModalDialog modal = ModalDialog.create("Metadaten: " + dataset.getTitle()).setAutoClose(true);
        modal.style().setMaxHeight("calc(100% - 120px)");
        modal.style().setOverFlowY("auto");

        modal.appendChild(h(4, "Beschreibung"));
        modal.appendChild(p().style("padding-bottom:20px;").textContent(dataset.getShortDescription()));

        modal.appendChild(h(4, "Weitere Metadaten"));

        HTMLElement geocatLink = div().add(Icons.ALL.open_in_new().style().setCursor("pointer")).element();
        geocatLink.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(elemental2.dom.Event evt) {
                Window.open(dataset.getFurtherMetadata(), "_blank", null);
            }
        });

        HTMLElement pdfLink = div().add(a()
                .attr("href", "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf")
                .attr("target", "_blank").add(Icons.ALL.file_download_outline_mdi().style().setCursor("pointer")))
                .element();

        Row metaRow = Row.create();
        metaRow.addColumn(Column.span3().appendChild(div().textContent("geocat.ch")))
                .addColumn(Column.span9().appendChild(geocatLink))
                .addColumn(Column.span3().appendChild(div().textContent("PDF")))
                .addColumn(Column.span9().appendChild(pdfLink));

        modal.appendChild(p().style("padding-bottom:20px;").add(metaRow));

        if (dataset.getTables() != null) {
            modal.appendChild(h(4, "Inhalt"));

            HTMLElement tables = div().element();

            // Variante 1
            for (DatasetTable datasetTable : dataset.getTables()) {
                HTMLElement details = (HTMLElement) DomGlobal.document.createElement("details");
                details.style.paddingBottom = CSSProperties.PaddingBottomUnionType.of("5px");
                HTMLElement summary = (HTMLElement) DomGlobal.document.createElement("summary");
                summary.style.display = "list-item"; // needed by firefox
                summary.textContent = datasetTable.getTitle();
                HTMLElement p = p().style("padding-top:5px;padding-bottom:5px;").textContent(datasetTable.getDescription()).element();
                details.appendChild(summary);
                details.appendChild(p);
                tables.appendChild(details);
            }

            // Variante 2
//            for (DatasetTable datasetTable : dataset.getTables()) {
//                HTMLElement table = p().style("padding-top:5px;padding-bottom:5px;")
//                        .textContent(datasetTable.getTitle() + ": " + datasetTable.getDescription()).element();
//                tables.appendChild(table);
//            }

            modal.appendChild(p().style("padding-bottom:20px;").add(tables)); 
        }

        Button closeButton = Button.create("SCHLIESSEN").linkify();
        closeButton.setBackground(Color.RED);
        EventListener closeModalListener = (evt) -> modal.close();
        closeButton.addClickListener(closeModalListener);
        modal.appendFooterChild(closeButton);
        modal.open();

        closeButton.blur();
    }

    private void openRegionSelectionDialog(Dataset dataset) {
        ModalDialog modal = ModalDialog.create("Gebietsauswahl: " + dataset.getTitle()).setAutoClose(true);
        modal.style().setMaxHeight("calc(100% - 120px)");
        modal.style().setOverFlowY("auto");

        String subunitsWmsLayer = dataset.getSubunits();

        TabsPanel tabsPanel = TabsPanel.create().setColor(Color.RED);
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

        TableConfig<Feature> tableConfig = new TableConfig<>();
        tableConfig
            .addColumn(ColumnConfig.<Feature>create("title", "Name").setShowTooltip(false).textAlign("left")
                .setCellRenderer(cell -> TextNode.of(cell.getTableRow().getRecord().get("aname"))))
            .addColumn(ColumnConfig.<Feature>create("lastEditingDate", "Aktualisiert").setShowTooltip(false).textAlign("left")
                .setCellRenderer(cell -> {
                    if (cell.getRecord().get("updatedatum") != null) {
                        Date date = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss").parse(cell.getRecord().get("updatedatum"));
                        String dateString = DateTimeFormat.getFormat("dd.MM.yyyy").format(date);
                        return TextNode.of(dateString);
                    } else {
                        Date date = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss").parse(cell.getRecord().get("erstellungsdatum"));
                        String dateString = DateTimeFormat.getFormat("dd.MM.yyyy").format(date);
                        return TextNode.of(dateString);
                    }
                }))
            .addColumn(ColumnConfig.<Feature>create("formats", "Daten herunterladen").setShowTooltip(false).textAlign("left")
                .setCellRenderer(cell -> {
                    HTMLElement badgesElement = div().element();
                    for (String fileStr : dataset.getFiles()) {
                        badgesElement.appendChild(a().css("badge-link")
                                .attr("href",
                                        "/dataset/" + cell.getRecord().getId() + "_" + fileStr + ".zip")
                                .add(Badge.create(formats.get(fileStr))
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
        closeButton.setBackground(Color.RED);
        EventListener closeModalListener = (evt) -> modal.close();
        closeButton.addClickListener(closeModalListener);
        modal.appendFooterChild(closeButton);
        modal.large().open();

        downloadTab.addClickListener(new EventListener() {
            @Override
            public void handleEvent(elemental2.dom.Event evt) {
                Vector vectorLayer = (Vector) getMapLayerById("vector");
                ol.source.Vector vectorSource = vectorLayer.getSource();
                ol.Collection<Feature> features = vectorSource.getFeaturesCollection();

                List<Feature> featuresList = new ArrayList<Feature>();
                for (int i = 0; i < features.getLength(); i++) {
                    Feature feature = features.item(i);
                    feature.set("files", dataset.getFiles());
                    featuresList.add(feature);
                }
                
                Collections.sort(featuresList, new Comparator<Feature>() {
                    @Override
                    public int compare(Feature o1, Feature o2) {
                        return o1.get("aname").toString().toLowerCase().compareTo(o2.get("aname").toString().toLowerCase());
                    }
                });
                
                subunitListStore.setData(featuresList);
            }
        });

        map = MapPresets.getBlackAndWhiteMap(mapDiv.id, subunitsWmsLayer);
        closeButton.blur();
    }

    private void openServiceLinkDialog(Dataset dataset) {
        ModalDialog modal = ModalDialog.create("Servicelink").setAutoClose(true);

        modal.appendChild(InfoBox.create(Icons.ALL.map(), "WMS", "https://geo.so.ch/wms").setIconBackground(Color.RED));
        modal.appendChild(InfoBox.create(Icons.ALL.file_download_mdi(), "WFS", "https://geo.so.ch/wfs")
                .setIconBackground(Color.RED));
        modal.appendChild(
                InfoBox.create(Icons.ALL.file_download_mdi(), "Data Service", "https://geo.so.ch/api/data/v1/api/")
                        .setIconBackground(Color.RED));

        Button closeButton = Button.create("SCHLIESSEN").linkify();
        closeButton.setBackground(Color.RED);
        EventListener closeModalListener = (evt) -> modal.close();
        closeButton.addClickListener(closeModalListener);
        modal.appendFooterChild(closeButton);
        modal.open();

        closeButton.blur();
    }

    // Get Openlayers map layer by id.
    private Base getMapLayerById(String id) {
        ol.Collection<Base> layers = map.getLayers();
        for (int i = 0; i < layers.getLength(); i++) {
            Base item = layers.item(i);
            try {
                String layerId = item.get("id");
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

    private static native void updateURLWithoutReloading(String newUrl) /*-{
                                                                        console.log("fubar");
                                                                        $wnd.history.pushState(newUrl, "", newUrl);
                                                                        }-*/;
}