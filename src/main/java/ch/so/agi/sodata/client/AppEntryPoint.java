package ch.so.agi.sodata.client;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.*;
import static org.dominokit.domino.ui.style.Unit.px;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.dominokit.domino.ui.button.Button;
import org.dominokit.domino.ui.button.ButtonSize;
import org.dominokit.domino.ui.chips.Chip;
import org.dominokit.domino.ui.dropdown.DropDownMenu;
import org.dominokit.domino.ui.forms.SuggestBox.DropDownPositionDown;
import org.dominokit.domino.ui.grid.Column;
import org.dominokit.domino.ui.grid.Row;
import org.dominokit.domino.ui.forms.Select;
import org.dominokit.domino.ui.forms.SelectOption;
import org.dominokit.domino.ui.forms.SuggestBox;
import org.dominokit.domino.ui.icons.Icon;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.icons.MdiIcon;
import org.dominokit.domino.ui.lists.ListGroup;
import org.dominokit.domino.ui.modals.ModalDialog;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.style.ColorScheme;
import org.dominokit.domino.ui.style.StyleType;
import org.dominokit.domino.ui.style.Styles;
import org.dominokit.domino.ui.themes.Theme;
import org.dominokit.domino.ui.utils.TextNode;
import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.jboss.elemento.HtmlContentBuilder;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;

import ch.so.agi.sodata.shared.Dataset;
import ch.so.agi.sodata.shared.SettingsResponse;
import ch.so.agi.sodata.shared.SettingsService;
import ch.so.agi.sodata.shared.SettingsServiceAsync;
import elemental2.core.Global;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.Headers;
import elemental2.dom.RequestInit;
import ol.Coordinate;
import ol.Map;
import ol.MapBrowserEvent;
import ol.MapEvent;
import ol.events.Event;

public class AppEntryPoint implements EntryPoint {
    private MyMessages messages = GWT.create(MyMessages.class);
    private final SettingsServiceAsync settingsService = GWT.create(SettingsService.class);
    
    // Application settings
    private String myVar;
    
    // Format settings
    private NumberFormat fmtDefault = NumberFormat.getDecimalFormat();
    private NumberFormat fmtPercent = NumberFormat.getFormat("#0.0");
        
    Dataset[] datasets;
    List<Dataset> datasetList;

    public void onModuleLoad() {
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

                DomGlobal.fetch("datasets", requestInit)
                .then(response -> {
                    if (!response.ok) {
                        return null;
                    }
                    return response.text();
                })
                .then(json -> {                    
                    datasets = (Dataset[]) Global.JSON.parse(json);
                    datasetList = Arrays.asList(datasets);
                    
                    console.log(datasetList.get(0).getEpsgCode());
                    
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
        
        HTMLElement container = div().id("container").element();
          
        HTMLElement logoDiv = div().id("logo").element();
        HTMLElement logoCanton = div().add(img().attr("src", GWT.getHostPageBaseURL() + "Logo.png")
                .attr("alt", "Logo Kanton")).element();
        logoDiv.appendChild(logoCanton);
        container.appendChild(logoDiv);

        container.appendChild(div().id("title").textContent("Geodaten Kanton Solothurn").element());
        
        String infoString = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy <a href='https://geoweb.so.ch/geodaten/index.php'>https://geoweb.so.ch/geodaten/index.php</a> eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";
        container.appendChild(div().id("info").innerHtml(SafeHtmlUtils.fromTrustedString(infoString)).element());
        
        
        SuggestBox suggestBox = SuggestBox.create("Suchbegriff", null);
        suggestBox.addLeftAddOn(Icons.ALL.search());
        suggestBox.setAutoSelect(false);
        suggestBox.setFocusColor(Color.RED);
        suggestBox.getInputElement().setAttribute("autocomplete", "off");
        suggestBox.getInputElement().setAttribute("spellcheck", "false");
        DropDownMenu suggestionsMenu = suggestBox.getSuggestionsMenu();
        suggestionsMenu.setPosition(new DropDownPositionDown());

        HTMLElement suggestBoxDiv = div().id("suggestBoxDiv").add(suggestBox).element();
        container.appendChild(div().id("searchPanel").add(div().id("suggestBoxDiv").add(suggestBox)).element());
        

        ListGroup<Dataset> listGroup = ListGroup.<Dataset>create()
                .setBordered(false)
                .setItemRenderer((listGroup1, listItem) -> {
                    
//                    HTMLElement datasetRow = div().id("datasetRow").element();
                    
                    HTMLElement datasetLink = a().attr("class", "datasetLink")
                            //.attr("href", listItem.getValue().getId())
                            //.attr("target", "_blank")
                            .add(TextNode.of(listItem.getValue().getTitle())).element();
                    datasetLink.addEventListener("click", event -> {
                        openDatasetDialog(listItem.getValue());
                    });
                    
                    Row datasetRow = Row.create();
                    datasetRow.appendChild(Column.span11().setContent(datasetLink));
                    
                    Button button = Button.createPrimary(Icons.ALL.arrow_forward())
                            .circle().setSize(ButtonSize.SMALL)
                            .setButtonType(StyleType.DANGER)
                            .style()
                            .setBackgroundColor("#ef5350")
                            .get();
                    
                    datasetRow.appendChild(Column.span1().style().setTextAlign("right").get().setContent(button));

                    listItem.appendChild(div()
                            .css("datasetList")
                            .add(datasetRow));                        
                })
                .setItems(datasetList);
        
        container.appendChild(listGroup.element());
        
        
        body().add(container);
    }
    
    private void openDatasetDialog(Dataset dataset) {
        ModalDialog modal = ModalDialog.create(dataset.getTitle()).setAutoClose(true);
        
        // Short description
        HTMLElement shortDescription = div().css("modal-body-paragraph").add(TextNode.of(dataset.getShortDescription())).element();
        modal.appendChild(shortDescription);
        
        // Last editing date
        Date date = DateTimeFormat.getFormat("yyyy-MM-dd").parse(dataset.getLastEditingDate());
        HTMLElement lastEditingDate = div().css("modal-body-paragraph").add(TextNode.of(
                "Stand der Daten: " + DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_LONG).format(date)))
                .element();
        modal.appendChild(lastEditingDate);

        // Download data
        Row downloadRow = Row.create();        
        Column downloadStringColumn = Column.span3();
        Column downloadSelectColumn = Column.span4();
       
        HTMLElement downloadString = div().css("modal-body-paragraph").add(TextNode.of("Datenbezug:")).element();
        downloadStringColumn.setContent(downloadString); 
//        downloadRow.addColumn(downloadStringColumn);
        
        Select downloadSelect = Select.create("Download")
            .appendChild(SelectOption.create("-", "Datenformat wählen"))
            .appendChild(SelectOption.create("value10", "INTERLIS"))
            .appendChild(SelectOption.create("value20", "GeoPackage"))
            .appendChild(SelectOption.create("value30", "Shapefile"))
            .appendChild(SelectOption.create("value40", "DXF"))
            .setSearchable(false)
            .selectAt(0);
        downloadSelect.setFocusColor(Color.RED);
        downloadSelectColumn.setContent(downloadSelect);
        downloadRow.addColumn(downloadSelectColumn);
//        downloadSelect.blur();
            
//        .addSelectionHandler(
//            (option) -> {
//              Notification.create("Item selected [ " + option.getValue() + " ]")
//                  .show();
//            }))));

        
        modal.appendChild(downloadRow);

        // Show data in map
        String knownWMS = dataset.getKnownWMS();
        HashMap<String,String> queryParams = this.getUrlValues(knownWMS);
        String layers = queryParams.get("LAYERS");
        // TODO: make this optional in gwt-wgc-embed ?
        String layersOpacity = "";
        for (String layer : layers.split(",")) {
            layersOpacity += "1,";
        }
        layersOpacity = layersOpacity.substring(0, layersOpacity.length() - 1);
        String embeddedMap = "<iframe src='https://geo-t.so.ch/api/v1/embed/embed.html?bgLayer=ch.so.agi.hintergrundkarte_sw&layers="+layers+"&layers_opacity="+layersOpacity+"&E=2618000&N=1237800&zoom=5' height='500' style='width: 100%; border:0px solid white;'></iframe>";
        modal.appendChild(div().id("map").css("modal-body-paragraph").innerHtml(SafeHtmlUtils.fromTrustedString(embeddedMap)).element());
        
        
        modal.appendChild(div().id("map2").css("modal-body-paragraph").innerHtml(SafeHtmlUtils.fromTrustedString(embeddedMap)).element());

        
        // TODO service
        
        Row chipRow = Row.create();
        Column chipColumn = Column.span12();
        String[] keywords = dataset.getKeywords().split(",");
        for (String keyword : keywords) {
            chipColumn.appendChild(Chip.create()
                    .setValue(keyword)
                    .setColor(Color.RED_LIGHTEN_1));
        }
        chipRow.appendChild(chipColumn);
        modal.appendChild(div().css("modal-body-paragraph").add(chipRow.element()));
        
        Button closeButton = Button.create("CLOSE").linkify();
        EventListener closeModalListener = (evt) -> modal.close();
        closeButton.addClickListener(closeModalListener);
        modal.appendFooterChild(closeButton);
        modal.large().open();
    }
    
    private HashMap<String, String> getUrlValues(String url) {
        int i = url.indexOf("?");
        HashMap<String, String> paramsMap = new HashMap<String, String>();
        if (i > -1) {
            String searchURL = url.substring(url.indexOf("?") + 1);
            String params[] = searchURL.split("&");

            for (String param : params) {
                String temp[] = param.split("=");
                try {
                    paramsMap.put(temp[0], URL.decodeQueryString(temp[1]));
                } catch (NullPointerException e) {}
            }
        }
        return paramsMap;
    }
    
    private static native void updateURLWithoutReloading(String newUrl) /*-{
        $wnd.history.pushState(newUrl, "", newUrl);
    }-*/;
}