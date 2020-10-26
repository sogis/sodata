package ch.so.agi.sodata.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;

public class SpringRemoteServiceServlet extends RemoteServiceServlet {
    private static final long serialVersionUID = -4332306688541651819L;
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void init() throws ServletException {
         super.init();
         SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, getServletContext());
    }
    
    // TODO: In Produktion überprüfen und eventuell kombinieren mit 
    // ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
    @Override
    protected SerializationPolicy doGetSerializationPolicy(HttpServletRequest request, String moduleBaseURL, String strongName) {        
        SerializationPolicy serializationPolicy = null;
        InputStream is = null;
        
        try {
            String serializationPolicyFilePath = SerializationPolicyLoader.getSerializationPolicyFileName(moduleBaseURL + strongName);

            URL url = new URL(serializationPolicyFilePath);

            HttpURLConnection connection = null;
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(4000);
            connection.setRequestMethod("GET");
                       
            is = connection.getInputStream();


            if (is != null) {
                try {
                    serializationPolicy = SerializationPolicyLoader.loadFromStream(is, null);
                } catch (ParseException e) {
                    log.error("ERROR: Failed to parse the policy file '" + serializationPolicyFilePath + "'", e);
                } catch (IOException e) {
                    log.error("ERROR: Could not read the policy file '" + serializationPolicyFilePath + "'", e);
                }
            } else {
                String message = "ERROR: The serialization policy file '" + serializationPolicyFilePath
                        + "' was not found; did you forget to include it in this deployment?";
                log.error(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // Ignore this error
                }
            }
        }
        return serializationPolicy;
    }
}
