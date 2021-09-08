package de.axxepta.metaselect;

import de.axxepta.converterservices.tools.Saxon;
import de.axxepta.converterservices.utils.HTTPUtils;
import de.axxepta.converterservices.utils.IOUtils;
import de.axxepta.converterservices.utils.JSONUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.text.SimpleDateFormat;

import static de.axxepta.metaselect.Configuration.*;
import static de.axxepta.metaselect.Configuration.BASE_PATH;
import static de.axxepta.metaselect.Configuration.TEMP_PATH;

class APIConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(APIConnector.class);

    private static String topLevelOptions = "";

    private static final Map<String, String> concepts = new HashMap<>();

    private static List<String> st4ViewerConcepts = new ArrayList<>();

    private static Saxon saxon = new Saxon();

    private static final String SCHEMES_RAW = "conceptSchemesRaw.xml";
    static final String SCHEMES = "conceptSchemes.xml";
    private static final String SCHEMES_TRANSFORMATION = "000_prepareConceptSchemes.xsl";

    APIConnector() {}

    String getTopLevelOptions(boolean... refresh) {
        if (topLevelOptions.equals("") || (refresh.length > 0 && refresh[0])) {
            synchronized (concepts) {
                getOptions();
                int nConcepts = concepts.size();
                if (nConcepts > 0) {
                    StringBuilder optionBuilder = new StringBuilder("<option ");
                    int i = 0;
                    for (String key : concepts.keySet()) {
                        //concepts.get(key) = concept id , but backend uses concept name...
                        optionBuilder.append("value=\"").append(key).append("\">").append(key).append("</option>").
                                append((i == nConcepts - 1) ? "" : "<option ");
                        i++;
                    }
                    topLevelOptions = optionBuilder.toString();
                }
            }
        }
        return topLevelOptions;
    }

    private void getOptions() {
        try {
            String query = val(PATH_TOPLEVEL) + "?query=" + enc(val(QUERY_TOPLEVEL));
            String optionsResponse = getFile(query, TEMP_PATH);
            LOGGER.debug("path toplevel:" + val(TOPLEVEL));
            LOGGER.debug("query: " + query);
            //LOGGER.debug("response: " + optionsResponse);
            IOUtils.saveStringToFile(optionsResponse, TEMP_PATH + "/" + SCHEMES_RAW);
            String param = "configFilePathRaw="  + BASE_PATH + "/config.xml";

            saxon.transform(TEMP_PATH + "/" + SCHEMES_RAW,
                    BASE_PATH + "/xsl/" + SCHEMES_TRANSFORMATION,
                    TEMP_PATH + "/" + SCHEMES,
                    param);
            Document dom = Saxon.loadDOM(TEMP_PATH + "/" + SCHEMES);
            extractConcepts(dom);
        } catch (IOException | JSONException | TransformerException | ParserConfigurationException | SAXException ex) {
            LOGGER.error("Could not retrieve top level concepts", ex);
        }
    }
    
    protected String getSparqlResults(String sparqlFile, String prefix, String... sparqlParams){

        String sparql, sparqlRaw, sparqlResultName;
         try{
            sparqlRaw = IOUtils.getResourceAsString(sparqlFile);
            sparql = String.format(sparqlRaw, (Object[]) sparqlParams);
            String dateString = new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new Date());
            sparqlResultName = String.format(prefix+"_%s_%s", (Object[]) sparqlParams).replace(' ', '_') + "_" + dateString + ".csv";

            String query = val(PATH_TOPLEVEL) + "?query=" + enc(sparql);
            LOGGER.debug("sparql query: " + query);

            //TODO: use post instead of get request?
            String sparqlResponse = getFile(query, TEMP_PATH);
            IOUtils.saveStringToFile(sparqlResponse, TEMP_PATH + "/" + "results_sparql.xml");

            String xslPath =  BASE_PATH + "/xsl/" + "005_sparqlResult2CSV.xsl";
            if(prefix == "EMPOLISWIDEN") xslPath =  BASE_PATH + "/xsl/" + "005_sparqlWiden2CSV.xsl";
            //save also as csv
             saxon.transform(TEMP_PATH + "/" + "results_sparql.xml",
                    xslPath,
                    TEMP_PATH + "/" +  sparqlResultName);

            return TEMP_PATH + "/" +  sparqlResultName;

     }catch(IOException | JSONException | TransformerException ex){
         LOGGER.warn("Could not get sparql results");
         ex.printStackTrace();
         return "";
     }
    }

    List<String> getST4ViewerConcepts(String concept, String tempPath, boolean shortProp) {
        String[] langs = val(LANGUAGES).split(";");
        List<String> inputs = new ArrayList<>();
        for (String lang : langs) {
            try {
                String fileName = requestConcept(concept, lang, tempPath, shortProp);
                if (!fileName.equals("")) {
                    inputs.add(fileName);
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("Could not retrieve %s language data of concept %s.", lang, concept), ex);
            }
        }
        return inputs;
    }

    private String requestConcept(String concept, String language, String tempPath, boolean shortProp) throws IOException, JSONException {
        String path = val(PATH_CONCEPT) +
                "?root=" + enc(val(ROOT_CONCEPT)) + concepts.get(concept) +
                "&properties=" + enc(val(shortProp ? PROP_SHORT : PROP_CONCEPT)) +
                "&language=" + language;
        LOGGER.debug("Request concept path: " + path);
        //String conceptData = getJSON(path);
        String conceptData = getJSON(tempPath, path);
        LOGGER.debug("Request concept data: " + conceptData);
        if (conceptData.equals("<array></array>")) {
            return "";
        } else {
            conceptData = conceptData.replaceAll("/", "_X2f_").replaceAll("<_X2f_", "</");
            conceptData = "<array xmlns:http=\"dummy\" xmlns:https=\"dummy\">" + conceptData.substring(7);
            String fileName = "concept_" + concepts.get(concept) + "_" + language + ".xml";
            IOUtils.saveStringToFile(conceptData, tempPath + "/" + fileName);
            return fileName;
        }
    }

    private String enc(String param) {
        try {
            return URLEncoder.encode(param,  "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException ue) {
            return param;
        }
    }

    private String getFile(String path, String tempPath) throws IOException, JSONException {
        String resp = getFile(
                val(PROTOCOL),
                val(HOST),
                path,
                val(USER),
                val(PWD),
                tempPath
        );
        if (resp.equals("")) {
            throw new IOException();
        } else {
            return resp;
        }
    }

    private String getJSON(String tempPath, String path) throws IOException, JSONException {
        String resp = getFile(path, tempPath);
        if (resp.startsWith("[")) {
            resp = "{ \"array\" : " + resp + "}";
        }
        return JSONUtils.JsonToXmlString(resp, "array");
    }

    private void extractConcepts(Document dom) {
        List<String> excludeConceptIRIs = Arrays.asList(val(EXCLUDE_CONCEPT).split(","));
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();
            NodeList nodes = (NodeList) xPath.compile(val(XPATH_TOPLEVEL)).evaluate(dom, XPathConstants.NODESET);
            Node node;
            String id;
            String name;

            concepts.clear();
            for (int i = 0; i < nodes.getLength(); i++) {
                node = (Node) xPath.compile(val(XPATH_TOPLEVEL_ID)).evaluate(nodes.item(i), XPathConstants.NODE);
                id = node.getTextContent();
                node = (Node) xPath.compile(val(XPATH_TOPLEVEL_NAME)).evaluate(nodes.item(i), XPathConstants.NODE);
                if (node != null) {
                    name = node.getTextContent();
                    concepts.put(name, id);
                    node = (Node) xPath.compile(val(XPATH_TOPLEVEL_IRI)).evaluate(nodes.item(i), XPathConstants.NODE);
                    if (node != null && !node.getTextContent().equals("") && !excludeConceptIRIs.contains(node.getTextContent())) {
                        st4ViewerConcepts.add(name);
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.warn("Could not extract node contents");
            ex.printStackTrace();
        }
    }

    private String getFile(String protocol, String host, String path, String user, String password, String tempPath) {
        String tempFile = tempPath + "/~temp";

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "*/*");
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("Authorization",
                "Basic " + Base64.getEncoder().encodeToString((user + ":"  + password).getBytes()));
        headers.put("Accept-Encoding", "gzip, deflate, br");

        try {
            HTTPUtils.get(protocol, host, -1, path, "", "", -1, tempFile, false, headers);
            return IOUtils.loadStringFromFile(tempFile);
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }

    static String getConceptId(String concept) {
        return concepts.get(concept);
    }

    static List<String> getST4ViewerConcepts() {
        return st4ViewerConcepts;
    }
}