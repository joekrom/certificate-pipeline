package de.axxepta.metaselect;

import de.axxepta.metaselect.xsltTransformation;

//import de.axxepta.converterservices.security.BasicAuthenticationFilter;
import de.axxepta.converterservices.security.BasicAuthenticationFilter;
import de.axxepta.converterservices.servlet.ServletUtils;
import de.axxepta.converterservices.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletResponse;

import static de.axxepta.metaselect.Configuration.*;
import java.io.InputStream;
import java.io.FileInputStream;
import org.apache.xmlbeans.impl.common.IOUtil;
import static spark.Spark.*;


import com.google.gson.Gson;


public class Certificate {

    private static final Logger LOGGER = LoggerFactory.getLogger(Certificate.class);

    private static final String PATH_SELECT                 = "/select";
    private static final String PATH_STOP                   = "/stop";
    private static final String PATH_REFRESH                = "/refresh";
    private static final String PATH_TRANSFORM              = "/transform";

    // private static final String PATH_QUERY                  = "/user-certificate";
    private static final String PATH_QUERY                  = "/query";
    private static final String PATH_CERTIFICATE            = "/get-certifiacte/userId";

    private static final String META_SELECT_FORM            = "static/metadata.html";
    private static final String META_SELECT_FORM_DOWNLOAD   = "static/metadata-download.html";


    static boolean DEBUG = false;
    static boolean AUTO_CLEANUP = false;
    
    private static int PORT = 4567;
    private static String SUFFIX = "std";
    private static String localConfig;

    private static Gson g = new Gson();
    private static final String jsonFormat = "application/json";
    private static final String xmlFormat = "application/xml";

    public static void main(String... args) {
        init(args);
        startServer();
    }

    private static void init(String... args) {

        //TODO: use console args utility
        for (String arg : args) {
            if (arg.toLowerCase().startsWith("-p")) {
                try {
                    PORT = Integer.parseInt(arg.substring(2));
                    port(PORT);
                } catch (NumberFormatException ne) {
                    LOGGER.warn("Provided parameter was not a number, started server on standard port 4567.");
                }
            } else if (arg.toLowerCase().startsWith("-d")) {
                DEBUG = true;
            } else if (arg.toLowerCase().startsWith("-f")) {
                LOGGER.info("using new path: " + arg);
                SUFFIX = arg.substring(2);
                
            }else{
                //do nothing
            }
        }
        Configuration.adjustPaths(SUFFIX);

        //see if user config file is present
        if (IOUtils.pathExists(CONFIG_FILE) && DEBUG) {
            loadConfig();
        //copy default config and load it
        }else{
            try {
             IOUtils.safeCreateDirectory(Configuration.BASE_PATH);

             String config = IOUtils.getResourceAsString("config-" + SUFFIX + ".xml");
             IOUtils.saveStringToFile(config, CONFIG_FILE, "UTF-8");
             loadConfig();

           } catch (IOException ie) {
            ie.printStackTrace();
            LOGGER.error("Could not create config directory");
        }
          
        }

        try {
            IOUtils.safeCreateDirectory(Configuration.BASE_PATH);
            IOUtils.safeCreateDirectory(Configuration.TEMP_PATH);
         //   IOUtils.copyResources(Configuration.BASE_PATH, "xsl", MetadataApp.class);
        } catch (IOException ie) {
            ie.printStackTrace();
            LOGGER.error("Could not create base or temp directory");
        }


        if (!DEBUG) {
            ServletUtils.setJettyLogLevel("WARN");
        }
    };

    private static void startServer() {

        APIConnector connector = new APIConnector();

        String topLevelOptions = connector.getTopLevelOptions();
        String targetOptions = Configuration.getTargetOptions();

        // before(new BasicAuthenticationFilter());

        get(PATH_SELECT, (request, response) ->
                (Configuration.val(SAVE_LOCAL).toLowerCase().equals("true")
                        ? String.format(IOUtils.getResourceAsString(META_SELECT_FORM),
                            val(TEXT_FORM_HEAD),
                            val(TEXT_FORM_HEAD),
                            val(TEXT_FORM_TARGET),
                            targetOptions,
                            val(TEXT_FORM_SCHEME),
                            topLevelOptions,
                            val(TEXT_FORM_BUTTON),
                            PATH_TRANSFORM,
                            Configuration.TARGET_ST4_VIEWER
                        )
                        : String.format(IOUtils.getResourceAsString(META_SELECT_FORM_DOWNLOAD),
                            val(TEXT_FORM_HEAD),
                            val(TEXT_FORM_HEAD),
                            PATH_TRANSFORM,
                            val(TEXT_FORM_TARGET),
                            targetOptions,
                            val(TEXT_FORM_SCHEME),
                            topLevelOptions,
                            val(TEXT_FORM_BUTTON),
                            Configuration.TARGET_ST4_VIEWER
                        )
                )
        );

        get(PATH_REFRESH, (request, response) -> connector.getTopLevelOptions(true));

        post(PATH_TRANSFORM, (request, response) -> new Transformator(connector).handleRequest(request, response));

        post(PATH_QUERY, (request, response) -> {
            String userReq = request.body(); 
            String message = "default message";
            xsltTransformation trans  = new xsltTransformation();
            Workflow flow = new Workflow();
            User user = g.fromJson(userReq, User.class);
            String apiToken = user.apiToken;

            if (user==null) {
				response.status(400);
				return "invalid request";
			}

            if (apiToken.equals(user.getToken())){
                // we can now handling the request deending on the content-type of the header
                message = "valid token";
                String ctype = request.headers("Content-Type");
                Object raw = null;
                switch (ctype) {
                    case jsonFormat:
                        //  we redirect to an other endpoint that will apply the transformation
                        /*trans.to_json();
                        message = "transformation in json format";
                        raw = trans.buildFileResponse(response);*/

                        // test with markus code 
                        flow.to_json();
                        raw = flow.buildFileResponseJson(response);
                        response.status(200); // 200
                        break;
                    case xmlFormat: 
                        message = "transformation in xml format";
                        response.status(201);
                        // trans.to_xml();
                        // raw = "format not supported";
                        //response.redirect("/user-cert-xml");
                        flow.to_xml();
                        raw = flow.buildFileResponseXml(response);
                        break; 
    
                    default:
                        message = "transformation not available";
                        response.status(400);
                        break;
                }    

                return raw;
            }
            else{
                response.status(400);
                message = "invalid token or bad request";
            }
			//res.redirect("/user_cert");
			//return "request for userId:"+user.userID+" "+ " token  :"  +user.apiToken;
            //return trans.buildFileResponse(response);
            return message; 
        });

        get("/hello", (req, res) -> "Hello world: microapp with gradle complete");
        get("/user-cert-xml", (req, res) -> {
            //new Transformator(connector).getCertificate();
            xsltTransformation trans  = new xsltTransformation();
            trans.step_1_Certificate();
            trans.step_2_Certificate();
            return "successful transformation in xml format";
        });
        get("/user-cert-json", (req, res) -> {
            //new Transformator(connector).getCertificate();
            xsltTransformation trans  = new xsltTransformation();
            trans.to_json();
            return "successful transformation in json format";
        });



        get(PATH_STOP, (request, response) -> {
                FileUtils.deleteDirectory(new File(TEMP_PATH));
                stop();
                return "Services stopped.";
        });

        System.out.print("\n\n ------------ \n");
        System.out.println("Type localhost:" + PORT + "/select in your browser to start");
        System.out.println("Type localhost:" + PORT + "/stop in your browser to stop");
        System.out.println("BASE Path: " + Configuration.BASE_PATH);
        System.out.println("TEMP Path: " + Configuration.TEMP_PATH);
        System.out.println("CONFIG Path: " + Configuration.CONFIG_FILE);
        System.out.println("LOCAL CONF (jar path)" + localConfig );
        System.out.print(" ------------ \n\n\n'");
    }

}
