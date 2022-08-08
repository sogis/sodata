
package ch.so.agi.sodata.model.dataproductservice;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "count",
    "dataproduct_id",
    "filterword"
})
@Generated("jsonschema2pojo")
public class ResultCount {

    @JsonProperty("count")
    private Object count;
    @JsonProperty("dataproduct_id")
    private String dataproductId;
    @JsonProperty("filterword")
    private String filterword;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("count")
    public Object getCount() {
        return count;
    }

    @JsonProperty("count")
    public void setCount(Object count) {
        this.count = count;
    }

    @JsonProperty("dataproduct_id")
    public String getDataproductId() {
        return dataproductId;
    }

    @JsonProperty("dataproduct_id")
    public void setDataproductId(String dataproductId) {
        this.dataproductId = dataproductId;
    }

    @JsonProperty("filterword")
    public String getFilterword() {
        return filterword;
    }

    @JsonProperty("filterword")
    public void setFilterword(String filterword) {
        this.filterword = filterword;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
