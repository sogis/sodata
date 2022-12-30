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

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.i18n.client.DateTimeFormat;

import elemental2.core.Global;
import elemental2.dom.AbortController;
import elemental2.dom.CSSProperties;
import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
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
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class App implements EntryPoint {
    // Internationalization
    private MyMessages messages = GWT.create(MyMessages.class);

    // Client application settings
    private String myVar;
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
    
    // Data-/Maplayers container elements
    private DataTabElement dataTabElement;
    private MapLayerTabElement mapLayerTabElement;

    // Format lookup and sort order
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

    // Abort controller for fetching from server
    private AbortController abortController = null;

    public void onModuleLoad() {

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
        
        init();
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

        topLevelContent.appendChild(div().css("sodata-title").textContent("Karten und Geodaten Kanton Solothurn").element());

        String infoString = "Geodaten vom Kanton Solothurn können kostenlos heruntergeladen werden. Die Vektordaten sowie die Rasterdaten werden "
                + "in vordefinierten Formaten und Gebieten (Kanton, Gemeinde oder andere) angeboten. Bei der Gebietseinteilung \"Gemeinde oder andere\" "
                + "kann der Benutzer das gewünschte Gebiet selber wählen. Weitere Informationen zur Datenliste und alternativen Bezugsmöglichkeiten "
                + "finden Sie <a class='default-link' href='https://geo.so.ch/' target='_blank'>hier</a>."
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
                
                if (dataTabElement != null) {
                    dataTabElement.resetTable();
                }
                if (mapLayerTabElement != null) {
                    mapLayerTabElement.resetTable();
                }

                textBox.clear();
                removeQueryParam(FILTER_PARAM_KEY);
            }
        }, true);
        textBox.addRightAddOn(resetIcon);
        
        // FIXME: textBox braucht es nicht in den Element. Den Wert kann man mit den update-Methoden übergeben.
        
        textBox.addEventListener("keyup", event -> {
            if (textBox.getValue().trim().length() > 0 && textBox.getValue().trim().length() <= 2) {
                if (dataTabElement != null) {
                    dataTabElement.resetTable();
                }
                if (mapLayerTabElement != null) {
                    mapLayerTabElement.resetTable();
                }
                return;
            }
            
            if (textBox.getValue().trim().length() == 0) {
                if (dataTabElement != null) {
                    dataTabElement.resetTable();
                }
                if (mapLayerTabElement != null) {
                    mapLayerTabElement.resetTable();
                }
                removeQueryParam(FILTER_PARAM_KEY);
                return;
            }
            
            updateUrlLocation(FILTER_PARAM_KEY, textBox.getValue().trim());
            
            if (dataTabElement != null) {
                dataTabElement.updateTable(textBox.getValue().trim());
            }            
            
            if (mapLayerTabElement != null) {
                mapLayerTabElement.updateTable(textBox.getValue().trim());
            }
        });
        
        topLevelContent.appendChild(div().id("search-panel").add(div().id("suggestbox-div").add(textBox)).element());

        // Add tabs
        TabsPanel tabsPanel = TabsPanel.create().setColor(Color.RED_DARKEN_3).setMarginTop("45px");
        Tab mapsTab = Tab.create(Icons.ALL.map_outline_mdi(), messages.tabs_header_maps().toUpperCase()).setWidth("180px").id("maps-tab");
        Tab dataTab = Tab.create(Icons.ALL.file_download_outline_mdi(), messages.tabs_header_data().toUpperCase()).setWidth("180px");
        tabsPanel.appendChild(mapsTab);
        tabsPanel.appendChild(dataTab);

        mapLayerTabElement = new MapLayerTabElement(messages);
        mapLayerTabElement.element().style.marginTop = CSSProperties.MarginTopUnionType.of("15px");
        mapsTab.appendChild(mapLayerTabElement.element());

        dataTabElement = new DataTabElement(messages, FILES_SERVER_URL);
        dataTabElement.element().style.marginTop = CSSProperties.MarginTopUnionType.of("15px");
        dataTab.appendChild(dataTabElement.element());
                
        topLevelContent.appendChild(tabsPanel.element());

        if (filter != null && filter.trim().length() > 0) {
            textBox.setValue(filter);
            textBox.element().dispatchEvent(new KeyboardEvent("keyup"));
        }
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