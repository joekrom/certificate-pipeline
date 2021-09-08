package de.axxepta.metaselect;

import de.axxepta.converterservices.tools.Saxon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.*;
import java.util.stream.Collectors;

class Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);


    // my config 

    static String SRC_PATH = "src/main/resources/";
    static String XSL_FILE_PATH = "src/main/resources/xsl/";

    static String BASE_PATH = System.getProperty("user.home") + "/.metaselect";
    static String TEMP_PATH = BASE_PATH + "/temp";

    static final String CONFIG_FILE_NAME    = "config.xml";
    static String CONFIG_FILE         = BASE_PATH + "/" + CONFIG_FILE_NAME;

    static final String OPTIONAL_LOG_FILE    = "log.txt";

    private static final String CRYPTO_PHRASE = "+ZAPPERLOT=BROT-";

    static final String TOPLEVEL        = "toplevel";
    static final String TARGET          = "target";

    static final String LANGUAGES       = "//entry[@type = \"languages\"]/entry[@type = \"language\"]";
    static final String MASTER_LANGUAGE = "//entry[@type = \"language\"][@master = \"true\"]";
    private static final String UI_LANGUAGE     = "//entry[@type = \"language\"][@ui = \"true\"]";

    static final String PROTOCOL = "//entry[@type = \"sourcesystem\"]/entry[@type = \"protocol\"]";
    static final String HOST = "//entry[@type = \"sourcesystem\"]/entry[@type = \"host\"]";
    static final String USER = "//entry[@type = \"login\"]/entry[@type = \"user\"]";
    static final String PWD = "//entry[@type = \"login\"]/entry[@type = \"pwd\"]";
    static final String PATH_TOPLEVEL = "//entry[@type = \"topconcepts\"]/entry[@type = \"path\"]";
    static final String QUERY_TOPLEVEL = "//entry[@type = \"topconcepts\"]/entry[@type = \"query\"]";
    static final String PATH_CONCEPT = "//entry[@type = \"tree\"]/entry[@type = \"path\"]";
    static final String ROOT_CONCEPT = "//entry[@type = \"tree\"]/entry[@type = \"param\"][@name = \"root\"]";
    static final String PROP_SHORT = "//entry[@type = \"tree\"]/entry[@type = \"param\"][@name = \"short_props\"]";
    static final String PROP_CONCEPT = "//entry[@type = \"tree\"]/entry[@type = \"param\"][@name = \"properties\"]";
    static final String EXCLUDE_CONCEPT = "//entry[@type = \"tree\"]/entry[@name = \"excludeIRI\"]";

    static final String XPATH_TOPLEVEL = "XPathTopLevel";
    static final String XPATH_TOPLEVEL_NAME = "XPathTopLevelName";
    static final String XPATH_TOPLEVEL_ID = "XPathTopLevelURI";
    static final String XPATH_TOPLEVEL_IRI = "XPathTopLevelIRI";

    static final String TARGET_ST4 = "SCHEMA ST4";
    static final String TARGET_ST4_VIEWER = "SCHEMA ST4 VIEWER";
    static final String TARGET_TID = "TID";
    static final String TARGET_TID2 = "TID Instrumentcodes";
    static final String TARGET_EMPOLIS = "EMPOLIS";
    static final String TARGET_EMPOLISWIDEN = "EMPOLISWIDEN";


    static final String SAVE_LOCAL = "//entry[@type = \"savelocal\"]";

    private static final String OUTPUT_DIR = "//entry[@type = \"targetsystems\"]/entry[@type = \"outputPath\"][@key = \"%s\"]";

    static final String TEXT_FORM_HEAD          = "//entry[@type = \"uitexts\"]/entry[@key = \"TEXT_FORM_HEAD\"]/entry[@lang = \"%s\"]";
    static final String TEXT_FORM_BUTTON        = "//entry[@type = \"uitexts\"]/entry[@key = \"TEXT_FORM_BUTTON\"]/entry[@lang = \"%s\"]";
    static final String TEXT_FORM_SCHEME        = "//entry[@type = \"uitexts\"]/entry[@key = \"TEXT_FORM_SCHEME\"]/entry[@lang = \"%s\"]";
    static final String TEXT_FORM_TARGET        = "//entry[@type = \"uitexts\"]/entry[@key = \"TEXT_FORM_TARGET\"]/entry[@lang = \"%s\"]";
    static final String TEXT_ERROR_NO_PARAM     = "//entry[@type = \"uitexts\"]/entry[@key = \"TEXT_ERROR_NO_PARAM\"]/entry[@lang = \"%s\"]";
    static final String TEXT_ERROR_NO_DATA      = "//entry[@type = \"uitexts\"]/entry[@key = \"TEXT_ERROR_NO_DATA\"]/entry[@lang = \"%s\"]";
    static final String TEXT_ERROR_PIPELINE     = "//entry[@type = \"uitexts\"]/entry[@key = \"TEXT_ERROR_PIPELINE\"]/entry[@lang = \"%s\"]";
    static final String TEXT_ERROR_DOWNLOAD     = "//entry[@type = \"uitexts\"]/entry[@key = \"TEXT_ERROR_DOWNLOAD\"]/entry[@lang = \"%s\"]";
    static final String TEXT_MSG_SAVED          = "//entry[@type = \"uitexts\"]/entry[@key = \"TEXT_MSG_SAVED\"]/entry[@lang = \"%s\"]";

    private static Map<String,String> config;
    private static Map<String,String> output;

    private Configuration() {}

    static {
        config = new HashMap<>();

        config.put(PROTOCOL, "https");
        config.put(HOST, "zeiss-test.poolparty.biz");
        config.put(USER, "metaselect");
        config.put(PWD, "u6ErnvZrpQ");

        config.put(UI_LANGUAGE, "de");
        config.put(MASTER_LANGUAGE, "en-us"); //TODO: not read from config.xml!

        config.put(LANGUAGES, "en-us;de-de"); //overwritten by config.xml
       // config.put(LANGUAGES, "en;de;bg-bg;cs-cz;da-dk;el-gr;es-es;et-ee;fi-fi;fr-fr;hu-hu;it-it;ja-jp;lv-lv;lt-lt;nl-nl;pl-pl;pt-pt;ro-ro;ru-ru;sk-sk;sl-si;sv-se;tr-tr;zh-cn");

         //TODO: .._TOPLEVEL entries are not being read from config.xml!
        config.put(PATH_TOPLEVEL, "/PoolParty/sparql/RMS");

        /* config.put(QUERY_TOPLEVEL, "PREFIX skos:<http://www.w3.org/2004/02/skos/core#> " +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "PREFIX CZ-MIC:<https://zeiss-test.poolparty.biz/CZ-MIC-Ontology/> " +
                "PREFIX CZmeta:<https://zeiss-test.poolparty.biz/CZmetadata/> " +
                "PREFIX dcterms:<http://purl.org/dc/terms/> " +
                "SELECT * " +
                "WHERE " +
                "{ " +
                "?node a skos:ConceptScheme . " +
                "OPTIONAL {?node rdfs:label ?label} " +
                "OPTIONAL {?node dcterms:title ?title} " +
                "OPTIONAL {?node CZ-MIC:InstrumentCode ?InstrumentCode} " +
                "OPTIONAL {?node CZ-MIC:IRI ?IRI} " +
                "OPTIONAL {?node CZ-MIC:URI4IRI ?URI4IRI} " +
                "OPTIONAL {?node CZ-MIC:SAPCompLocKey ?SAPCompLocKey} " +
                "}"); */
      config.put(QUERY_TOPLEVEL, "PREFIX skos:<http://www.w3.org/2004/02/skos/core#> " +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
               // "PREFIX CZ-MIC:<https://schema.zeiss.com/CZ-MIC-Ontology/> " +
               // "PREFIX CZmeta:<https://meta.zeiss.com/CZmetadata/> " +
                "PREFIX CZ-MIC:<https://schema.zeiss.com/CZ-Ontology/> " +
               // "PREFIX CZmeta:<https://zeiss-test.poolparty.biz/CZmetadata/> " +
                "PREFIX dcterms:<http://purl.org/dc/terms/> " +
                "SELECT * " +
                "WHERE " +
                "{ " +
                "?node a skos:ConceptScheme . " +
                "OPTIONAL {?node rdfs:label ?label} " +
                "OPTIONAL {?node dcterms:title ?title} " +
               // "OPTIONAL {?node CZ-MIC:InstrumentCode ?InstrumentCode} " +
               // "OPTIONAL {?node CZ-MIC:IRI ?IRI} " +
                "OPTIONAL {?node CZ-MIC:URI4IRI ?URI4IRI} " +
                "OPTIONAL {?node CZ-MIC:Skill ?Skill} " +
                "OPTIONAL {?node CZ-MIC:SM-Nummer ?SMNummer} " +
               // "OPTIONAL {?node CZ-MIC:SAPCompLocKey ?SAPCompLocKey} " +
               //"OPTIONAL {?node CZ-MIC:SAPCompKey_2 ?SAPCompKey_2} " +
                "}");
        //config.put(PATH_CONCEPT, "/PoolParty/api/thesaurus/1E267050-DDA9-0001-911C-BA7017C09940/subtree");
        config.put(PATH_CONCEPT, "");
        //config.put(ROOT_CONCEPT, "https://zeiss-test.poolparty.biz/MICMetadata/");
        config.put(ROOT_CONCEPT, "");
        
        //config.put(PROP_SHORT, "skos:altLabel, https://zeiss-test.poolparty.biz/CZ-MIC-Ontology/IRI, https://zeiss-test.poolparty.biz/CZ-MIC-Ontology/URI4IRI");
        config.put(PROP_SHORT, "skos:altLabel, https://schema.zeiss.com/CZ-MIC-Ontology/IRI, https://schema.zeiss.com/CZ-MIC-Ontology/URI4IRI");


        config.put(PROP_CONCEPT, "https://zeiss-test.poolparty.biz/CZ-MIC-Ontology/InstrumentCode, skos:altLabel, skos:definition, " +
                "https://zeiss-test.poolparty.biz/CZ-MIC-Ontology/IRI, https://zeiss-test.poolparty.biz/CZ-MIC-Ontology/URI4IRI, https://zeiss-test.poolparty.biz/CZ-MIC-Ontology/SAPCompLocKey_2, " +
                "https://zeiss-test.poolparty.biz/CZ-MIC-Ontology/SAPCompKey_2, http://schema.semantic-web.at/ppt/appliedType, http://schema.semantic-web.at/ppt/propagateType," +
                "https://zeiss-test.poolparty.biz/CZ-MIC-Ontology/SAPProdKeySHIFT, " +
                "https://zeiss-test.poolparty.biz/CZ-MIC-Ontology/SortKeyESC, " +
                "https://zeiss-test.poolparty.biz/CZ-MIC-Ontology/MapWidenProduct2MICComp, " +
                "https://zeiss-test.poolparty.biz/CZ-MIC-Ontology/MapWidenProduct2MICProd, " +
                "https://zeiss-test.poolparty.biz/CZ-MIC-Ontology/MapWidenProduct2MICSWMod, " +
                "https://zeiss-test.poolparty.biz/CZ-MIC-Ontology/MapWidenProduct2MICSWProd, " +
                "https://zeiss-test.poolparty.biz/CZ-MIC-Ontology/MapWidenRocType2MICDocType, " +
                "http://www.w3.org/2004/02/skos/core#definition"
                );
        config.put(EXCLUDE_CONCEPT, "http://metadata.zeiss.de/cz#ShadowWiden");

        config.put(XPATH_TOPLEVEL, "//root/entry");

        //TODO: make configurable, everytime different ...
        config.put(XPATH_TOPLEVEL_NAME, "./title[@*[local-name() = 'lang'] = \"en-us\"]");
        config.put(XPATH_TOPLEVEL_ID, "./@id");
        config.put(XPATH_TOPLEVEL_IRI, "./@uri4iri");

        config.put(SAVE_LOCAL, "false");

        config.put(TEXT_FORM_HEAD, "Poolparty Metadaten");
        config.put(TEXT_FORM_BUTTON, "Daten abfragen");
        config.put(TEXT_FORM_SCHEME, "Concept Scheme");
        config.put(TEXT_FORM_TARGET, "Zielsystem");
        config.put(TEXT_ERROR_NO_PARAM, "Kein Parameter ausgew?hlt");
        config.put(TEXT_ERROR_NO_DATA, "Es konnten keine Daten abgefragt werden");
        config.put(TEXT_ERROR_PIPELINE, "Fehler bei der Ausf?hrung der Datentransformation");
        config.put(TEXT_ERROR_DOWNLOAD, "Fehler beim ?bermitteln der Ergebnisdatei");
        config.put(TEXT_MSG_SAVED, "Datei gespeichert als ");

        output = new HashMap<>();
        Arrays.stream(Target.values()).forEach(e -> output.put(e.toString(), "E:/Dateien/Projekte/Zeiss/out"));
    }

    static void loadConfig() {
        try {
            CryptoProvider cryptoProvider = new CryptoProvider(CRYPTO_PHRASE);

            Document dom = Saxon.loadDOM(CONFIG_FILE);
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();

            extractConfigVal(dom, xPath, UI_LANGUAGE, false, cryptoProvider);

            extractConfigVal(dom, xPath, PROTOCOL, false, cryptoProvider);
            extractConfigVal(dom, xPath, HOST, false, cryptoProvider);
            extractConfigVal(dom, xPath, USER, false, cryptoProvider);
            extractConfigVal(dom, xPath, PWD, false, cryptoProvider);

            loadOutput(dom, xPath);

            NodeList nodes = (NodeList) xPath.compile(LANGUAGES).evaluate(dom, XPathConstants.NODESET);
            List<String> langs = new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                langs.add(nodes.item(i).getTextContent());
            }
            if (langs.size() > 0) {
                config.replace(LANGUAGES, String.join(";", langs));
            }

            extractConfigVal(dom, xPath, SAVE_LOCAL, false, cryptoProvider);

            extractConfigVal(dom, xPath, PATH_TOPLEVEL, false, cryptoProvider);
            //extractConfigVal(dom, xPath, QUERY_TOPLEVEL, false, cryptoProvider);

            extractConfigVal(dom, xPath, PATH_CONCEPT, false, cryptoProvider);
            extractConfigVal(dom, xPath, ROOT_CONCEPT, false, cryptoProvider);
            extractConfigVal(dom, xPath, PROP_SHORT, false, cryptoProvider);
            extractConfigVal(dom, xPath, PROP_CONCEPT, false, cryptoProvider);
            extractConfigVal(dom, xPath, EXCLUDE_CONCEPT, false, cryptoProvider);

            extractConfigVal(dom, xPath, TEXT_FORM_HEAD, false, cryptoProvider, val(UI_LANGUAGE));
            extractConfigVal(dom, xPath, TEXT_FORM_BUTTON, false, cryptoProvider, val(UI_LANGUAGE));
            extractConfigVal(dom, xPath, TEXT_FORM_SCHEME, false, cryptoProvider, val(UI_LANGUAGE));
            extractConfigVal(dom, xPath, TEXT_FORM_TARGET, false, cryptoProvider, val(UI_LANGUAGE));
            extractConfigVal(dom, xPath, TEXT_ERROR_NO_PARAM, false, cryptoProvider, val(UI_LANGUAGE));
            extractConfigVal(dom, xPath, TEXT_ERROR_NO_DATA, false, cryptoProvider, val(UI_LANGUAGE));
            extractConfigVal(dom, xPath, TEXT_ERROR_PIPELINE, false, cryptoProvider, val(UI_LANGUAGE));
            extractConfigVal(dom, xPath, TEXT_ERROR_DOWNLOAD, false, cryptoProvider, val(UI_LANGUAGE));
            extractConfigVal(dom, xPath, TEXT_MSG_SAVED, false, cryptoProvider, val(UI_LANGUAGE));

        } catch (Exception ex) {
            LOGGER.warn("Error during loading of configuration file");
        }
    }

    static void adjustPaths(String suffix) {
        BASE_PATH = BASE_PATH + "-" + suffix;
        //do not change config file name: 
        //CONFIG_FILE_NAME = "config-" + suffix + ".xml";
        TEMP_PATH = BASE_PATH + "/temp";
        CONFIG_FILE = BASE_PATH + "/" + CONFIG_FILE_NAME;
    }

    private static void loadOutput(Document dom, XPath xPath) {
        Arrays.stream(Target.values()).
                forEach(e -> extractOutputDir(dom, xPath, String.format(OUTPUT_DIR, e.toString()), e.toString()));
    }

    static void loadOutputDirs() {
        try {
            Document dom = Saxon.loadDOM(CONFIG_FILE);
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();
            loadOutput(dom, xPath);
        } catch (Exception ex) {
            LOGGER.warn("Error during loading of configuration file");
        }
    }

    private static void extractConfigVal(Document dom, XPath xPath, String path, boolean encrypted, CryptoProvider cryptoProvider, String... param) throws Exception {
        String queryPath = param.length > 0
                ? String.format(path, param[0])
                : path;
        Node node = (Node) xPath.compile(queryPath).evaluate(dom, XPathConstants.NODE);
        if (node != null) {
            config.replace(path, encrypted ? cryptoProvider.decrypt(node.getTextContent()) : node.getTextContent());
        }
    }

    private static void extractOutputDir(Document dom, XPath xPath, String path, String key) {
        try {
            Node node = (Node) xPath.compile(path).evaluate(dom, XPathConstants.NODE);
            if (node != null) {
                output.replace(key, node.getTextContent());
            }
        } catch (Exception ex) {
            LOGGER.warn("Error loading output directories", ex);
        }
    }

    static String val(String key) {
        return config.get(key);
    }

    static String outputDir(String key) {
        return output.get(key);
    }

    static String getTargetOptions() {
        return "<option>" + Arrays.stream(Target.values()).map(Target::getVal).collect(Collectors.joining("</option><option>")) + "</option>";
    }

    public static void main(String... args) {
        if (args.length > 0) {
            CryptoProvider cryptoProvider = new CryptoProvider(CRYPTO_PHRASE);
            String encrypted = cryptoProvider.encrypt(args[0]);
            System.out.println(encrypted);
            System.out.println(cryptoProvider.decrypt(encrypted));
        }
    }


    enum Target {
        ST4(TARGET_ST4),
        ST4VIEWER(TARGET_ST4_VIEWER),
        TID (TARGET_TID),
        TID2 (TARGET_TID2),
        EMPOLIS (TARGET_EMPOLIS),
        EMPOLISWIDEN (TARGET_EMPOLISWIDEN);


        private String val;

        Target(String val){
            this.val = val;
        }

        String getVal() {
            return val;
        }

        private static final Map<String, Target> lookup = new HashMap<>();

        static {
            for(Target env : Target.values()) {
                lookup.put(env.getVal(), env);
            }
        }

        public static Target get(String url) {
            return lookup.get(url);
        }
    }

}
