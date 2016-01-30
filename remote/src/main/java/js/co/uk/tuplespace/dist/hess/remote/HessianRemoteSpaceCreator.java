package js.co.uk.tuplespace.dist.hess.remote;

import js.co.uk.tuplespace.space.SpaceCreator;
import com.caucho.hessian.server.HessianServlet;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import js.co.uk.tuplespace.renderers.ConsoleSpaceRenderer;
import js.co.uk.tuplespace.events.EventHint;
import js.co.uk.tuplespace.events.SpaceChangeEvent;
import js.co.uk.tuplespace.events.SpaceRenderer;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Run this 'remotely', it is the server-side code for a tuplespace. This is
 * experimental code. It uses Jetty as a servlet container with a HessianServlet
 * as a controller. See http://hessian.caucho.com/ . The createSpace method is
 * exposed and when called by a client a new HessianSpaceServlet is created and loaded
 * dynamically.
 *
 *
 *
 *
 *
 */
public class HessianRemoteSpaceCreator extends HessianServlet implements SpaceCreator<Void> {

    private static final Logger LOG = Logger.getLogger(HessianRemoteSpaceCreator.class.getName());
    private static final int DEFAULT_PORT = 8080;
    // private static HessianRemoteSpaceCreator remoteSpaceCreator;
    private Server server = null;
    private ServletContextHandler context;
    private SpaceRenderer spaceRenderer;
    private Map<String, HessianRemoteSpace> spaceMap;
    private int port = DEFAULT_PORT;

    /**
     * Starts the Jetty server and places itself as a servlet on /spaceManager.
     * SpaceEvents are delegated to the supplied SpaceRenderer.
     *
     * @param spaceRenderer
     * @throws Exception
     */
    public HessianRemoteSpaceCreator(final SpaceRenderer spaceRenderer) throws Exception {


        final PropertiesConfiguration props = loadProperties();
        setupUsingProperties(props);

        spaceMap = new HashMap<String, HessianRemoteSpace>();
        server = new Server(port);

        context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(this), "/spaceManager");

        server.start();
        server.join();



    }

    /**
     * Starts the Jetty server and places itself as a servlet on /spaceManager.
     * There is no rendering of the spaces
     *
     * @throws Exception
     */
    public HessianRemoteSpaceCreator() throws Exception {
        this(null);

    }

    /**
     * Creates a space, nothing is returned as the creation of a space is really
     * the creation of another servlet that delegates to a space.
     *
     * @param spaceName
     * @return
     */
    public synchronized Void createSpace(String spaceName) {

        if (spaceMap.containsKey(spaceName)) {
            return null;
        }

        final HessianSpaceServlet spServlet = new HessianSpaceServlet(spaceName);

        //space changes go into the renderer
        if (spaceRenderer != null) {
            spServlet.addSpaceChangeListener(spaceRenderer);
            spaceRenderer.spaceChanged(new SpaceChangeEvent(spaceName, null, null, EventHint.SPACE_CREATED));
        }


        final ServletHolder holder = new ServletHolder(spServlet);

        context.addServlet(holder, "/" + spaceName);
        spaceMap.put(spaceName, spServlet);

        //Void type 
        return null;
    }

    /**
     *
     * @return @throws IOException
     */
    private PropertiesConfiguration loadProperties() throws ConfigurationException {

        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration("spaces.properties");
       return propertiesConfiguration;

    }

    private void createRendererFromProperties(final PropertiesConfiguration props) {
        ConsoleSpaceRenderer c;
        final String className = props.getString("renderer");
        if (className != null) {
            try {
                Class clazz = Class.forName(className);
                this.spaceRenderer = (SpaceRenderer) clazz.newInstance();
               
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.WARNING, "Check the properties file.", ex);
            } catch (InstantiationException ex) {
                LOG.log(Level.WARNING, "Check the properties file.", ex);
            } catch (IllegalAccessException ex) {
                LOG.log(Level.WARNING, "Check the properties file.", ex);
            }

        }

      
    }

    private void setupUsingProperties(PropertiesConfiguration props) {

        
        if (props == null) {

            return;
        }

        //alternatives are ALL, DEBUG, INFO, WARN, 
        System.setProperty("org.eclipse.jetty.LEVEL", props.getString("org.eclipse.jetty.LEVEL", "INFO"));
        createRendererFromProperties(props);
        String suppliedPortStr = props.getString("jetty.port", "" + DEFAULT_PORT);
        try {
            port = Integer.parseInt(suppliedPortStr);
        } catch (NumberFormatException nfe) {

            LOG.log(Level.SEVERE, "Problem with 'jetty.port'. Check the properties file.", nfe);
        }

    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {


        new HessianRemoteSpaceCreator();





    }
}
