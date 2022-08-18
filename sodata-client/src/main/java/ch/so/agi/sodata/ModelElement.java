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

import ch.so.agi.sodata.model.Dataproduct;
import ch.so.agi.sodata.model.ModelInfo;
import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.dom.AbortController;
import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
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
import elemental2.dom.KeyboardEvent;
import elemental2.dom.Node;
import elemental2.dom.RequestInit;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import ol.OLFactory;
import ol.layer.Image;
import ol.layer.LayerOptions;
import ol.source.ImageWms;
import ol.source.ImageWmsOptions;
import ol.source.ImageWmsParams;

public class ModelElement implements IsElement<HTMLElement> {
    private final HTMLElement root;
    
    private String dataBaseUrl;
    private ModelInfoMapper mapper;
    private List<ModelInfo> models;
    private List<ModelInfo> completeModels;
    
    private TextBox textBox;
    
    private List<Dataproduct> mapLayers;
    private List<Dataproduct> completeMapLayers;

    private HTMLElement resetIcon;
    
    private AbortController abortController = null;

    private HTMLTableElement rootTable = table().element();
//    private HTMLTableSectionElement mapsTableBody = tbody().element();

    public ModelElement(String dataBaseUrl) {
        root = div().element();
        
        this.dataBaseUrl = dataBaseUrl;
                
        // Create the json-mapper
        mapper = GWT.create(ModelInfoMapper.class);

        // Get all models for initialize the table.
        DomGlobal.fetch("https://geo.so.ch/modelfinder/search?ilisite=geo.so.ch&query=geo.so.ch") // TODO da stimmt nocho was beim modelfinder nicht
        .then(response -> {
            if (!response.ok) {
                DomGlobal.window.alert(response.statusText + ": " + response.body);
                return null;
            }
            return response.text();
        })
        .then(json -> {            
            JsPropertyMap<?> parsed = Js.cast(Global.JSON.parse(json));
            parsed.get("geo.so.ch");
            JsArray<ModelInfo> modelInfoArray = (JsArray) parsed.get("geo.so.ch");
            
            models = modelInfoArray.asList();
            Collections.sort(models, new UmlautComparatorModels());
            completeModels = models;
            
            removeResults();

            init();
            return null;
        }).catch_(error -> {
            console.log(error);
            DomGlobal.window.alert(error.toString());
            return null;
        }); 
    }

    public String getSearchText() {
        return textBox.getValue().trim();
    }
    
    public void setSearchText(String searchTerms) {
        textBox.setValue(searchTerms);
        textBox.element().dispatchEvent(new KeyboardEvent("keyup"));
    }

    private void init() {
        textBox = TextBox.create().setLabel("Suchbegriff");
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
                
                Collections.sort(models, new UmlautComparatorModels());

                removeResults();

                updatingResultTableElement(completeModels);

                resetIcon.style.setProperty("color", "rgba(0, 0, 0, 0.54)");

                //removeQueryParam(FILTER_PARAM_KEY);
                
                CustomEventInit eventInit = CustomEventInit.create();
                eventInit.setDetail("");
                eventInit.setBubbles(true);
                CustomEvent cevent = new CustomEvent("searchStringChanged", eventInit);
                root.dispatchEvent(cevent);
            }
        });
        textBox.addRightAddOn(resetIcon);

        textBox.addEventListener("keyup", event -> {
            if (textBox.getValue().trim().length() == 0) {
                removeResults();
                updatingResultTableElement(completeModels);
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

            DomGlobal.fetch("https://geo.so.ch/modelfinder/search?ilisite=geo.so.ch&query=" + textBox.getValue().toLowerCase(), init)
            .then(response -> {
                if (!response.ok) {
                    return null;
                }
                return response.text();
            }).then(json -> {
                JsPropertyMap<?> parsed = Js.cast(Global.JSON.parse(json));
                parsed.get("geo.so.ch");
                JsArray<ModelInfo> modelInfoArray = (JsArray) parsed.get("geo.so.ch");
                
                models = modelInfoArray.asList();
                Collections.sort(models, new UmlautComparatorModels());
                
                removeResults();
                
                updatingResultTableElement(models);
                                
                abortController = null;

                return null;
            }).catch_(error -> {
                console.log(error);
                return null;
            });
        });
        
        root.appendChild(textBox.element());
        
        updatingResultTableElement(models);
        
        root.appendChild(rootTable);        
    }
    
    private void updatingResultTableElement(List<ModelInfo> models) {
        rootTable.id = "maps-table";

        rootTable.appendChild(colgroup()
//                .add(col().attr("span", "1").style("width: 2%"))
                .add(col().attr("span", "1").style("width: 40%"))
                .add(col().attr("span", "1").style("width: 20%"))                
                .add(col().attr("span", "1").style("width: 10%"))
                .add(col().attr("span", "1").style("width: 30%"))
                .element());
        HTMLTableSectionElement mapsTableHead = thead()
                .add(tr()
//                        .add(th().add(""))
                        .add(th().add("Modell"))
                        .add(th().style("text-align:center;").add("Link"))
                        .add(th().style("text-align:center;").add("Version"))
                        .add(th().style("text-align:center;").add("Modellinformation")))
                .element();
        rootTable.appendChild(mapsTableHead);
        
        for (ModelInfo modelInfo : models) {
            HTMLTableSectionElement tbodyParent = tbody().element();
            HTMLTableRowElement tr = tr().element();

//            HTMLElement modelInfoIcon = Icons.ALL.plus_mdi().style().setCursor("pointer").get().element();
//            tr.appendChild((td().add(modelInfoIcon)).element());
            
            
            HTMLElement launchIcon = Icons.ALL.launch_mdi().style().addCss("model-launch-icon").get().element();
            HTMLElement modelLink = a()
                    .attr("class", "icon-link")
                    .attr("href", modelInfo.getFile())
                    .attr("target", "_blank").add(launchIcon).element();
//            tr.appendChild(td().add(span().add(TextNode.of(modelInfo.getName() + " ")).add(modelLink)).element()); 
            tr.appendChild(td().add(span().add(TextNode.of(modelInfo.getName()))).element()); 
            
            HTMLElement modelLinkElement = div().add(Icons.ALL.launch_mdi().style().setCursor("pointer")).element();
            HTMLElement fileLink = a()
                    .attr("class", "icon-link")
                    .attr("href", modelInfo.getFile())
                    .attr("target", "_blank")
                    .add(modelLinkElement)
                    .element();
          tr.appendChild(td().attr("align", "center").add(fileLink).element());
//          tr.appendChild(td().attr("align", "center").add("").element());
            
            tbodyParent.appendChild(tr);


            tr.appendChild(td().style("text-align:center;").add(TextNode.of(modelInfo.getVersion())).element()); 

            HTMLElement metadataLinkElement = div()
                    .add(Icons.ALL.information_outline_mdi().style().setCursor("pointer"))
                    .element();
            metadataLinkElement.addEventListener("click", new EventListener() {
                @Override
                public void handleEvent(Event evt) {
                    openMetadataDialog(modelInfo);
                }
            });
            tr.appendChild(td().attr("align", "center").add(metadataLinkElement).element());
                        
            rootTable.appendChild(tbodyParent);


        }        
    }
    
    private void openMetadataDialog(ModelInfo modelInfo) {
        ModalDialog modal = ModalDialog.create().setAutoClose(true);
        modal.css("modal-object");
        
        modal.add(div().add(b().add(TextNode.of("Modelldatei"))));
        if (modelInfo.getFile() == null) {
            modal.add(div().add(TextNode.of("—")));
        } else {
            modal.add(div().style("text-overflow:ellipsis;overflow:hidden;white-space:nowrap;")
                    .add(a()
                            .attr("class", "default-link")
                            .attr("href",
                                    modelInfo.getFile())
                            .attr("target", "_blank")
                            .add(modelInfo.getFile())
                         ));
        }
        modal.add(div().add(br()));
        
        modal.add(div().add(b().add(TextNode.of("Beschreibung"))));
        if (modelInfo.getShortDescription() == null) {
            modal.add(div().add(TextNode.of("—")));
        } else {
            modal.add(div().add(modelInfo.getShortDescription()));
        }
        modal.add(div().add(br()));

        modal.add(div().add(b().add(TextNode.of("Weiterführende Informationen"))));
        if (modelInfo.getFurtherInformation() == null) {
            modal.add(div().add(TextNode.of("—")));
        } else {
            modal.add(div().style("text-overflow:ellipsis;overflow:hidden;white-space:nowrap;")
                    .add(a()
                            .attr("class", "default-link")
                            .attr("href",
                                    modelInfo.getFurtherInformation())
                            .attr("target", "_blank")
                            .add(modelInfo.getFurtherInformation())
                         ));
        }
        modal.add(div().add(br()));

        modal.add(div().add(b().add(TextNode.of("Fachamt"))));
        if (modelInfo.getIssuer() == null) {
            modal.add(div().add(TextNode.of("—")));
        } else {
            modal.add(div()
                    .add(a()
                            .attr("class", "default-link")
                            .attr("href",
                                    modelInfo.getIssuer())
                            .attr("target", "_blank")
                            .add(modelInfo.getIssuer())
                         ));
        }
        modal.add(div().add(br()));
        
        modal.add(div().add(b().add(TextNode.of("Technischer Kontakt"))));
        if (modelInfo.getTechnicalContact() == null) {
            modal.add(div().add(TextNode.of("—")));
        } else {
            modal.add(div()
                    .add(a()
                            .attr("class", "default-link")
                            .attr("href",
                                    modelInfo.getTechnicalContact())
                            .attr("target", "_blank")
                            .add(modelInfo.getTechnicalContact().substring(7))
                         ));
        }
                
        Button closeButton = Button.create("SCHLIESSEN").linkify();
        closeButton.removeWaves();
        closeButton.setBackground(Color.RED_DARKEN_3);
        EventListener closeModalListener = (evt) -> modal.close();
        closeButton.addClickListener(closeModalListener);
        modal.appendFooterChild(closeButton);
        modal.open();

        closeButton.blur();
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
    
    public static interface ModelInfoMapper extends ObjectMapper<List<ModelInfo>> {
    }
}
