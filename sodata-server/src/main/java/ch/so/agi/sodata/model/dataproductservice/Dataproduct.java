
package ch.so.agi.sodata.model.dataproductservice;

import java.util.HashMap;
import java.util.List;
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
    "dataproduct_id",
    "display",
    "dset_info",
    "stacktype",
    "sublayers",
    "type"
})
@Generated("jsonschema2pojo")
public class Dataproduct {

    @JsonProperty("dataproduct_id")
    private String dataproductId;
    @JsonProperty("display")
    private String display;
    @JsonProperty("dset_info")
    private Boolean dsetInfo;
    @JsonProperty("stacktype")
    private String stacktype;
    @JsonProperty("sublayers")
    private List<Sublayer> sublayers = null;
    @JsonProperty("type")
    private String type;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("dataproduct_id")
    public String getDataproductId() {
        return dataproductId;
    }

    @JsonProperty("dataproduct_id")
    public void setDataproductId(String dataproductId) {
        this.dataproductId = dataproductId;
    }

    @JsonProperty("display")
    public String getDisplay() {
        return display;
    }

    @JsonProperty("display")
    public void setDisplay(String display) {
        this.display = display;
    }

    @JsonProperty("dset_info")
    public Boolean getDsetInfo() {
        return dsetInfo;
    }

    @JsonProperty("dset_info")
    public void setDsetInfo(Boolean dsetInfo) {
        this.dsetInfo = dsetInfo;
    }

    @JsonProperty("stacktype")
    public String getStacktype() {
        return stacktype;
    }

    @JsonProperty("stacktype")
    public void setStacktype(String stacktype) {
        this.stacktype = stacktype;
    }

    @JsonProperty("sublayers")
    public List<Sublayer> getSublayers() {
        return sublayers;
    }

    @JsonProperty("sublayers")
    public void setSublayers(List<Sublayer> sublayers) {
        this.sublayers = sublayers;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
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