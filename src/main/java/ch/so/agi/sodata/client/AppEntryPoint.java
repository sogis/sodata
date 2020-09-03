package ch.so.agi.sodata.client;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.*;
import static org.dominokit.domino.ui.style.Unit.px;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.dominokit.domino.ui.button.Button;
import org.dominokit.domino.ui.button.ButtonSize;
import org.dominokit.domino.ui.dropdown.DropDownMenu;
import org.dominokit.domino.ui.forms.SuggestBox.DropDownPositionDown;
import org.dominokit.domino.ui.grid.Column;
import org.dominokit.domino.ui.grid.Row;
import org.dominokit.domino.ui.forms.SuggestBox;
import org.dominokit.domino.ui.icons.Icon;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.icons.MdiIcon;
import org.dominokit.domino.ui.lists.ListGroup;
import org.dominokit.domino.ui.modals.ModalDialog;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.style.ColorScheme;
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
                        console.log("fubar");
                        openDatasetDialog(listItem.getValue());
                        ModalDialog datasetDialog = createDatasetDialog().large();
                        datasetDialog.open();
                    });
                    
                    

                    Row datasetRow = Row.create();
                    datasetRow.appendChild(Column.span11().setContent(datasetLink));
                    MdiIcon arrow = Icons.ALL.arrow_right_mdi().css("datasetArrow").style().setColor("#ef5350").get();
                    arrow.addClickListener(event -> {
                        Window.open("https://geo.so.ch", "_blank", null);
                    });
                    datasetRow.appendChild(Column.span1().style().setTextAlign("right").get().setContent(arrow));

                    
                    
                    listItem.appendChild(div()
                            .css("datasetList")
                            //.add(span().textContent(listItem.getValue().getTitle())));                        
                            //.add(datasetLink));                        
                            .add(datasetRow));                        
                })
                .setItems(datasetList);
        
        container.appendChild(listGroup.element());
        
        
        body().add(container);
    }
    
    private void openDatasetDialog(Dataset dataset) {
        
    }
    
    private ModalDialog createDatasetDialog() {
        ModalDialog modal = ModalDialog.create("Modal title").setAutoClose(true);
            modal.appendChild(TextNode.of("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."));
            Button closeButton = Button.create("CLOSE").linkify();
            Button saveButton = Button.create("SAVE CHANGES").linkify();
            EventListener closeModalListener = (evt) -> modal.close();
            closeButton.addClickListener(closeModalListener);
            saveButton.addClickListener(closeModalListener);
//            modal.appendFooterChild(saveButton);
            modal.appendFooterChild(closeButton);
            return modal;
    }


    private static native void updateURLWithoutReloading(String newUrl) /*-{
        $wnd.history.pushState(newUrl, "", newUrl);
    }-*/;
}