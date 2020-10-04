package ch.so.agi.sodata.client;

import static java.util.Objects.nonNull;
import static elemental2.dom.DomGlobal.console;

import java.util.List;

import org.dominokit.domino.ui.datatable.events.SearchEvent;
import org.dominokit.domino.ui.datatable.model.Category;
import org.dominokit.domino.ui.datatable.model.Filter;
import org.dominokit.domino.ui.datatable.store.SearchFilter;

import ch.so.agi.sodata.shared.Dataset;

public class DatasetSearchFilter implements SearchFilter<Dataset> {

    @Override
    public boolean filterRecord(SearchEvent searchEvent, Dataset dataset) {
        List<Filter> searchFilters = searchEvent.getByCategory(Category.SEARCH);

        boolean foundBySearch = searchFilters.isEmpty() || foundBySearch(dataset, searchFilters.get(0));

        return foundBySearch;
    }

    private boolean foundBySearch(Dataset dataset, Filter searchFilter) {
        if (nonNull(searchFilter.getValues().get(0)) && !searchFilter.getValues().get(0).isEmpty()) {
            return filterByAll(dataset, searchFilter.getValues().get(0));
        }
        return true;
    }
    
    private boolean filterByAll(Dataset dataset, String searchText) {
        return filterByTitle(dataset, searchText)
                || filterById(dataset, searchText);
    }

    private boolean filterById(Dataset dataset, String searchText) {
        return dataset.getId().toLowerCase().contains(searchText.toLowerCase());
    }

    private boolean filterByTitle(Dataset dataset, String searchText) {
        return dataset.getTitle().toLowerCase().contains(searchText.toLowerCase());
    }

}
