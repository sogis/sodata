package ch.so.agi.sodata.client;

import static org.jboss.elemento.Elements.*;

import ch.so.agi.sodata.shared.DatasetTable;
import com.gargoylesoftware.htmlunit.html.HtmlSummary;
import elemental2.dom.*;
import org.dominokit.domino.ui.datatable.CellRenderer;
import org.dominokit.domino.ui.grid.Column;
import org.dominokit.domino.ui.grid.Row;
import org.dominokit.domino.ui.style.Styles;

import static elemental2.dom.DomGlobal.console;

import org.jboss.elemento.IsElement;

import ch.so.agi.sodata.shared.Dataset;

public class DatasetDetail implements IsElement<HTMLElement> {

    private Row rowElement = Row.create().css("fubar").style().add(Styles.margin_0).get();
    private CellRenderer.CellInfo<Dataset> cell;

    public DatasetDetail(CellRenderer.CellInfo<Dataset> cell) {
        this.cell = cell;
        
        //rowElement.appendChild(div().textContent("fubar"));

        Column descriptionColumn = Column.span4()
                .appendChild(
                        h(4, "Beschreibung")
                )
                .appendChild(
                        p().style("padding-right:30px;").textContent(cell.getRecord().getShortDescription())
                );

        Column contentColumn = Column.span4()
                .appendChild(
                        h(4, "Inhalt")
                );

        Column moreInfoColumn = Column.span4()
                .appendChild(
                        h(4, "Weitere Informationen")
                )
                .appendChild(
                        p().style("padding-right:30px;").textContent("Link zu PDF und geocat.ch etc.")
                );

        if (cell.getRecord().getTables() != null) {
            for (DatasetTable datasetTable : cell.getRecord().getTables()) {
                HTMLElement details = (HTMLElement) DomGlobal.document.createElement("details");
                details.style.paddingBottom = CSSProperties.PaddingBottomUnionType.of("5px");
                details.style.paddingRight = CSSProperties.PaddingRightUnionType.of("30px");
                HTMLElement summary = (HTMLElement) DomGlobal.document.createElement("summary");
                summary.textContent = datasetTable.getTitle();
                HTMLElement p = p().style("padding-top:5px;padding-bottom:5px;").textContent(datasetTable.getDescription()).element();
                details.appendChild(summary);
                details.appendChild(p);

                contentColumn.appendChild(details);
            }
        }

        rowElement.appendChild(descriptionColumn);
        rowElement.appendChild(contentColumn);
        rowElement.appendChild(moreInfoColumn);
    }

    @Override
    public HTMLElement element() {
        return rowElement.element();
    }

}
