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
import org.dominokit.domino.ui.forms.SuggestBox;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.style.ColorScheme;
import org.dominokit.domino.ui.themes.Theme;
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
                console.log("myVar="+myVar);
//                init();
                
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
                    Dataset ds = datasets[0];
                    console.log(ds.getId());
                    console.log(ds.getFiles()[0]);
                    
                    List<Dataset> datasetList = Arrays.asList(datasets);
                    
                    Collections.sort(datasetList, new Comparator<Dataset>() {
                        @Override
                        public int compare(Dataset o1, Dataset o2) {
                            return o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase());
                        }
                    });

//                    console.log(datasetList.get(0));
//                    console.log(datasetList.get(1));
//                    console.log(datasetList.get(2));
//                    console.log(datasetList.get(3));
                    
                    
//                    init();
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
          
        HTMLElement logoDiv = div().id("logo").element();
        HTMLElement logoCanton = div().add(img().attr("src", GWT.getHostPageBaseURL() + "Logo.png")
                .attr("alt", "Logo Kanton")).element();
        logoDiv.appendChild(logoCanton);
        body().add(logoDiv);

        body().add(div().id("title").textContent("Geodaten Kanton Solothurn"));
        
        SuggestBox suggestBox = SuggestBox.create("Suchbegriff", null);
        suggestBox.addLeftAddOn(Icons.ALL.search());
        suggestBox.setAutoSelect(false);
        suggestBox.setFocusColor(Color.RED);
        suggestBox.getInputElement().setAttribute("autocomplete", "off");
        suggestBox.getInputElement().setAttribute("spellcheck", "false");
        DropDownMenu suggestionsMenu = suggestBox.getSuggestionsMenu();
        suggestionsMenu.setPosition(new DropDownPositionDown());

        
        HTMLElement suggestBoxDiv = div().id("suggestBoxDiv").add(suggestBox).element();
        body().add(div().id("searchPanel").add(div().id("suggestBoxDiv").add(suggestBox)));

        
    }

   private static native void updateURLWithoutReloading(String newUrl) /*-{
        $wnd.history.pushState(newUrl, "", newUrl);
    }-*/;
}