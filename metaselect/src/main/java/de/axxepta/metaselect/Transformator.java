package de.axxepta.metaselect;

import de.axxepta.converterservices.proc.Pipeline;
import de.axxepta.converterservices.servlet.ServletUtils;
import de.axxepta.converterservices.utils.IOUtils;
import de.axxepta.converterservices.tools.Saxon;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static de.axxepta.metaselect.Configuration.*;
import static de.axxepta.metaselect.Configuration.BASE_PATH;
import static de.axxepta.metaselect.Configuration.TEMP_PATH;

class Transformator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Transformator.class);
    private static final List<String> activeDirectories = new ArrayList<>();

    private String workPath = "";
    private String dateString = "";
    private APIConnector connector;

    public static final String SRC_PATH = "/scr/main/ressources/";
    private static Saxon saxon = new Saxon();

    Transformator(APIConnector apiConnector){
        connector = apiConnector;
    }

    
    Object handleRequest(Request request, Response response) {
        if (setTemPath() == -1)
            return "Could not create temporary directory.";

        workPath = TEMP_PATH + "/" + dateString;

        Map<String, String> formFields = new HashMap<>();
        ServletUtils.parseMultipartRequest(request, null, formFields, workPath);
        if (!formFields.containsKey(Configuration.TARGET) || formFields.get(Configuration.TARGET).equals("") ) {
            response.status(400);
            return val(TEXT_ERROR_NO_PARAM);
        }
        String targetSystem = formFields.get(Configuration.TARGET);
        String topLevel = formFields.get(Configuration.TOPLEVEL);
        String topLevelId = APIConnector.getConceptId(topLevel);

        LOGGER.debug("current target: " + targetSystem);
        LOGGER.debug("current toplevel: " + topLevel);
        LOGGER.debug("current toplvl id: " + topLevelId);

        //ST4 VIEWER
        if (targetSystem.equals(TARGET_ST4_VIEWER)) {

            List<List<String>> allConcepts = new ArrayList<>();
            List<String> topLevels = APIConnector.getST4ViewerConcepts();
            for (String conceptId : topLevels) {
                allConcepts.add(downloadData(conceptId, true));
            }

            return transformMulti(response,
                    targetSystem,
                    allConcepts,
                    topLevels);

        } else if (targetSystem.equals(TARGET_TID2)) {

             String prefix = "TID2_";
             String resultFile = connector.getSparqlResults("sparql/sparql_zeiss_IC_short_conScheme_path.rq", prefix, topLevelId, topLevel);

                try {
                    return buildFileResponse(response, resultFile, "");
                } catch (IOException ex) {
                    return val(TEXT_ERROR_DOWNLOAD);
                }
            
        
       }  else if (targetSystem.equals(TARGET_EMPOLISWIDEN)) {

             String prefix = "EMPOLISWIDEN";
             String resultFile = connector.getSparqlResults("sparql/empolis_widen_sparql.rq", prefix, topLevelId, topLevel);

                try {
                    return buildFileResponse(response, resultFile, "");
                } catch (IOException ex) {
                    return val(TEXT_ERROR_DOWNLOAD);
                }
            
        
       } else {

            List<String> concepts = downloadData(topLevel, false);
            if (concepts.size() == 0) {
                response.status(501);
                return val(TEXT_ERROR_NO_DATA);
            }

            if (localSave()) {
                Configuration.loadOutputDirs();
            }

            return transform(response,
                    targetSystem,
                    concepts,
                    topLevel.replaceAll(" ", "_"),
                    topLevelId);
        }
    }

    private Pipeline.PipelineBuilder pipelineHead() {
        return Pipeline.builder().setInputPath(workPath + "/")
                .setWorkPath(workPath + "/")
                .setHttpHost(val(HOST))
                .setHttpUser(val(USER))
                .setHttpPwd(val(PWD))
                .verbose();
    }

    private Object transform(Response response, String target, List<String> concepts, String conceptName, String conceptId) {
        final String lastLanguageStep = "lastLangStep";
        final String targetId = Target.get(target).toString();

        final String currentConceptParam = "currentConceptSchemeId=" + conceptId;
        final String configFileParam = "configFilePathRaw=" + BASE_PATH + "/config.xml";
        final String conceptSchemeParam = "conceptSchemeFilePathRaw=" + TEMP_PATH + "/" + APIConnector.SCHEMES;

        String dateString = new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new Date());

        Pipeline.PipelineBuilder pipelineBuilder = pipelineHead();

        if (localSave()) {
            pipelineBuilder = pipelineBuilder.setOutputPath(outputDir(targetId));
        }

        for (String concept : concepts) {
            pipelineBuilder = pipelineBuilder.step(Pipeline.StepType.XSLT,
                    "",
                    concept,
                    "tr_" + concept,
                    "file://" + BASE_PATH + "/xsl/010_prepareJsonXML.xsl",
                    true);
        }

        String[] languages = concepts.stream()
                .map(e -> e.substring(e.lastIndexOf("_") + 1, e.lastIndexOf(".")))
                .toArray(String[]::new);

        pipelineBuilder = pipelineBuilder.
                step(Pipeline.StepType.XSLT, languages.length < 2 ? lastLanguageStep : "",
                        "tr_concept_" + conceptId + "_" + val(MASTER_LANGUAGE) + ".xml",
                        conceptId + "_master.xml",
                        "file://" + BASE_PATH + "/xsl/100_buildTreeMasterLang.xsl", true,
                        conceptSchemeParam,
                        currentConceptParam,
                        configFileParam,
                        "currentLanguage=" + val(MASTER_LANGUAGE));

        int langCounter = 0;
        for (String lang : languages) {
            if (!lang.equals(val(MASTER_LANGUAGE))) {
                pipelineBuilder = pipelineBuilder.step(Pipeline.StepType.XSLT,
                        langCounter == languages.length - 2 ? lastLanguageStep : "",
                        "",
                        "110_MergeOtherLanguage_" + lang + ".xml",
                        "file://" + BASE_PATH + "/xsl/110_MergeOtherLanguage.xsl", true,
                        "newLanguageFilePathRaw=" + workPath + "/tr_concept_" + conceptId + "_" + lang + ".xml",
                        currentConceptParam,
                        conceptSchemeParam,
                        configFileParam,
                        "currentLanguage=" + lang);
                langCounter++;
            }
        }

        pipelineBuilder = pipelineBuilder.
                step(Pipeline.StepType.XSLT, "",
                        "",
                        "",
                        "file://" + BASE_PATH + "/xsl/190_checks.xsl", true,
                        "outFilePathRaw=" + workPath + "/" + OPTIONAL_LOG_FILE);

        switch (target) {
            case Configuration.TARGET_ST4: pipelineBuilder = pipelineBuilder.step(Pipeline.StepType.XSLT, "",
                    "step://" + lastLanguageStep,
                            targetId + "_" + conceptId + ".csv",
                            "file://" + BASE_PATH + "/xsl/210_tree2st4_table.xsl", true,
                            configFileParam).
                    step(Pipeline.StepType.XML_XLSX, "",
                            "",
                            targetId + "_" + conceptId + "_" + conceptName + "_" + dateString + ".xlsx", "", true,
                            "tr=tr", "td=td");
                break;
            case Configuration.TARGET_TID: pipelineBuilder = pipelineBuilder.step(Pipeline.StepType.XSLT, "",
                        "step://" + lastLanguageStep,
                        targetId + "_" + conceptId + "_" + conceptName + "_" + dateString + ".csv",
                        "file://" + BASE_PATH + "/xsl/210_tree2TID_csv.xsl", true,
                    configFileParam);
                break;
            case Configuration.TARGET_EMPOLIS: pipelineBuilder = pipelineBuilder.step(Pipeline.StepType.XSLT, "",
                        "step://" + lastLanguageStep,
                        targetId + "_" + conceptId + "_" + conceptName + "_" + dateString + ".jsons",
                        "file://" + BASE_PATH + "/xsl/210_tree2empolis_jsons.xsl", true,
                    configFileParam);
                break;
            case Configuration.TARGET_EMPOLISWIDEN: pipelineBuilder = pipelineBuilder.step(Pipeline.StepType.XSLT, "",
                        "step://" + lastLanguageStep,
                        "",
                        "file://" + BASE_PATH + "/xsl/150_widenApiRequestAdditionalInfos.xsl", true,
                    configFileParam
                    ).
                    step(Pipeline.StepType.OUTPUT_CONTENT, "","","","", true).
                    step(Pipeline.StepType.HTTP_GET, "",
                        "",
                        "160_apiResultWiden.json",
                        "", true,
                        "port=-1", "timeout=-1", "protocol=" + val(PROTOCOL)
                    ).
                    step(Pipeline.StepType.JSON_XML, "",
                        "",
                        "160_apiResultWiden.xml",
                        "", true,
                        "root=array"
                    ).
                    step(Pipeline.StepType.REPLACE, "",
                            "",
                            "",
                            "", true,
                            "replace=/", "with=_X2f_",
                            "replace=<_X2f_", "with=</",
                            "replacefirst=<array>", "with=<array xmlns:http=\"dummy\" xmlns:https=\"dummy\">",
                            "inPlace=true"
                    ).

                    step(Pipeline.StepType.XSLT,
                    "",
                    "",
                    "tr_" + "160_apiResultWiden.xml",
                    "file://" + BASE_PATH + "/xsl/010_prepareJsonXML.xsl",
                    true).
                    step(Pipeline.StepType.XSLT, "",
                            "step://" + lastLanguageStep,
                            "",
                            "file://" + BASE_PATH + "/xsl/160_widenMergeAdditionalInfos.xsl", true,
                            configFileParam,
                            "additionalInfosRaw=" + workPath + "/" + "tr_160_apiResultWiden.xml"
                    ).
                    step(Pipeline.StepType.XSLT, "",
                        "",
                        targetId + "_" + conceptId + "_" + conceptName + "_" + dateString + "_widen.csv",
                        "file://" + BASE_PATH + "/xsl/210_tree2empolis_widen.xsl", true,
                    configFileParam);
                break;
        }

        Object result = pipelineBuilder.exec();

        cleanup();

        if (result instanceof List) {
            String pipeOutputFile = (String) ((List) result).get(0);
            if (localSave()) {
                return val(TEXT_MSG_SAVED) +
                        outputDir(targetId) + File.separator +
                        IOUtils.filenameFromPath(pipeOutputFile);
            } else {
                try {
                    return buildFileResponse(response, pipeOutputFile, target);
                } catch (IOException ex) {
                    return val(TEXT_ERROR_DOWNLOAD);
                }
            }
        } else {
            response.status(500);
            return val(TEXT_ERROR_PIPELINE);
        }
    }

    private Object transformMulti(Response response, String target, List<List<String>> allConcepts, List<String> topLevels) {
        final String targetId = Target.get(target).toString();

        final String configFileParam = "configFilePathRaw=" + BASE_PATH + "/config.xml";
        final String conceptSchemeParam = "conceptSchemeFilePathRaw=" + TEMP_PATH + "/" + APIConnector.SCHEMES;

        String dateString = new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new Date());

        Pipeline.PipelineBuilder pipelineBuilder = pipelineHead();

/*        if (localSave()) {
            pipelineBuilder = pipelineBuilder.setOutputPath(outputDir(targetId));
        }*/

        List<String> combinedLanguageFiles = new ArrayList<>();
        int conceptCounter = 0;
        for (String topLevel : topLevels) {

            List<String> concepts = allConcepts.get(conceptCounter);
            String conceptId = APIConnector.getConceptId(topLevel);

            String currentConceptParam = "currentConceptSchemeId=" + conceptId;

            for (String concept : concepts) {
                pipelineBuilder = pipelineBuilder.step(Pipeline.StepType.XSLT,
                        "",
                        concept,
                        "tr_" + concept,
                        "file://" + BASE_PATH + "/xsl/010_prepareJsonXML.xsl",
                        true);
            }

            String[] languages = concepts.stream()
                    .map(e -> e.substring(e.lastIndexOf("_") + 1, e.lastIndexOf(".")))
                    .toArray(String[]::new);

            String langMergeOutputFile = conceptId + "_master.xml";

            pipelineBuilder = pipelineBuilder.step(Pipeline.StepType.XSLT, "",
                        "tr_concept_" + conceptId + "_" + val(MASTER_LANGUAGE) + ".xml",
                        langMergeOutputFile,
                        "file://" + BASE_PATH + "/xsl/100_buildTreeMasterLang.xsl", true,
                        conceptSchemeParam,
                        currentConceptParam,
                        configFileParam,
                        "currentLanguage=" + val(MASTER_LANGUAGE));

            if (languages.length < 2) {
                combinedLanguageFiles.add(langMergeOutputFile);
            }

            int langCounter = 0;
            for (String lang : languages) {
                if (!lang.equals(val(MASTER_LANGUAGE))) {
                    langMergeOutputFile = conceptId + "_110_MergeOtherLanguage_" + lang + ".xml";
                    pipelineBuilder = pipelineBuilder.step(Pipeline.StepType.XSLT, "",
                            "",
                            langMergeOutputFile,
                            "file://" + BASE_PATH + "/xsl/110_MergeOtherLanguage.xsl", true,
                            "newLanguageFilePathRaw=" + workPath + "/tr_concept_" + conceptId + "_" + lang + ".xml",
                            currentConceptParam,
                            conceptSchemeParam,
                            configFileParam,
                            "currentLanguage=" + lang);
                    if (langCounter == languages.length - 2) {
                        combinedLanguageFiles.add(langMergeOutputFile);
                    }
                    langCounter++;
                }
            }

            conceptCounter++;
        }

        pipelineBuilder = pipelineBuilder.step(Pipeline.StepType.XSLT, "",
                "file://" + BASE_PATH + "/xsl/110_MergeOtherLanguage.xsl",
                "",
                "file://" + BASE_PATH + "/xsl/120_mergeConceptSchemes.xsl", true,
                "workPath=" + workPath + "/",
                "mergedConceptFiles=" + String.join(",", combinedLanguageFiles)).

                step(Pipeline.StepType.XSLT, "", "",
                        targetId + "_" + "allConceptSchemes" + "_" + dateString + ".json",
                        "file://" + BASE_PATH + "/xsl/210_tree2st4Viewer_json.xsl", true,
                        configFileParam);


        Object result = pipelineBuilder.exec();

        cleanup();

        if (result instanceof List) {
            String pipeOutputFile = (String) ((List) result).get(0);
            if (localSave()) {
                return val(TEXT_MSG_SAVED) +
                        outputDir(targetId) + File.separator +
                        IOUtils.filenameFromPath(pipeOutputFile);
            } else {
                try {
                    return buildFileResponse(response, pipeOutputFile, target);
                } catch (IOException ex) {
                    return val(TEXT_ERROR_DOWNLOAD);
                }
            }
        } else {
            response.status(500);
            return val(TEXT_ERROR_PIPELINE);
        }
    }


    private List<String> downloadData(String topLevel, boolean shortProp) {
        APIConnector connector = new APIConnector();
        return connector.getST4ViewerConcepts(topLevel, workPath, shortProp);
    }

    private int setTemPath() {
        String dateString = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        synchronized (activeDirectories) {
            if (activeDirectories.contains(dateString)) {
                dateString += "__" + activeDirectories.size();
            }
            activeDirectories.add(dateString);
        }
        try {
            IOUtils.safeCreateDirectory(IOUtils.pathCombine(TEMP_PATH, dateString));
        } catch (IOException ex) {
            return -1;
        }
        this.dateString = dateString;
        return 0;
    }

    private boolean localSave() {
        return val(SAVE_LOCAL).toLowerCase().equals("true");
    }

    private void cleanup() {
        synchronized (activeDirectories) {
            activeDirectories.remove(dateString);
        }
        if (Certificate.AUTO_CLEANUP) {
            TimerTask rdTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        FileUtils.deleteDirectory(new File(workPath));
                    } catch (IOException ex) {
                        LOGGER.warn("Error while deleting directory: ", ex);
                    }
                }
            };
            Timer timer = new Timer();
            timer.schedule(rdTask, 120000);
        }
    }

    private Object buildFileResponse(Response response, String fileName, String target) throws IOException {
/*        if (IOUtils.isFile(IOUtils.pathCombine(workPath, OPTIONAL_LOG_FILE))) {
            HttpServletResponse raw = ServletUtils.multiPartResponse(response);
            File outputFile = new File(fileName);
            try (InputStream is = new BufferedInputStream(new FileInputStream(outputFile))) {
                ServletUtils.addMultiPartFile(
                        raw.getOutputStream(),
                        target.equals(TARGET_ST4) ? Const.TYPE_XLSX : Const.TYPE_CSV,
                        is,
                        IOUtils.filenameFromPath(fileName));
            }
            File logFile = new File(IOUtils.pathCombine(workPath, OPTIONAL_LOG_FILE));
            try (InputStream is2 = new BufferedInputStream(new FileInputStream(logFile))) {
                ServletUtils.addMultiPartFile(
                        raw.getOutputStream(),
                        "application/octet-stream", //"application/force-download",
                        is2,
                        OPTIONAL_LOG_FILE);
            }
            ServletUtils.multiPartClose(raw.getOutputStream());
            return raw;
        } else {*/
            HttpServletResponse raw = ServletUtils.singleFileResponse(response, IOUtils.filenameFromPath(fileName));
            File file = new File(fileName);
            try (InputStream is = new FileInputStream(file)) {
                IOUtils.copyStreams(is, raw.getOutputStream());
                raw.getOutputStream().close();
                return raw;
//            }
        }
    }

}
