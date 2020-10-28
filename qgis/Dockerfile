FROM sogis/qgis-server-base:3.10

# copy .qgs and .gpkg for dummy data
COPY subunits.qgs /data/subunits.qgs
COPY ch.so.agi.subunits.amtliche_vermessung.gpkg /data/ch.so.agi.subunits.amtliche_vermessung.gpkg
COPY ch.so.agi.subunits.lidar_2019.dtm_hillshade.gpkg /data/ch.so.agi.subunits.lidar_2019.dtm_hillshade.gpkg

RUN chown -R www-data:www-data /data

#sed command to change URL rewrite
RUN sed -i 's/\^\/qgis\//\^\/wms\//g' /etc/apache2/sites-enabled/qgis-server.conf

HEALTHCHECK --interval=10s --timeout=5s --start-period=20s CMD curl http://localhost/wms/subunits?SERVICE=WMS&REQUEST=GetCapabilities


