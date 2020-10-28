package ch.so.agi.sodata.client;

import static elemental2.dom.DomGlobal.console;

import com.google.gwt.core.client.GWT;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

import elemental2.dom.DomGlobal;
import elemental2.dom.Headers;
import elemental2.dom.RequestInit;
import ol.Collection;
import ol.Coordinate;
import ol.Extent;
import ol.Feature;
import ol.Map;
import ol.MapBrowserEvent;
import ol.MapOptions;
import ol.OLFactory;
import ol.View;
import ol.ViewOptions;
import ol.control.Control;
import ol.event.EventListener;
import ol.format.Wkt;
import ol.geom.Geometry;
import ol.interaction.DefaultInteractionsOptions;
import ol.interaction.Interaction;
import ol.layer.Base;
import ol.layer.Group;
import ol.layer.Image;
import ol.layer.LayerOptions;
import ol.layer.Tile;
import ol.layer.VectorLayerOptions;
import ol.proj.Projection;
import ol.proj.ProjectionOptions;
import ol.source.ImageWms;
import ol.source.ImageWmsOptions;
import ol.source.ImageWmsParams;
import ol.source.TileWms;
import ol.source.TileWmsOptions;
import ol.source.TileWmsParams;
import ol.source.Vector;
import ol.source.VectorOptions;
import ol.source.Wmts;
import ol.source.WmtsOptions;
import ol.style.Fill;
import ol.style.Stroke;
import ol.style.Style;
import ol.tilegrid.TileGrid;
import ol.tilegrid.WmtsTileGrid;
import ol.tilegrid.WmtsTileGridOptions;
import proj4.Proj4;

public class MapPresets {
    
    private MapPresets() {
        throw new AssertionError();
    }

    public static double resolutions[] = new double[] { 4000.0, 2000.0, 1000.0, 500.0, 250.0, 100.0, 50.0, 20.0, 10.0, 5.0, 2.5, 1.0, 0.5, 0.25, 0.1 };

    public static Map getBlackAndWhiteMap(String mapId, String subunitsWmsLayer) {
        Proj4.defs("EPSG:2056", "+proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 +k_0=1 +x_0=2600000 +y_0=1200000 +ellps=bessel +towgs84=674.374,15.056,405.346,0,0,0,0 +units=m +no_defs");

        ProjectionOptions projectionOptions = OLFactory.createOptions();
        projectionOptions.setCode("EPSG:2056");
        projectionOptions.setUnits("m");
        projectionOptions.setExtent(new Extent(2420000, 1030000, 2900000, 1350000));

        Projection projection = new Projection(projectionOptions);

        WmtsOptions wmtsOptions = OLFactory.createOptions();
        wmtsOptions.setUrl("https://geo.so.ch/api/wmts/1.0.0/{Layer}/default/2056/{TileMatrix}/{TileRow}/{TileCol}");
        wmtsOptions.setLayer("ch.so.agi.hintergrundkarte_sw");
        wmtsOptions.setRequestEncoding("REST");
        wmtsOptions.setFormat("image/png");
        wmtsOptions.setMatrixSet("EPSG:2056");
        wmtsOptions.setStyle("default");
        wmtsOptions.setProjection(projection);
        wmtsOptions.setWrapX(true);
        wmtsOptions.setTileGrid(createWmtsTileGrid(projection, resolutions));

        Wmts wmtsSource = new Wmts(wmtsOptions);

        LayerOptions wmtsLayerOptions = OLFactory.createOptions();
        wmtsLayerOptions.setSource(wmtsSource);

        Tile wmtsLayer = new Tile(wmtsLayerOptions);
        wmtsLayer.setOpacity(0.8);

        ViewOptions viewOptions = OLFactory.createOptions();
        viewOptions.setProjection(projection);
        viewOptions.setResolutions(new double[] { 4000.0, 2000.0, 1000.0, 500.0, 250.0, 100.0, 50.0, 20.0, 10.0, 5.0, 2.5, 1.0, 0.5, 0.25, 0.1 });
        View view = new View(viewOptions);
        Coordinate centerCoordinate = new Coordinate(2618000, 1237800);

        view.setCenter(centerCoordinate);
        view.setZoom(6);
        
        MapOptions mapOptions = OLFactory.createOptions();
        mapOptions.setTarget(mapId);
        mapOptions.setView(view);
        mapOptions.setControls(new Collection<Control>());

        DefaultInteractionsOptions interactionOptions = new ol.interaction.DefaultInteractionsOptions();
        interactionOptions.setPinchRotate(false);
        mapOptions.setInteractions(Interaction.defaults(interactionOptions));

        Map map = new Map(mapOptions);
        map.addLayer(wmtsLayer);
        
        // Add subunit wms layer
        ImageWmsParams imageWMSParams = OLFactory.createOptions();
        imageWMSParams.setLayers(subunitsWmsLayer);
        imageWMSParams.set("FORMAT", "image/png");
        imageWMSParams.set("TRANSPARENT", "true");

        ImageWmsOptions imageWMSOptions = OLFactory.createOptions();
        imageWMSOptions.setUrl("http://localhost:8083/wms/subunits"); // TODO: settings
        imageWMSOptions.setRatio(1.2f);
        imageWMSOptions.setParams(imageWMSParams);

        ImageWms imageWMSSource = new ImageWms(imageWMSOptions);

        LayerOptions layerOptions = OLFactory.createOptions();
        layerOptions.setSource(imageWMSSource);
        
        ol.layer.Image wmsLayer = new Image(layerOptions);
        map.addLayer(wmsLayer);
        
        // Add empty vector layer
        VectorOptions vectorSourceOptions = OLFactory.createOptions();
        vectorSourceOptions.setUseSpatialIndex(false); // Notwendig, sonst liefert getFeatureCollection null zurück.
        Vector vectorSource = new Vector(vectorSourceOptions);

        VectorLayerOptions vectorLayerOptions = OLFactory.createOptions();
        vectorLayerOptions.setSource(vectorSource);
        ol.layer.Vector vectorLayer = new ol.layer.Vector(vectorLayerOptions);
        vectorLayer.set("id", "vector");
        map.addLayer(vectorLayer);

        // Add click event for selecting subunit.
        map.addClickListener(new EventListener<MapBrowserEvent>() {
            @Override
            public void onEvent(MapBrowserEvent event) {
                double resolution = map.getView().getResolution();

                double minX = event.getCoordinate().getX() - 50 * resolution;
                double maxX = event.getCoordinate().getX() + 51 * resolution;
                double minY = event.getCoordinate().getY() - 50 * resolution;
                double maxY = event.getCoordinate().getY() + 51 * resolution;

                // TODO: settings
                String getFeatureInfoUrl = "http://localhost:8083/wms/subunits?SERVICE=WMS&version=1.3.0&REQUEST=GetFeatureInfo&x=51&y=51&i=51&j=51&height=101&width=101&srs=EPSG:2056&crs=EPSG:2056&info_format=text%2Fxml&with_geometry=true&with_maptip=false&feature_count=1&FI_POINT_TOLERANCE=2&FI_LINE_TOLERANCE=2&FI_POLYGON_TOLERANCE=2";
                getFeatureInfoUrl += "&layers=" + subunitsWmsLayer;
                getFeatureInfoUrl += "&query_layers=" + subunitsWmsLayer;
                getFeatureInfoUrl += "&bbox=" + minX + "," + minY + "," + maxX + "," + maxY;

//                console.log(getFeatureInfoUrl);
                
                RequestInit requestInit = RequestInit.create();
                Headers headers = new Headers();
                headers.append("Content-Type", "application/x-www-form-urlencoded"); 
                requestInit.setHeaders(headers);
                
                DomGlobal.fetch(getFeatureInfoUrl)
                .then(response -> {
                    if (!response.ok) {
                        return null;
                    }
                    return response.text();
                })
                .then(xml -> {
                    Document messageDom = XMLParser.parse(xml);
                    if (messageDom.getElementsByTagName("Feature").getLength() == 0) {
                        return null;
                    }
                                       
                    Node layerNode = messageDom.getElementsByTagName("Layer").item(0);

                    NodeList featureNodes = ((com.google.gwt.xml.client.Element) layerNode).getElementsByTagName("Feature");
                    String featureId = ((com.google.gwt.xml.client.Element) featureNodes.item(0)).getAttribute("id");
                    
                    Feature feature = vectorSource.getFeatureById(featureId);
                    if (feature != null) {
                        console.log("fubar");
                        vectorSource.removeFeature(feature);
                        return null;
                    } 
                    
                    feature = new Feature();
                    feature.setId(featureId);
                    
                    Style style = new Style();
                    Stroke stroke = new Stroke();
                    stroke.setWidth(8);
                    stroke.setColor(new ol.color.Color(244, 67, 53, 1));
                    style.setStroke(stroke);
                    Fill fill = new Fill();
                    fill.setColor(new ol.color.Color(255, 255, 255, 0.6));
                    style.setFill(fill);
                    feature.setStyle(style);

                    NodeList attrNodes = ((com.google.gwt.xml.client.Element) featureNodes.item(0)).getElementsByTagName("Attribute");
                    for (int i = 0; i < attrNodes.getLength(); i++) {
                        Node attrNode = attrNodes.item(i);
                        
                        String attrName = ((com.google.gwt.xml.client.Element) attrNode).getAttribute("name");
                        String attrValue = ((com.google.gwt.xml.client.Element) attrNode).getAttribute("value");

                        if (attrName.equalsIgnoreCase("geometry")) {
                            feature.setGeometry(new Wkt().readGeometry(attrValue));
                        } else {
                            feature.set(attrName, attrValue);
                        }
                    }
                    vectorSource.addFeature(feature);
                    return null;
                })
                .catch_(error -> {
                    console.log(error);
                    DomGlobal.window.alert(error);
                    return null;
                });

                
            }
        });
        
        return map;
    }
    
    private static TileGrid createWmtsTileGrid(Projection projection, double[] resolutions) {
        WmtsTileGridOptions wmtsTileGridOptions = OLFactory.createOptions();
        
        String[] matrixIds = new String[resolutions.length];

        for (int z = 0; z < resolutions.length; ++z) {
            matrixIds[z] = String.valueOf(z);
        }

        Coordinate tileGridOrigin = projection.getExtent().getTopLeft();
        wmtsTileGridOptions.setOrigin(tileGridOrigin);
        wmtsTileGridOptions.setResolutions(resolutions);
        wmtsTileGridOptions.setMatrixIds(matrixIds);

        return new WmtsTileGrid(wmtsTileGridOptions);
    }
}
