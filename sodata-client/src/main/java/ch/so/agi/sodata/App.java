package ch.so.agi.sodata;

import static elemental2.dom.DomGlobal.console;
import static elemental2.dom.DomGlobal.fetch;
import static org.jboss.elemento.Elements.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.dominokit.domino.ui.badges.Badge;
import org.dominokit.domino.ui.breadcrumbs.Breadcrumb;
import org.dominokit.domino.ui.datatable.ColumnConfig;
import org.dominokit.domino.ui.datatable.DataTable;
import org.dominokit.domino.ui.datatable.TableConfig;
import org.dominokit.domino.ui.datatable.store.LocalListDataStore;
import org.dominokit.domino.ui.forms.TextBox;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.style.ColorScheme;
import org.dominokit.domino.ui.themes.Theme;
import org.dominokit.domino.ui.utils.TextNode;

import com.google.gwt.core.client.GWT;
import org.gwtproject.safehtml.shared.SafeHtmlUtils;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.client.DateTimeFormat;

import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLElement;
import elemental2.dom.Location;
import ol.Extent;
import ol.Map;
import ol.OLFactory;
import ol.proj.Projection;
import ol.proj.ProjectionOptions;
import proj4.Proj4;

import ch.so.agi.sodata.Dataset;

public class App implements EntryPoint {

    // Application settings
    private String myVar;

    // Format settings
    private NumberFormat fmtDefault = NumberFormat.getDecimalFormat();
    private NumberFormat fmtPercent = NumberFormat.getFormat("#0.0");

    private HTMLElement container;
    private HTMLElement topLevelContent;
    private HTMLElement datasetContent;
    
    private List<Dataset> datasets;
    private LocalListDataStore<Dataset> listStore;
    private DataTable<Dataset> datasetTable;

    private HashMap<String, String> formatLookUp = new HashMap<String, String>();

    // Projection
    //private static final String EPSG_2056 = "EPSG:2056";
    //private static final String EPSG_4326 = "EPSG:4326"; 
    //private Projection projection;

    public static interface DatasetMapper extends ObjectMapper<List<Dataset>> {}

    private String MAP_DIV_ID = "map";
    private Map map;

	public void onModuleLoad() {
	    formatLookUp.put("xtf", "INTERLIS");
	    formatLookUp.put("shp", "Shapefile");
	    formatLookUp.put("dxf", "DXF");
	    formatLookUp.put("gpkg", "GeoPackage");
	    formatLookUp.put("gtiff", "GeoTIFF");

	    DatasetMapper mapper = GWT.create(DatasetMapper.class);   

        DomGlobal.fetch("/datasets")
        .then(response -> {
            if (!response.ok) {
                DomGlobal.window.alert(response.statusText + ": " + response.body);
                return null;
            }
            return response.text();
        })
        .then(json -> {
            console.log(json);
            List<Dataset> datasets = mapper.read(json);
            
            Collections.sort(datasets, new Comparator<Dataset>() {
                @Override
                public int compare(Dataset o1, Dataset o2) {
                    return o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase());
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
	    
        // Change Domino UI color scheme.
        Theme theme = new Theme(ColorScheme.RED);
        theme.apply();

        container = div().id("container").element();
        body().add(container);
        
        Location location = DomGlobal.window.location;
        if (location.pathname.length() > 1) {
            location.pathname += "/"; 
        }
        HTMLElement logoDiv = div().css("logo")
                .add(div()
                        .add(img().attr("src", location.protocol + "//" + location.host + location.pathname + "Logo.png").attr("alt", "Logo Kanton")).element()).element();
        container.appendChild(logoDiv);

        topLevelContent = div().id("top-level-content").element();
        container.appendChild(topLevelContent);

        Breadcrumb breadcrumb = Breadcrumb.create().appendChild(Icons.ALL.home(), " Home ", (evt) -> {
            DomGlobal.window.open("https://geo.so.ch/", "_self");
        }).appendChild(" Geodaten ", (evt) -> {
        });
        topLevelContent.appendChild(breadcrumb.element());

        topLevelContent.appendChild(div().css("sodata-title").textContent("Geodaten Kanton Solothurn").element());

        String infoString = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy "
              + "<a class='default-link' href='https://geoweb.so.ch/geodaten/index.php' target='_blank'>https://geoweb.so.ch/geodaten/index.php</a> eirmod "
              + "tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et "
              + "justo <a class='default-link' href='https://geo.so.ch/geodata' target='_blank'>https://geo.so.ch/geodata</a> "
              + "duo dolores et ea rebum. Stet clita kasd gubergren <a class='default-link' href='ftp://geo.so.ch/' target='_blank'>ftp://geo.so.ch/</a>, "
              + "no sea takimata sanctus est Lorem ipsum dolor sit amet.";
        topLevelContent.appendChild(div().css("info").innerHtml(SafeHtmlUtils.fromTrustedString(infoString)).element());

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
                //listStore.setData(Arrays.asList(datasets));
            }
        });
        textBox.addRightAddOn(resetIcon);

        textBox.addEventListener("keyup", event -> {
            if (textBox.getValue().trim().length() == 0) {
                console.log("leeeeeeer");
                //listStore.setData(Arrays.asList(datasets));
                return;
            }

            DomGlobal.fetch("/datasets?query=" + textBox.getValue().toLowerCase()).then(response -> {
                if (!response.ok) {
                    return null;
                }
                return response.text();
            }).then(json -> {
                //Dataset[] searchResults = (Dataset[]) Global.JSON.parse(json);
                //List<Dataset> searchResultList = Arrays.asList(searchResults);
                //listStore.setData(searchResultList);
                return null;
            }).catch_(error -> {
                console.log(error);
                return null;
            });
        });
        topLevelContent.appendChild(div().id("search-panel").add(div().id("suggestbox-div").add(textBox)).element());

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
                                    //openMetadataDialog(cell.getRecord());
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
                                        //openRegionSelectionDialog(cell.getRecord());
                                    }

                                });
                                return regionSelectionElement;
                            } else {
                                for (String fileStr : cell.getRecord().getFileFormats()) {
                                    badgesElement.appendChild(a().css("badge-link")
                                            .attr("href",
                                                    "/dataset/" + cell.getRecord().getId() + "_" + fileStr + ".zip")
                                            .add(Badge.create(formatLookUp.get(fileStr))
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
                                    //openServiceLinkDialog(cell.getRecord());
                                }
                            });
                            return serviceLinkElement;
                        }));

        listStore = new LocalListDataStore<>();
        listStore.setData(datasets);

        datasetTable = new DataTable<>(tableConfig, listStore);
        datasetTable.setId("dataset-table");
        datasetTable.noStripes();
        datasetTable.noHover();
        datasetTable.load();

        topLevelContent.appendChild(datasetTable.element());

        
        
        // Add the Openlayers map (element) to the body.
        /*
        HTMLElement mapElement = div().id(MAP_DIV_ID).element();
        body().add(mapElement);
        map = MapPresets.getColorMap(MAP_DIV_ID);
        */
        
        console.log("fubar");
	}
}