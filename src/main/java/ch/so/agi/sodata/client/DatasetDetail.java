package ch.so.agi.sodata.client;

import static org.jboss.elemento.Elements.*;

import org.dominokit.domino.ui.datatable.CellRenderer;
import org.dominokit.domino.ui.grid.Row;
import org.dominokit.domino.ui.style.Styles;

import static elemental2.dom.DomGlobal.console;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import org.jboss.elemento.IsElement;

import ch.so.agi.sodata.shared.Dataset;

public class DatasetDetail implements IsElement<HTMLElement> {

    private Row rowElement = Row.create().css("fubar").style().add(Styles.margin_0).get();
    private CellRenderer.CellInfo<Dataset> cell;

    public DatasetDetail(CellRenderer.CellInfo<Dataset> cell) {
        this.cell = cell;
        
        rowElement.appendChild(div().textContent("fubar"));
        
        //initDetails();
    }

    @Override
    public HTMLElement element() {
        return rowElement.element();
    }

}
