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
import org.dominokit.domino.ui.datatable.ColumnConfig;
import org.dominokit.domino.ui.datatable.DataTable;
import org.dominokit.domino.ui.datatable.TableConfig;
import org.dominokit.domino.ui.datatable.store.LocalListDataStore;
import org.dominokit.domino.ui.forms.TextBox;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.utils.TextNode;
import org.jboss.elemento.IsElement;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

import ch.so.agi.sodata.App.ThemePublicationMapper;
import ch.so.agi.sodata.dto.FileFormatDTO;
import ch.so.agi.sodata.dto.ThemePublicationDTO;
import elemental2.dom.AbortController;
import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLElement;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.RequestInit;

public class DataElement implements IsElement<HTMLElement> {
    private final HTMLElement root;

    // search box
    private MyMessages messages;
    private String filesServerUrl;
    private TextBox textBox;
    
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
    
    // Create model mapper interfaces
    public static interface ThemePublicationMapper extends ObjectMapper<List<ThemePublicationDTO>> {
    }

    public DataElement(MyMessages messages, String filesServerUrl, TextBox textBox) {
        root = div().element();
        
        this.messages = messages;
        this.filesServerUrl = filesServerUrl;
        this.textBox = textBox;
                
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

            console.log(themePublications.size());
            
            init();

            return null;
        }).catch_(error -> {
            console.log(error);
            DomGlobal.window.alert(error.toString());
            return null;
        });
    }
    
    private void init() {
        // Keyup-Event der Suche hinzufügen.
        textBox.addEventListener("keyup", event -> {
            if (textBox.getValue().trim().length() > 0 && textBox.getValue().trim().length() <= 2) {
                themePublicationListStore.setData(themePublications);
                return;
            }

            if (textBox.getValue().trim().length() == 0) {
                themePublicationListStore.setData(themePublications);
                //removeQueryParam(FILTER_PARAM_KEY);
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

                //updateUrlLocation(FILTER_PARAM_KEY, textBox.getValue().trim());

                abortController = null;

                return null;
            }).catch_(error -> {
                console.log(error);
                return null;
            });
        });
        
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
                                    //openMetadataDialog(cell.getRecord());
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
                                        //openRegionSelectionDialog(cell.getRecord());
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
    
    public void resetStore() {
        themePublicationListStore.setData(themePublications);
    } 
    
    @Override
    public HTMLElement element() {
        return root;
    }

}
